package com.grouchykoala.locationbird

import android.location.Location
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MapViewModel: ViewModel() {
    var carLocation: CarLocation? = null
    var requestingLocationUpdates = false

    fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 100
        }
    }

    fun calculateDistance(currentLocation: Location): Float? {
        return carLocation?.let {
            if (it.location == null) {
                it.location = locationFromCoordinates(it.coordinates)
            }
            currentLocation.distanceTo(it.location)
        }
    }

    fun locationFromCoordinates(coordinates: LatLng): Location {
        val l = Location("")
        l.latitude = coordinates.latitude
        l.longitude = coordinates.longitude
        return l
    }
}

data class CarLocation(var marker: Marker?, var location: Location?, val coordinates: LatLng)
