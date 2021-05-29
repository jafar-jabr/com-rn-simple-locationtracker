package com.rn.simple.locationtracker.utils

import android.location.Location
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap

class Formatter {
    fun formatLocation(location: Location): WritableMap {
        val formattedLocation: WritableMap = Arguments.createMap()
        formattedLocation.putDouble("longitude", location.longitude)
        formattedLocation.putDouble("latitude", location.latitude)
        formattedLocation.putBoolean("isFromMockProvider", location.isFromMockProvider)
        return formattedLocation
    }

}