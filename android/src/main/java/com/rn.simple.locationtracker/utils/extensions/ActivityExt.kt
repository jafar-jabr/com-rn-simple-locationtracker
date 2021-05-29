package com.rn.simple.locationtracker.utils.extensions

import android.app.Activity
import android.view.View
import androidx.annotation.IdRes
import com.rn.simple.locationtracker.utils.unsafeLazy

fun <T : View> Activity.bind(@IdRes res: Int) : Lazy<T> {
    // the lazy delegate is thread safe by default to avoid that the lambda gets computed more than once
//    return lazy { findViewById<T>(res) }

    // since the bind function will only be called by the main thread, we don't need synchronization
    return unsafeLazy { findViewById<T>(res) }
}