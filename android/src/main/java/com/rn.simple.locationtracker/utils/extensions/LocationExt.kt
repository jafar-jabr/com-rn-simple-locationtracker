package com.rn.simple.locationtracker.utils.extensions

import android.location.Location

// Returns the `location` object as a human readable string.
fun Location?.toText() = if (this == null) { "Unknown location" } else { "($latitude, $longitude $isFromMockProvider)" }