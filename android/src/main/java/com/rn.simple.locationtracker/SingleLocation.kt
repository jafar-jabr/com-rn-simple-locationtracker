package com.rn.simple.locationtracker

import android.content.Context
import android.location.Location
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.facebook.react.bridge.ReadableMap;
import com.rn.simple.locationtracker.utils.Formatter

class SingleLocation{

        fun getLastKnownLocation(context: Context, myCallback: (result: ReadableMap?) -> Unit) {
         val mFusedLocationClient: FusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context)
        try {
          mFusedLocationClient.lastLocation.addOnCompleteListener(OnCompleteListener<Location> { task ->
            if (task.isSuccessful && task.result != null) {
              myCallback.invoke(Formatter().formatLocation(task.result!!))
            } else {
              myCallback.invoke(null)
            }
          })
        } catch (unlikely: SecurityException) {
            myCallback.invoke(null)
        }
    }
}
