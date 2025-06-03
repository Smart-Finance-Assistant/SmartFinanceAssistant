package com.example.smartfinanceassistance.externalApp

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.smartfinanceassistance.R
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.InputStreamReader

class MapFragment : Fragment(R.layout.fragment_map), OnMapReadyCallback {

    private lateinit var map: GoogleMap
    private var currentLocation: Location? = null

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 100
        const val RADIUS_KM = 10.0  // 반경 10km
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        enableUserLocation()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE &&
            grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            enableUserLocation()
        }
    }

    private fun enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            map.isMyLocationEnabled = true

            val fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(requireActivity())
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    currentLocation = it
                    val userLatLng = LatLng(it.latitude, it.longitude)
                    map.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 13f))

                    showNearbyStations()
                }
            }
        }
    }

    private fun showNearbyStations() {
        lifecycleScope.launch {
            val stations = loadPoliceStations()
            val userLoc = currentLocation ?: return@launch

            for ((name, lat, lng) in stations) {
                val distance = distanceBetweenKm(userLoc.latitude, userLoc.longitude, lat, lng)
                if (distance <= RADIUS_KM) {
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title("$name (${"%.2f".format(distance)}km)")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    )
                }
            }
        }
    }

    private fun loadPoliceStations(): List<Triple<String, Double, Double>> {
        val result = mutableListOf<Triple<String, Double, Double>>()
        val inputStream = resources.openRawResource(R.raw.police_stations)
        val reader = BufferedReader(InputStreamReader(inputStream))

        reader.readLine() // skip header
        reader.forEachLine { rawLine ->
            val tokens = rawLine.split(",").map { it.trim() }.filter { it.isNotEmpty() }

            if (tokens.size >= 6) {
                val name = tokens[2]
                val lat = tokens[4].toDoubleOrNull()
                val lng = tokens[5].toDoubleOrNull()
                if (lat != null && lng != null) {
                    result.add(Triple(name, lat, lng))
                }
            }
        }

        return result
    }

    private fun distanceBetweenKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0] / 1000.0
    }
}