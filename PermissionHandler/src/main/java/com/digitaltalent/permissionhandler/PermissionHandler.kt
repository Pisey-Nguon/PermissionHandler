package com.digitaltalent.permissionhandler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import android.net.Uri
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat

interface PermissionInterface{
    fun runSinglePermission(permission:String,execute: () -> Unit)
    fun runSinglePermission(permission:String,callBack: PermissionHandler.SinglePermissionCallback,execute: () -> Unit)
    fun runMultiplePermission(vararg permissions:String, execute:()->Unit)
    fun runMultiplePermission(vararg permissions:String,callBack: PermissionHandler.MultiplePermissionCallback, execute:()->Unit)
}

class PermissionHandler(private val activity: ComponentActivity):PermissionInterface{
    private var multiplePermissionCallback:MultiplePermissionCallback? = null
    private var singlePermissionCallback:SinglePermissionCallback? = null

    private var multiplePermissionExecute:(()-> Unit)? = null
    private var singlePermissionExecute:(()-> Unit)? = null
    private var permissions = ArrayList<String>()
    private var permission:String? = null

    private val requestSinglePermissionWithCallbackLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        val info = activity.packageManager.getPermissionInfo(permission!!, GET_META_DATA)
        val label = activity.getString(info.labelRes)
        val description = activity.getString(info.descriptionRes)
        val permissionInfo = PermissionInfo(permission!!, label, description)
        val fullDescription = "This lets you $label. \n" +
                "To enable this, click App Settings below and activate this permissions"
        val data = SinglePermissionData(permissionInfo, fullDescription)
        when{
            isGranted -> {
                singlePermissionExecute?.let { it() }
            }
            !activity.shouldShowRequestPermissionRationale(permission!!) -> {
                singlePermissionCallback?.onRequestPermissionInSettings(data)
            }
            else -> {
                singlePermissionCallback?.onPermissionDenied(data)
            }
        }
    }



    private val requestSinglePermissionNoCallbackLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted->
        when{
            isGranted->{
                singlePermissionExecute?.let { it() }
            }
            !activity.shouldShowRequestPermissionRationale(permission!!) -> {
                val info = activity.packageManager.getPermissionInfo(permission!!, GET_META_DATA)
                val label = activity.getString(info.labelRes)
                val fullDescription = "This lets you $label. \n" +
                        "To enable this, click App Settings below and activate this permissions"
                showDialogAllowInSettings(fullDescription)
            }
        }

    }


    private val requestMultiplePermissionWithCallbackLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ maps->
        when{
            hasSelfPermission(activity,maps.map { it.key }.toTypedArray()) -> {
                multiplePermissionExecute?.let { it() }
            }
            permissions.map { activity.shouldShowRequestPermissionRationale(it) }.contains(false) -> {
                val permissionDenied  = maps.filter { !it.value }.map {
                    val info = activity.packageManager.getPermissionInfo(it.key, GET_META_DATA)
                    val label = activity.getString(info.labelRes)
                    val description = activity.getString(info.descriptionRes)
                    PermissionInfo(it.key,label, description)
                }
                val labels = permissionDenied.joinToString(separator = ", ", truncated = "") { it.label }
                val fullDescription = "This lets you $labels. \n" +
                        "To enable this, click App Settings below and activate these permissions."
                val data = MultiplePermissionData(permissionDenied,fullDescription)
                multiplePermissionCallback?.onRequestPermissionInSettings(data)
            }
        }
    }

    private val requestMultiplePermissionNoCallbackLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ maps->
        when{
            hasSelfPermission(activity,maps.map { it.key }.toTypedArray()) -> {
                multiplePermissionExecute?.let { it() }
            }
            permissions.map { activity.shouldShowRequestPermissionRationale(it) }.contains(false) -> {
                val permissionDenied  = maps.filter { !it.value }.map {
                    val info = activity.packageManager.getPermissionInfo(it.key, GET_META_DATA)
                    val label = activity.getString(info.labelRes)
                    val description = activity.getString(info.descriptionRes)
                    PermissionInfo(it.key,label, description)
                }
                val labels = permissionDenied.joinToString(separator = ", ") { it.label }
                val fullDescription = "This lets you $labels. \n" +
                        "To enable this, click App Settings below and activate these permissions."
                showDialogAllowInSettings(fullDescription)
            }
        }
    }

    override fun runSinglePermission(permission: String, execute: () -> Unit) {
        singlePermissionExecute = execute
        this@PermissionHandler.permission = permission
        when{
            hasSelfPermission(activity, arrayOf(permission)) -> {
                execute()
            }
            else -> {
                requestSinglePermissionNoCallbackLauncher.launch(permission)
            }
        }
    }

    override fun runSinglePermission(
        permission: String,
        callBack: SinglePermissionCallback,
        execute: () -> Unit
    ) {
        singlePermissionExecute = execute
        singlePermissionCallback = callBack
        this@PermissionHandler.permission = permission
        when{
            hasSelfPermission(activity, arrayOf(permission)) -> {
                execute()
            }
            else -> {
                requestSinglePermissionWithCallbackLauncher.launch(permission)
            }
        }
    }

    override fun runMultiplePermission(vararg permissions: String, execute: () -> Unit) {
        multiplePermissionExecute = execute
        this@PermissionHandler.permissions.clear()
        this@PermissionHandler.permissions.addAll(arrayOf(*permissions))
        when{
            hasSelfPermission(activity, arrayOf(*permissions)) -> {
                execute()
            }
            else -> {
                requestMultiplePermissionNoCallbackLauncher.launch(arrayOf(*permissions))
            }
        }
    }

    override fun runMultiplePermission(
        vararg permissions: String,
        callBack: MultiplePermissionCallback,
        execute: () -> Unit
    ) {

        multiplePermissionExecute = execute
        multiplePermissionCallback = callBack
        this@PermissionHandler.permissions.clear()
        this@PermissionHandler.permissions.addAll(arrayOf(*permissions))
        when{
            hasSelfPermission(activity, arrayOf(*permissions)) -> {
                execute()
            }
            else -> {
                requestMultiplePermissionWithCallbackLauncher.launch(arrayOf(*permissions))
            }
        }
    }

    fun hasSelfPermission(activity: Context, permissions:Array<String>): Boolean {
        permissions.forEach {
            if (ActivityCompat.checkSelfPermission(activity, it) != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }
        return true
    }

    private fun showDialogAllowInSettings(fullDescription: String) {
        AlertDialog.Builder(activity)
            .setMessage(fullDescription)
            .setPositiveButton("App Settings"){dialog,_->
                dialog.dismiss()
                startApplicationDetailsActivity()
            }
            .setNegativeButton("Not Now"){dialog,_->
                dialog.dismiss()
            }
            .show()
    }
    private fun startApplicationDetailsActivity() {
        val intent = Intent()
        intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
        val uri: Uri = Uri.fromParts("package", activity.packageName, null)
        intent.data = uri
        activity.startActivity(intent)
    }

    interface MultiplePermissionCallback {
        fun onPermissionsDenied(data:MultiplePermissionData)
        fun onRequestPermissionInSettings(data:MultiplePermissionData)
    }

    interface SinglePermissionCallback {
        fun onPermissionDenied(data: SinglePermissionData)
        fun onRequestPermissionInSettings(data: SinglePermissionData)
    }

    data class PermissionInfo(val permission: String,val label:String,val description:String)
    data class MultiplePermissionData(val permissionInfoList:List<PermissionInfo>,val fullDescription:String)
    data class SinglePermissionData(val permissionInfo: PermissionInfo,val fullDescription:String)

}