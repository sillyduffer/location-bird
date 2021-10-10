package com.grouchykoala.locationbird

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.grouchykoala.locationbird.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity() {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private val model: MapViewModel by viewModels()
    private val locationPermissionRequest = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions.getOrDefault(Manifest.permission.ACCESS_FINE_LOCATION, false)
            || permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)
        ) {
            model.requestingLocationUpdates = true
            enableUserLocation()
        } else {
            model.requestingLocationUpdates = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync { googleMap ->
            mMap = googleMap
            enableUserLocation()

            model.carLocation?.let {
                it.marker?.remove()
                val coordinates = LatLng(it.coordinates.latitude, it.coordinates.longitude)
                it.marker = mMap.addMarker(MarkerOptions().position(coordinates))
            } ?: kotlin.run {
                val prefs = getPreferences(MODE_PRIVATE)
                if (prefs.contains(LAT_KEY) && prefs.contains(LNG_KEY)) {
                    val coordinates = LatLng(
                        Double.fromBits(prefs.getLong(LAT_KEY, 0)),
                        Double.fromBits(prefs.getLong(LNG_KEY, 0))
                    )
                    val marker = mMap.addMarker(MarkerOptions().position(coordinates))

                    model.carLocation = CarLocation(marker, model.locationFromCoordinates(coordinates), coordinates)
                }
            }

            binding.markerDropButton.setOnClickListener {
                dropPinAtCurrentLocation()
            }
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                binding.distanceIndicator.text = locationResult ?.let { result ->
                    val distance = model.calculateDistance(result.lastLocation)
                    distance?.let {
                        resources.getQuantityString(R.plurals.distance_from_pin, it.toInt())
                    } ?: resources.getString(R.string.distance_placeholder)
                } ?: resources.getString(R.string.distance_error_message)
            }
        }

    }

    override fun onResume() {
        super.onResume()
        if (model.requestingLocationUpdates) startLocationUpdates()
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            return
        }
        mMap.isMyLocationEnabled = true
    }

    private fun dropPinAtCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                location?.let {
                    model.carLocation?.marker?.remove()
                    val coordinates = LatLng(it.latitude, it.longitude)
                    val marker = mMap.addMarker(MarkerOptions().position(coordinates))

                    model.carLocation = CarLocation(marker, it, coordinates)

                    val editor = getPreferences(MODE_PRIVATE).edit()
                    editor.putLong(LAT_KEY, coordinates.latitude.toRawBits())
                    editor.putLong(LNG_KEY, coordinates.longitude.toRawBits())
                    editor.apply()
                }
            }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionRequest.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ))
            return
        }
        fusedLocationClient.requestLocationUpdates(
            model.getLocationRequest(),
            locationCallback,
            Looper.getMainLooper()
        )
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    companion object {
        const val LAT_KEY = "latitude key"
        const val LNG_KEY = "longitude key"
    }
}