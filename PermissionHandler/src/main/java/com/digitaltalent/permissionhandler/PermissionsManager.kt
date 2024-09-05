package com.digitaltalent.permissionhandler

import android.Manifest
import android.os.Build
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity


fun FragmentActivity.runWithPermissions(
    vararg permissions: String,
    forceGranted: Boolean = false,
    callback: () -> Unit,
) {
    return runWithPermissionsHandler(
        this,
        forceGranted = forceGranted,
        permissions = permissions,
        callback = callback
    )
}

fun Fragment.runWithPermissions(
    vararg permissions: String,
    forceGranted: Boolean,
    callback: () -> Unit,
    ) {
    runWithPermissionsHandler(
        this,
        permissions = permissions,
        callback = callback,
        forceGranted = forceGranted
    )
}

fun FragmentActivity.runWithGPSPermissions(forceGranted: Boolean = true, callback: () -> Unit) {
    runWithPermissionGPSHandler(this, callback = callback, forceGranted = forceGranted)
}

fun Fragment.runWithGPSPermissions(forceGranted: Boolean = true, callback: () -> Unit) {
    runWithPermissionGPSHandler(this, callback = callback, forceGranted = forceGranted)
}

fun FragmentActivity.runWithLocationPermission(forceGranted: Boolean = true, callback: () -> Unit) {
    runWithPermissionsHandler(
        this,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        forceGranted = forceGranted
    ) {
        runWithGPSPermissions(callback = callback, forceGranted = forceGranted)
    }
}

fun Fragment.runWithLocationPermission(forceGranted: Boolean = true, callback: () -> Unit) {
    runWithGPSPermissions(callback = {
        runWithPermissionsHandler(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            callback = callback,
            forceGranted = forceGranted
        )
    }, forceGranted = forceGranted)
}

fun FragmentActivity.runWithBackgroundLocationPermission(
    forceGranted: Boolean = true,
    callback: () -> Unit
) {
    runWithGPSPermissions(callback = {
        runWithPermissionsHandler(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            forceGranted = forceGranted
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) runWithPermissionsHandler(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                forceGranted = forceGranted,
                callback = callback
            )
            else callback()
        }
    }, forceGranted = forceGranted)
}

fun Fragment.runWithBackgroundLocationPermission(
    forceGranted: Boolean = true,
    callback: () -> Unit
) {
    runWithGPSPermissions(callback = {
        runWithPermissionsHandler(
            this,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            forceGranted = forceGranted
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) runWithPermissionsHandler(
                this,
                Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                callback = callback,
                forceGranted = forceGranted
            )
            else callback()
        }
    }, forceGranted = forceGranted)
}

private fun runWithPermissionGPSHandler(
    target: Any,
    forceGranted: Boolean = true,
    callback: () -> Unit
) {
    val permissionCheckerFragment = PermissionCheckerFragment.newInstance(forceGranted)
    when (target) {
        is FragmentActivity -> {
            target.supportFragmentManager.beginTransaction().apply {
                add(
                    permissionCheckerFragment,
                    PermissionCheckerFragment::class.java.canonicalName
                ).runOnCommit { permissionCheckerFragment.requestPermissionGPSFromUser(callback) }
                    .commitAllowingStateLoss()
            }
            // make sure fragment is added before we do any context based operations
            target.supportFragmentManager.executePendingTransactions()
        }

        is Fragment -> {
            target.childFragmentManager.beginTransaction().apply {
                add(
                    permissionCheckerFragment,
                    PermissionCheckerFragment::class.java.canonicalName
                ).runOnCommit { permissionCheckerFragment.requestPermissionGPSFromUser(callback) }
                    .commitAllowingStateLoss()
            }
            // make sure fragment is added before we do any context based operations
            target.childFragmentManager.executePendingTransactions()
        }
    }
}


private fun runWithPermissionsHandler(
    target: Any,
    vararg permissions: String,
    forceGranted: Boolean,
    callback: () -> Unit
) {
    val context = when (target) {
        is FragmentActivity -> target.applicationContext
        is Fragment -> target.requireContext()
        else -> {
            // cannot handle the permission checking from the any class other than AppCompatActivity/Fragment
            // crash the app RIGHT NOW!
            throw IllegalStateException("Found " + target::class.java.canonicalName + " : No support from any classes other than AppCompatActivity/Fragment")
        }
    }

    // check if we have the permissions
    if (context?.hasSelfPermission(permissions = permissions) == true) {
        callback()
    } else {
        // begin the permission request flow
        val permissionCheckerFragment = PermissionCheckerFragment.newInstance(forceGranted)
        when (target) {
            is FragmentActivity -> {
                target.supportFragmentManager.beginTransaction().apply {
                    add(
                        permissionCheckerFragment,
                        PermissionCheckerFragment::class.java.canonicalName
                    )
                    runOnCommit { permissionCheckerFragment.requestPermissionsFromUser(permissions = permissions) { callback() } }.commitAllowingStateLoss()
                }
                // make sure fragment is added before we do any context based operations
                target.supportFragmentManager.executePendingTransactions()
            }

            is Fragment -> {
                target.childFragmentManager.beginTransaction().apply {
                    add(
                        permissionCheckerFragment,
                        PermissionCheckerFragment::class.java.canonicalName
                    )
                    runOnCommit { permissionCheckerFragment.requestPermissionsFromUser(permissions = permissions) { callback() } }.commitAllowingStateLoss()
                }
                // make sure fragment is added before we do any context based operations
                target.childFragmentManager.executePendingTransactions()
            }
        }

    }
}
