package com.rn.simple.locationtracker.utils.extensions

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

fun Context.toast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
fun Context.toast(@StringRes messageRes: Int) = toast(getString(messageRes))
fun Context.longToast(message: CharSequence) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
fun Context.longToast(@StringRes messageRes: Int) = longToast(getString(messageRes))