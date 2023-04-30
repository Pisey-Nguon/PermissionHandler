package com.digitaltalent.permissionhandler

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.PackageManager.GET_META_DATA
import android.net.Uri
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment

interface PermissionFragmentInterface{
    fun runSinglePermission(permission:String,execute: () -> Unit)
    fun runSinglePermission(permission:String,callBack: PermissionFragmentHandler.SinglePermissionCallback,execute: () -> Unit)
    fun runMultiplePermission(vararg permissions:String, execute:()->Unit)
    fun runMultiplePermission(vararg permissions:String,callBack: PermissionFragmentHandler.MultiplePermissionCallback, execute:()->Unit)
}

class PermissionFragmentHandler(private val fragment: Fragment):PermissionFragmentInterface{
    private val context:Context
    get() = fragment.requireContext()
    private var multiplePermissionCallback:MultiplePermissionCallback? = null
    private var singlePermissionCallback:SinglePermissionCallback? = null

    private var multiplePermissionExecute:(()-> Unit)? = null
    private var singlePermissionExecute:(()-> Unit)? = null
    private var permissions = ArrayList<String>()
    private var permission:String? = null

    private val requestSinglePermissionWithCallbackLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        val info = context.packageManager.getPermissionInfo(permission!!, GET_META_DATA)
        val label = context.getString(info.labelRes)
        val description = context.getString(info.descriptionRes)
        val permissionInfo = PermissionInfo(permission!!, label, description)
        val fullDescription = "This lets you $label. \n" +
                "To enable this, click App Settings below and activate this permissions"
        val data = SinglePermissionData(permissionInfo, fullDescription)
        when{
            isGranted -> {
                singlePermissionExecute?.let { it() }
            }
            !fragment.shouldShowRequestPermissionRationale(permission!!) -> {
                singlePermissionCallback?.onRequestPermissionInSettings(data)
            }
            else -> {
                singlePermissionCallback?.onPermissionDenied(data)
            }
        }
    }



    private val requestSinglePermissionNoCallbackLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted->
        val info = context.packageManager.getPermissionInfo(permission!!, GET_META_DATA)
        val label = context.getString(info.labelRes)
        when{
            isGranted->{
                singlePermissionExecute?.let { it() }
            }
            !fragment.shouldShowRequestPermissionRationale(permission!!) -> {
                val fullDescription = "This lets you $label. \n" +
                        "To enable this, click App Settings below and activate this permissions"
                showDialogAllowInSettings(fullDescription)
            }
            else -> {
                val fullDescription = "This lets you $label. \n" +
                        "Please allow this permission to use this feature."
                showDialogSinglePermissionDenied(fullDescription)
            }
        }

    }


    private val requestMultiplePermissionWithCallbackLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ maps->
        val permissionDenied  = maps.filter { !it.value }.map {
            val info = context.packageManager.getPermissionInfo(it.key, GET_META_DATA)
            val label = context.getString(info.labelRes)
            val description = context.getString(info.descriptionRes)
            PermissionInfo(it.key,label, description)
        }
        val labels = permissionDenied.joinToString(separator = ", ", truncated = "") { it.label }
        when{
            hasSelfPermission(context,maps.map { it.key }.toTypedArray()) -> {
                multiplePermissionExecute?.let { it() }
            }
            permissions.map { fragment.shouldShowRequestPermissionRationale(it) }.contains(false) -> {
                val fullDescription = "This lets you $labels. \n" +
                        "To enable this, click App Settings below and activate these permissions."
                val data = MultiplePermissionData(permissionDenied,fullDescription)
                multiplePermissionCallback?.onRequestPermissionInSettings(data)
            }
            else -> {
                val fullDescription = "This lets you $labels. \n" +
                        "Please allow these permission to use this feature."
                val data = MultiplePermissionData(permissionDenied,fullDescription)
                multiplePermissionCallback?.onPermissionsDenied(data)
            }
        }
    }

    private val requestMultiplePermissionNoCallbackLauncher = fragment.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ maps->
        val permissionDenied  = maps.filter { !it.value }.map {
            val info = context.packageManager.getPermissionInfo(it.key, GET_META_DATA)
            val label = context.getString(info.labelRes)
            val description = context.getString(info.descriptionRes)
            PermissionInfo(it.key,label, description)
        }
        val labels = permissionDenied.joinToString(separator = ", ") { it.label }
        when{
            hasSelfPermission(context,maps.map { it.key }.toTypedArray()) -> {
                multiplePermissionExecute?.let { it() }
            }
            permissions.map { fragment.shouldShowRequestPermissionRationale(it) }.contains(false) -> {

                val fullDescription = "This lets you $labels. \n" +
                        "To enable this, click App Settings below and activate these permissions."
                showDialogAllowInSettings(fullDescription)
            }
            else -> {
                val fullDescription = "This lets you $labels. \n" +
                        "Please allow these permission to use this feature."
                showDialogMultiplePermissionDenied(fullDescription)
            }
        }
    }

    override fun runSinglePermission(permission: String, execute: () -> Unit) {
        singlePermissionExecute = execute
        this@PermissionFragmentHandler.permission = permission
        when{
            hasSelfPermission(context, arrayOf(permission)) -> {
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
        this@PermissionFragmentHandler.permission = permission
        when{
            hasSelfPermission(context, arrayOf(permission)) -> {
                execute()
            }
            else -> {
                requestSinglePermissionWithCallbackLauncher.launch(permission)
            }
        }
    }

    override fun runMultiplePermission(vararg permissions: String, execute: () -> Unit) {
        multiplePermissionExecute = execute
        this@PermissionFragmentHandler.permissions.clear()
        this@PermissionFragmentHandler.permissions.addAll(arrayOf(*permissions))
        when{
            hasSelfPermission(context, arrayOf(*permissions)) -> {
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
        this@PermissionFragmentHandler.permissions.clear()
        this@PermissionFragmentHandler.permissions.addAll(arrayOf(*permissions))
        when{
            hasSelfPermission(context, arrayOf(*permissions)) -> {
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

    private fun showDialogSinglePermissionDenied(fullDescription: String){
        AlertDialog.Builder(context)
            .setMessage(fullDescription)
            .setPositiveButton("Allow"){dialog,_->
                dialog.dismiss()
                requestSinglePermissionNoCallbackLauncher.launch(permission)
            }
            .setNegativeButton("Don't Allow"){dialog,_->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDialogMultiplePermissionDenied(fullDescription: String){
        AlertDialog.Builder(context)
            .setMessage(fullDescription)
            .setPositiveButton("Allow"){dialog,_->
                dialog.dismiss()
                requestMultiplePermissionNoCallbackLauncher.launch(permissions.toTypedArray())
            }
            .setNegativeButton("Don't Allow"){dialog,_->
                dialog.dismiss()
            }
            .show()
    }

    private fun showDialogAllowInSettings(fullDescription: String) {
        AlertDialog.Builder(context)
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
        val uri: Uri = Uri.fromParts("package", context.packageName, null)
        intent.data = uri
        context.startActivity(intent)
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