package com.digitaltalent.permissionhandlerexample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.digitaltalent.permissionhandler.PermissionCallback
import com.digitaltalent.permissionhandler.PermissionHandler
const val TAG = "MyMain"
class MainActivity : AppCompatActivity() {
    private lateinit var permissionHandler: PermissionHandler
    private val permissionCallback = object :PermissionCallback{
        override fun permissionsDenied(permissions: List<String>) {

        }

        override fun permissionsGranted(permissions: List<String>) {
        }

        override fun shouldShowRequestPermissionRationale(permissions: List<String>) {
        }

    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        permissionHandler = PermissionHandler(this)
        findViewById<Button>(R.id.btnRequestPermission).setOnClickListener {
            permissionHandler.runWithPermissions(
                android.Manifest.permission.CAMERA,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                callBack = permissionCallback
            ){
                Log.d(TAG, "onCreate: all permission granted")
            }
        }
    }
}