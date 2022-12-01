package com.digitaltalent.permissionhandler

import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat


class PermissionHandler(private val activity: AppCompatActivity){
    private var callBack:PermissionCallback? = null
    private var execute:(()-> Unit)? = null
    private var permissions = ArrayList<String>()
    private val requestPermissionsLauncher = activity.registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()){ maps->
        val permissionsDenied = ArrayList<String>()
        val permissionsGranted = ArrayList<String>()
        maps.forEach {
            if (it.value){
                permissionsGranted.add(it.key)
            }else{
                permissionsDenied.add(it.key)
            }

        }

        if (permissionsDenied.isNotEmpty()){
            callBack?.permissionsDenied(permissionsDenied)
        }
        if (permissionsGranted.isNotEmpty()){
            callBack?.permissionsGranted(permissionsGranted)
        }
        if (permissionsGranted.size == permissions.size){
            execute?.let { it() }
        }
    }
    fun runWithPermissions(vararg permissions:String, callBack:PermissionCallback, execute:()->Unit){
        this@PermissionHandler.permissions.clear()
        this@PermissionHandler.permissions.addAll(arrayOf(*permissions))
        when{
            hasSelfPermission(activity, arrayOf(*permissions)) -> {
                execute()
            }
            permissions.any { activity.shouldShowRequestPermissionRationale(it) } ->{
                val list = permissions.filter { activity.shouldShowRequestPermissionRationale(it) }
                callBack.shouldShowRequestPermissionRationale(list)
            }

            else -> {
                requestPermissionsLauncher.launch(arrayOf(*permissions))
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

}