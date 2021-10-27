package com.rn.simple.locationtracker

import android.app.*
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.location.Location
import android.os.*
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.content.edit
import androidx.preference.PreferenceManager
import com.facebook.react.HeadlessJsTaskService
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.rn.simple.locationtracker.utils.ResourcesResolver
import java.util.concurrent.TimeUnit


class TrackingService : Service() {

    companion object Constants {
        // The identifier for the notification displayed for the foreground service.
        private const val NOTIFICATION_ID = 12345578
        // The name of the channel for notifications.
        private const val CHANNEL_ID = "com_rn_simple_location_tracker_channel"
        private const val CHANNEL_NAME = "Simple Location Tracking Channel"
        // The desired interval for location updates. Inexact. Updates may be more or less frequent.
        private const val UPDATE_INTERVAL_IN_SECONDS: Long = 90
        const val EXTRA_LOCATION = "com.rn.simple.location.tracker.updates"
        // The fastest rate for active location updates. Updates will never be more frequent than this value.
        private const val FASTEST_UPDATE_INTERVAL_IN_SECONDS: Long = 60
    }

    private val mBinder = LocalBinder()

    /**
     * Used to check whether the bound activity has really gone away and not unbound as part of an
     * orientation change. We create a foreground service notification only if the former takes
     * place.
     */
    private var mChangingConfiguration = false

    private var mNotificationManager: NotificationManager? = null

    /**
     * Contains parameters used by [com.google.android.gms.location.FusedLocationProviderApi].
     */
    private var mLocationRequest: LocationRequest? = null

    /**
     * Provides access to the Fused Location Provider API.
     */
    private var mFusedLocationClient: FusedLocationProviderClient? = null

    /**
     * Callback for changes in location.
     */
    private var mLocationCallback: LocationCallback? = null

    private var mServiceHandler: Handler? = null

    /**
     * The current location.
     */
    private var mLocation: Location? = null

    override fun onCreate() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        mLocationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                super.onLocationResult(locationResult)
                locationResult?.lastLocation?.let { onNewLocation(it) }
            }
        }
        createLocationRequest()
        getLastLocation()
        val handlerThread = HandlerThread(this::class.java.simpleName)
        handlerThread.start()
        mServiceHandler = Handler(handlerThread.looper)
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // Android O requires a Notification Channel.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the channel for the notification
            val mChannel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW
            )
            mChannel.setSound(null,null)
            // Set the Notification Channel for the Notification Manager.
           mNotificationManager?.run { createNotificationChannel(mChannel) }
//          super.startForeground( NOTIFICATION_ID, null)
        }

    }

    /**
     * Sets the location request parameters.
     */
    private fun createLocationRequest() {
        mLocationRequest = LocationRequest.create().apply {
            interval = TimeUnit.SECONDS.toMillis(UPDATE_INTERVAL_IN_SECONDS)
            fastestInterval = TimeUnit.SECONDS.toMillis(FASTEST_UPDATE_INTERVAL_IN_SECONDS)
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Tells the system to not try to recreate the service after it has been killed.
        return START_NOT_STICKY
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        mChangingConfiguration = true
    }

    override fun onBind(intent: Intent): IBinder? {
        stopForeground(true)
        mChangingConfiguration = false
        return mBinder
    }

    override fun onRebind(intent: Intent) {
        stopForeground(true)
        mChangingConfiguration = false
        super.onRebind(intent)
    }

    override fun onUnbind(intent: Intent): Boolean {
        if (!mChangingConfiguration && LocationUpdatesPreferenceUtil.requestingLocationUpdates(this)) {
//            Timber.i("Starting foreground service")

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(
                        this,
                        Intent(this, TrackingService::class.java)
                )
            }
            startForeground(NOTIFICATION_ID, buildNotification())
        }
        return true // Ensures onRebind() is called when a client re-binds.
    }

    override fun onDestroy() {
        mServiceHandler?.apply { removeCallbacksAndMessages(null) }
    }

    /**
     * Makes a request for location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun requestLocationUpdates() {
        LocationUpdatesPreferenceUtil.setRequestingLocationUpdates(this, true)
        startService(Intent(applicationContext, TrackingService::class.java))
        try {
            mFusedLocationClient?.apply {
                requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper())
            }
        } catch (unlikely: SecurityException) {
            LocationUpdatesPreferenceUtil.setRequestingLocationUpdates(this, false)
        }
    }

    /**
     * Removes location updates. Note that in this sample we merely log the
     * [SecurityException].
     */
    fun removeLocationUpdates() {
        try {
            mFusedLocationClient?.apply { removeLocationUpdates(mLocationCallback) }
            LocationUpdatesPreferenceUtil.setRequestingLocationUpdates(this, false)
            stopSelf()
        } catch (unlikely: SecurityException) {
            LocationUpdatesPreferenceUtil.setRequestingLocationUpdates(this, true)
        }
    }

    private fun getLastLocation() {
        try {
            mFusedLocationClient!!.lastLocation.addOnCompleteListener(OnCompleteListener<Location> { task ->
                if (task.isSuccessful && task.result != null) {
                    mLocation = task.result
                }
            })
        } catch (unlikely: SecurityException) {
        }
    }
    private fun onNewLocation(location: Location) {
        mLocation = location
        try{
            val headlessIntent = Intent(
                    this.applicationContext,
                    LocationUpdateBroadcastReceiver::class.java
            )
            headlessIntent.putExtra(EXTRA_LOCATION, location)
            val name: ComponentName? = this.applicationContext.startService(headlessIntent)
            if (name != null) {
                HeadlessJsTaskService.acquireWakeLockNow(this.applicationContext)
            }
        }catch (ignored: IllegalStateException){
        }
        mNotificationManager!!.notify(NOTIFICATION_ID, buildNotification())
    }

      /**
     * Returns the [NotificationCompat] used as part of the foreground service.
     */
    private fun buildNotification(): Notification {
        val appName: String = ResourcesResolver(applicationContext).getString("app_name")
        val text = "$appName va continua sa iti urmareasca locatia."
        val title ="Info"
        val appIconResourceId: Int = applicationContext.applicationInfo.icon
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(text)
            .setSmallIcon(appIconResourceId)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setTicker(text)
            .setWhen(System.currentTimeMillis())
            .setSound(null)
        return builder.build()
    }

    /**
     * Class used for the client Binder. Since this service runs in the same process as its
     * clients, we don't need to deal with IPC.
     */
    inner class LocalBinder : Binder() {
        internal val service: TrackingService
            get() = this@TrackingService
    }

    internal object LocationUpdatesPreferenceUtil {

        private const val KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates"

        /**
         * Returns true if requesting location updates, false otherwise.
         * @param context The [Context].
         */
        @JvmStatic
        fun requestingLocationUpdates(context: Context): Boolean {
            return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false)
        }

        /**
         * Stores the location updates state in SharedPreferences.
         * @param requestingLocationUpdates The location updates state.
         */
        @JvmStatic
        fun setRequestingLocationUpdates(context: Context, requestingLocationUpdates: Boolean) {
            PreferenceManager.getDefaultSharedPreferences(context)
                .edit { putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates) }
        }
    }
}
