package com.rn.simple.locationtracker

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.location.LocationManager
import android.os.IBinder
import android.provider.Settings
import androidx.core.content.ContextCompat.startActivity
import com.facebook.react.bridge.Callback
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod

class SimpleLocationTrackerModule(reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

    override fun getName(): String {
        return "SimpleLocationTracker"
    }

  private val appContext: Context = reactContext.applicationContext
  private var trackingActivity: Activity? = null
  // A reference to the service used to get location updates.
  private var mService: TrackingService? = null
  // Tracks the bound state of the service.
  private var mBound = false
  private var isRunning = false

  private val mServiceConnection = object : ServiceConnection {

    override fun onServiceConnected(name: ComponentName, service: IBinder) {
      val binder = service as TrackingService.LocalBinder
      mService = binder.service
      mBound = true
      isRunning = true
    }

    override fun onServiceDisconnected(name: ComponentName) {
      mService = null
      mBound = false
      isRunning = false
    }
  }
  @ReactMethod
  fun initializeLocationTracker(TickTimeFilterValue: Number) {
    trackingActivity = currentActivity;
    if(trackingActivity != null) {
      TrackingService.LocationUpdatesPreferenceUtil.requestingLocationUpdates(appContext)
      val serviceIntent = Intent(appContext, TrackingService::class.java)
      trackingActivity!!.bindService(
        serviceIntent, mServiceConnection, Context.BIND_AUTO_CREATE
      )
    }
  }

  @ReactMethod
  fun startObserving() {
    if(trackingActivity != null) {
      mService?.apply { requestLocationUpdates() }
    }
  }

  @ReactMethod
  fun stopObserving() {
    if(trackingActivity != null) {
      mService?.apply { removeLocationUpdates() }
      isRunning = false
    }
  }

  @ReactMethod
  fun isRunning(cb: Callback) {
    cb.invoke(isRunning)
  }

  @ReactMethod
  fun getLastGeoLocation(cb: Callback) {
    SingleLocation().getLastKnownLocation(appContext){ result ->
      cb.invoke(result)
    }
  }

  @ReactMethod
  fun isLocationEnabledInSettings(cb: Callback) {
    val lm = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    val result = lm.isProviderEnabled(LocationManager.GPS_PROVIDER) && lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    cb.invoke(result)
  }
  @ReactMethod
  fun checkIfLocationOpened(cb: Callback) {
    if(trackingActivity != null) {
      val provider: String = Settings.Secure.getString(trackingActivity?.contentResolver, Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
      val cc: Boolean = (provider.contains("gps") && provider.contains("network"))
      cb(cc)
    }else {
      cb(true)
    }
  }
  @ReactMethod
  fun goToLocationSettings() {
    val inn = Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS)
    inn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    startActivity(appContext, inn, null)
  }

//    private val taskHandler: Handler = Handler()
//    private val periodicTaskRunnable: Runnable = Runnable {
//        run {
//            try {
//                // TODO unbind you service and promote it to foreground service
//                taskHandler.removeCallbacks(periodicTaskRunnable)
//            } catch (e: Exception) {
//                taskHandler.postDelayed(periodicTaskRunnable, TimeUnit.MINUTES.toMillis(2))
//            }
//        }
//    }
//    private fun promoteServiceWithDelay() {
//        taskHandler.postDelayed(periodicTaskRunnable, TimeUnit.MINUTES.toMillis(2))
//    }

  @ReactMethod
  fun releaseTheService() {
    if(trackingActivity != null) {
      if (mBound) {
        // Unbind from the service. This signals to the service that this activity is no longer
        // in the foreground, and the service can respond by promoting itself to a foreground
        // service.
        trackingActivity!!.unbindService(mServiceConnection)
        mBound = false
      }
    }
  }
}
