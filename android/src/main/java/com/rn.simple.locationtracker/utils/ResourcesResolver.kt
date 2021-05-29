package com.rn.simple.locationtracker.utils

import android.content.Context

class ResourcesResolver(applicationContext: Context) {
    private val appContext: Context = applicationContext;

    private fun getAppResource(name: String?, type: String?): Int {
        return appContext.resources.getIdentifier(name, type, appContext.packageName)
    }

    fun getDrawable(resourceName: String?): Int {
        return getAppResource(resourceName, "drawable")
    }

    fun getString(name: String): String {
        return appContext.getString(getAppResource(name, "string"))
    }
}