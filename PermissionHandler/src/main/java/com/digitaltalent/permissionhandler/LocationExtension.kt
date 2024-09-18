package com.digitaltalent.permissionhandler

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import android.os.Looper
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.Task

@SuppressLint("MissingPermission")
private fun Activity.requestCurrentLocation(callback: (Location) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(this)
    val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            val location = locationResult.lastLocation
            location?.let {
                location.let(callback)
                fusedLocationClient.removeLocationUpdates(this)
            }
        }
    }
    fun requestNewLocationData() {
        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
            .setWaitForAccurateLocation(false)
            .setMinUpdateIntervalMillis(5000)
            .setMaxUpdateDelayMillis(10000)
            .build()

        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }
    val task: Task<Location> = fusedLocationClient.lastLocation
    task.addOnSuccessListener { location: Location? ->
        if (location == null) {
            requestNewLocationData()
            return@addOnSuccessListener
        }
        location.let(callback)
    }
}
fun FragmentActivity.currentLocation(callback: (Location) -> Unit) {
    runWithLocationPermission {
        this.requestCurrentLocation(callback)
    }

}

fun Fragment.currentLocation(callback: (Location) -> Unit) {
    runWithLocationPermission {
        this.requireActivity().requestCurrentLocation(callback)
    }

}

