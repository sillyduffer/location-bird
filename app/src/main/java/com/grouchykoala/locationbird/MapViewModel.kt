package com.grouchykoala.locationbird

import android.location.Location
import androidx.lifecycle.ViewModel
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import java.math.BigDecimal
import java.math.RoundingMode

class MapViewModel: ViewModel() {
    var carLocation: CarLocation? = null
    var requestingLocationUpdates = true

    fun getLocationRequest(): LocationRequest {
        return LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 500
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
    }

    fun calculateDistanceAndUnits(currentLocation: Location): DistanceAndUnits? {
        return carLocation?.let {
            if (it.location == null) {
                it.location = locationFromCoordinates(it.coordinates)
            }
            val meters = currentLocation.distanceTo(it.location)
            val distance: BigDecimal
            val units: UnitType
            if (meters >= 152.4) {
                distance = BigDecimal(getMiles(meters))
                    .setScale(2, RoundingMode.HALF_EVEN)
                units = UnitType.MILES
            } else {
                distance = BigDecimal(getFeet(meters))
                    .setScale(2, RoundingMode.HALF_EVEN)
                units = UnitType.FEET
            }
            DistanceAndUnits(distance.toFloat(), units)
        }
    }

    fun locationFromCoordinates(coordinates: LatLng): Location {
        val l = Location("")
        l.latitude = coordinates.latitude
        l.longitude = coordinates.longitude
        return l
    }

    private fun getMiles(meters: Float): Double {
        return meters*0.0006213712
    }

    private fun getFeet(meters: Float): Double {
        return meters*3.280839895
    }
}

data class CarLocation(var marker: Marker?, var location: Location?, val coordinates: LatLng)
data class DistanceAndUnits(val distance: Float, val units: UnitType)

enum class UnitType {
    FEET,
    MILES
}