package com.digitaltalent.permissionhandler


import android.os.Bundle
import androidx.fragment.app.Fragment

/**
 * This fragment holds the single permission request and holds it until the flow is completed
 */

class PermissionCheckerFragment : Fragment() {
    companion object {
        private const val ARG_FORCE_GRANTED = "forceGranted"

        fun newInstance(forceGranted: Boolean): PermissionCheckerFragment {
            val args = Bundle()
            args.putBoolean(ARG_FORCE_GRANTED, forceGranted)
            val fragment = PermissionCheckerFragment()
            fragment.arguments = args
            return fragment
        }
    }

    private val forceGranted: Boolean by lazy {
        arguments?.getBoolean(ARG_FORCE_GRANTED) ?: false
    }

    private val permissionSingleHandler = PermissionSingleHandler(this, forceGranted)
    private val permissionMultiHandler = PermissionMultiHandler(this, forceGranted)
    private val permissionGPSHandler = PermissionGPSHandler(this, forceGranted)

    fun requestPermissionsFromUser(vararg permissions: String, onGranted: () -> Unit) {
        if (permissions.size == 1) permissionSingleHandler.runSinglePermission(
            permission = permissions[0],
            onGranted = onGranted
        )
        else permissionMultiHandler.runMultiplePermission(
            permissions = permissions,
            onGranted = onGranted
        )
    }

    fun requestPermissionGPSFromUser(onGranted: () -> Unit) {
        permissionGPSHandler.runGPSPermission(onGranted)
    }


}
