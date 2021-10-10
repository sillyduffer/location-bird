package com.grouchykoala.locationbird

import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MapViewModel: ViewModel() {
    var carLocation: CarLocation? = null
    var requestingLocationUpdates = false

    fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 100
            fastestInterval = 50
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
            maxWaitTime= 100
        }
    }
}

data class CarLocation(var marker: Marker?, val coordinates: LatLng)
