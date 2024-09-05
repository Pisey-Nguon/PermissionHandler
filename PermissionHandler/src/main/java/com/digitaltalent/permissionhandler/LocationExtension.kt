package com.digitaltalent.permissionhandler

import android.annotation.SuppressLint
import android.app.Activity
import android.location.Location
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.tasks.Task

@SuppressLint("MissingPermission")
private fun Activity.requestCurrentLocation(callback: (Location) -> Unit) {
    val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(this)

    val task: Task<Location> = fusedLocationClient.lastLocation
    task.addOnSuccessListener { location: Location? ->
        location?.let(callback)
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

