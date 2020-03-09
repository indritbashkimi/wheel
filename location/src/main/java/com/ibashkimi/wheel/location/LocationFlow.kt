package com.ibashkimi.wheel.location

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import com.google.android.gms.location.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

@SuppressLint("MissingPermission")
fun locationFlow(application: Application): Flow<Location> = callbackFlow {
    val locationProvider = LocationServices.getFusedLocationProviderClient(application)

    val locationCallback: LocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            offer(locationResult.lastLocation)
        }

        override fun onLocationAvailability(p0: LocationAvailability) {
            //Toast.makeText(context, "location availability $p0", Toast.LENGTH_SHORT).show()
        }
    }
    val locationRequest = LocationRequest()
    locationRequest.interval = 60000
    locationRequest.fastestInterval = 30000
    locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
    locationProvider.requestLocationUpdates(locationRequest, locationCallback, null)

    awaitClose {
        locationProvider.removeLocationUpdates(locationCallback)
    }
}