package com.rn.simple.locationtracker

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.annotation.Nullable
import com.facebook.react.HeadlessJsTaskService
import com.facebook.react.jstasks.HeadlessJsTaskConfig
import com.rn.simple.locationtracker.utils.Formatter


class LocationUpdateBroadcastReceiver : HeadlessJsTaskService() {
    @Nullable
    override fun getTaskConfig(intent: Intent): HeadlessJsTaskConfig? {
        val extras: Bundle? = intent.extras
        if (extras != null) {
            val location =
                    intent.getParcelableExtra(TrackingService.EXTRA_LOCATION) as Location?
            location?.let {
                return HeadlessJsTaskConfig(
                        "LocationUpdatesTask",
                        Formatter().formatLocation(location),
                        5000,  // timeout for the task
                        true // optional: defines whether or not  the task is allowed in foreground. Default is false
                )
            }
        }
        return null
    }
}