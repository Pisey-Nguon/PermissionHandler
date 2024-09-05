package com.digitaltalent.permissionhandler

import android.app.AlertDialog
import android.content.Context
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

class PermissionGPSHandler(val fragment: Fragment, private val forceGranted: Boolean) {


    private var permissionGPSExecutor: (() -> Unit)? = null
    private var intentSenderRequest: IntentSenderRequest? = null

    private val context: Context by lazy { fragment.requireContext() }

    private val resolutionForResult =
        fragment.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK) permissionGPSExecutor?.invoke() else showDialogForceGPSPermissionDenied()
        }

    private fun showDialogForceGPSPermissionDenied() {
        val builder = AlertDialog.Builder(context)
        val alertDialog = builder.setTitle("Location Permission Required")
            .setMessage("To enhance your experience, we need access to your location. Please enable GPS in your settings to continue using all features of the app.")
            .setPositiveButton("Enable GPS") { _, _ ->
                intentSenderRequest?.let { resolutionForResult.launch(it) }
            }

        if (!forceGranted) {
            alertDialog.setNegativeButton("Not Allow") { dialog, _ -> dialog.dismiss() }
        }
        alertDialog.show()
    }

    fun runGPSPermission(execute: () -> Unit) {
        permissionGPSExecutor = execute
        LocationUtils.enableLoc(fragment.requireActivity(),
            allowRequest = { permissionGPSExecutor?.invoke() },
            launchRequest = {
                intentSenderRequest = it
                resolutionForResult.launch(it)
            }
        )
    }
}