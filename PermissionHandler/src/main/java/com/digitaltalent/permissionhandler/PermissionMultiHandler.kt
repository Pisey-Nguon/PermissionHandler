package com.digitaltalent.permissionhandler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager.GET_META_DATA
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment

class PermissionMultiHandler(private val fragment: Fragment, private val forceGranted: Boolean) {

    private val context: Context by lazy { fragment.requireContext() }
    private var multiplePermissionGranted: (() -> Unit)? = null
    private var permissions = ArrayList<String>()

    private val requestPermissionSettings =
        fragment.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (permissions.isEmpty()) return@registerForActivityResult
            if (!context.hasSelfPermission(permissions = permissions)) {
                showDialogRequestPermission(permissions)
            } else {
                multiplePermissionGranted?.invoke()
            }
        }

    private val requestMultiplePermissionNoCallbackLauncher =
        fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { maps ->
            val permissionDenied = maps.filter { !it.value }.map {
                val info = context.packageManager.getPermissionInfo(it.key, GET_META_DATA)
                val label = context.getString(info.labelRes)
                label
            }
            val labels = permissionDenied.joinToString(separator = ", ")
            when {
                context.hasSelfPermission(maps.map { it.key }) -> {
                    multiplePermissionGranted?.let { it() }
                }

                permissions.map { fragment.shouldShowRequestPermissionRationale(it) }
                    .contains(false) -> {
                    val fullDescription = StringBuilder()
                        .append("This permission lets you access ")
                        .append(labels)
                        .append(". Please enable the permission in the app settings to continue using this feature.")
                        .toString()
                    showDialogAllowInSettings(fullDescription)
                }

                else -> {
                    val fullDescription = StringBuilder()
                        .append("This permission lets you access ")
                        .append(labels)
                        .append(". Please allow the permission to continue using this feature.")
                        .toString()
                    showDialogMultiplePermissionDenied(fullDescription)
                }
            }
        }

    fun runMultiplePermission(vararg permissions: String, onGranted: () -> Unit) {
        multiplePermissionGranted = onGranted
        this.permissions.clear()
        this.permissions.addAll(arrayOf(*permissions))
        when {
            context.hasSelfPermission(arrayListOf(*permissions)) -> {
                onGranted()
            }

            else -> {
                requestMultiplePermissionNoCallbackLauncher.launch(arrayOf(*permissions))
            }
        }
    }

    private fun showDialogMultiplePermissionDenied(fullDescription: String) {
        val builder = AlertDialog.Builder(context)
        val alertDialog = builder.setTitle("Permission Required")
            .setMessage(fullDescription)
            .setPositiveButton("Allow") { _, _ ->
                requestMultiplePermissionNoCallbackLauncher.launch(
                    permissions.toTypedArray()
                )
            }
        if (!forceGranted) {
            alertDialog.setNegativeButton("Don't Allow") { dialog, _ -> dialog.dismiss() }
        }
        alertDialog.show()

    }

    private fun showDialogRequestPermission(permissions: ArrayList<String>) {
        if (permissions.isEmpty()) return
        val permissionDenied = permissions.filter { !context.hasSelfPermission(permissions) }.map {
            val info = context.packageManager.getPermissionInfo(it, GET_META_DATA)
            val label = context.getString(info.labelRes)
            label
        }
        val labels = permissionDenied.joinToString(separator = ", ", truncated = "")
        val fullDescription = StringBuilder()
            .append("This permission lets you access ")
            .append(labels)
            .append(". Please enable the permission in the app settings to continue using this feature.")
            .toString()
        showDialogAllowInSettings(fullDescription)
    }

    private fun showDialogAllowInSettings(fullDescription: String) {
        val builder = AlertDialog.Builder(context)
        val alertDialog = builder.setTitle("Permission Required")
            .setMessage(fullDescription)
            .setPositiveButton("App Settings") { _, _ -> startApplicationDetailsActivity() }
        if (!forceGranted) {
            alertDialog.setNegativeButton("Don't Allow") { dialog, _ -> dialog.dismiss() }
        }
        alertDialog.show()
    }

    private fun startApplicationDetailsActivity() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        requestPermissionSettings.launch(intent)
    }
}
