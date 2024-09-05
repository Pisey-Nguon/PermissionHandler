package com.digitaltalent.permissionhandler

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat


fun Context.hasSelfPermission(permissions: ArrayList<String>): Boolean {
    permissions.forEach {
        if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun Context.hasSelfPermission(permissions: List<String>): Boolean {
    permissions.forEach {
        if (ActivityCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED) {
            return false
        }
    }
    return true
}

fun Context.hasSelfPermission(vararg permissions: String): Boolean {
    return permissions.all { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
}
