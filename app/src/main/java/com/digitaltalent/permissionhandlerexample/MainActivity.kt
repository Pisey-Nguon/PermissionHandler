package com.digitaltalent.permissionhandlerexample

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.digitaltalent.permissionhandler.PermissionHandler
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices


const val TAG = "MyMain"
class MainActivity : AppCompatActivity() {
    private var permissionHandler = PermissionHandler(this)
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        findViewById<Button>(R.id.btnRequestMultiplePermission).setOnClickListener {
            runMultiplePermission()
        }
        findViewById<Button>(R.id.btnRequestSinglePermission).setOnClickListener {
            runSinglePermission()
        }
    }


    @SuppressLint("MissingPermission")
    private fun runMultiplePermission() {
        permissionHandler.runMultiplePermission(
            android.Manifest.permission.ACCESS_FINE_LOCATION,
            android.Manifest.permission.ACCESS_COARSE_LOCATION,
        ){
            Log.d(TAG, "runMultiplePermission: Permissions allowed")
            Toast.makeText(this, "Location Permission allowed", Toast.LENGTH_LONG).show()
        }
    }

    private fun runSinglePermission(){
        permissionHandler.runSinglePermission(android.Manifest.permission.CAMERA){
            Log.d(TAG, "runMultiplePermission: Permission allowed")
            Toast.makeText(this, "Camera Permission allowed", Toast.LENGTH_LONG).show()
        }
    }
}