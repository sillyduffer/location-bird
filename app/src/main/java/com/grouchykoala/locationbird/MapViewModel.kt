package com.grouchykoala.locationbird

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker

class MapViewModel: ViewModel() {
    var carLocation: CarLocation? = null
}

data class CarLocation(var marker: Marker?, val coordinates: LatLng)
