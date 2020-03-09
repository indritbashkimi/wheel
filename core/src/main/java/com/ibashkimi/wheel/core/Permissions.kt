package com.ibashkimi.wheel.core

import android.app.Activity
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

fun Activity.isPermissionGranted(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
}

fun Activity.isPermissionRationaleNeeded(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(this, permission)
}

fun Activity.requestPermission(permission: String, requestCode: Int) {
    ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
}

inline fun AppCompatActivity.checkPermission(
    permission: String,
    whenGranted: (AppCompatActivity.() -> Unit),
    whenExplanationNeed: (AppCompatActivity.() -> Unit),
    whenDenied: (AppCompatActivity.() -> Unit)
) {
    when {
        isPermissionGranted(permission) -> whenGranted()
        isPermissionRationaleNeeded(permission) -> whenExplanationNeed()
        else -> whenDenied()
    }
}

inline fun AppCompatActivity.handlePermissionResult(
    permission: String,
    permissions: Array<String>,
    grantResults: IntArray,
    whenGranted: (AppCompatActivity.() -> Unit),
    whenDenied: (AppCompatActivity.() -> Unit)
) {
    for (i in permission.indices) {
        if (permissions[i] == permission) {
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) whenGranted() else whenDenied()
            return
        }
    }
}


fun Fragment.isPermissionGranted(permission: String): Boolean {
    return ActivityCompat.checkSelfPermission(
        requireContext(),
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

fun Fragment.isPermissionRationaleNeeded(permission: String): Boolean {
    return ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), permission)
}

fun Fragment.requestPermission(permission: String, requestCode: Int) {
    this.requestPermissions(arrayOf(permission), requestCode)
}

inline fun Fragment.checkPermission(
    permission: String,
    whenGranted: (Fragment.() -> Unit),
    whenExplanationNeed: (Fragment.() -> Unit),
    whenDenied: (Fragment.() -> Unit)
) {
    when {
        isPermissionGranted(permission) -> whenGranted()
        isPermissionRationaleNeeded(permission) -> whenExplanationNeed()
        else -> whenDenied()
    }
}