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
import androidx.navigation.fragment.findNavController
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

        // 버튼 이벤트 설정
        setupButtonListeners(view)

        val mapFragment = childFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun setupButtonListeners(view: View) {
        // 뒤로가기 버튼
        view.findViewById<View>(R.id.btn_back).setOnClickListener {
            // Navigation을 사용하거나 Activity를 종료
            try {
                findNavController().navigateUp()
            } catch (e: Exception) {
                requireActivity().finish()
            }
        }

        // 현재 위치 버튼
        view.findViewById<View>(R.id.btn_my_location).setOnClickListener {
            goToCurrentLocation()
        }
    }

    private fun goToCurrentLocation() {
        currentLocation?.let { location ->
            val userLatLng = LatLng(location.latitude, location.longitude)
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
        } ?: run {
            // 현재 위치를 다시 가져오기
            enableUserLocation()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        // 지도 스타일 설정 (두 번째 사진처럼 깔끔하게)
        map.uiSettings.apply {
            isZoomControlsEnabled = false
            isMyLocationButtonEnabled = false // 커스텀 버튼 사용
            isCompassEnabled = true
        }

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
                    // 두 번째 사진처럼 검은색 경찰서 아이콘 사용
                    map.addMarker(
                        MarkerOptions()
                            .position(LatLng(lat, lng))
                            .title("$name (${"%.2f".format(distance)}km)")
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                    )
                }
            }

            Log.d("MapFragment", "총 ${stations.size}개 경찰서 중 반경 ${RADIUS_KM}km 내 표시")
        }
    }

    private fun loadPoliceStations(): List<Triple<String, Double, Double>> {
        val result = mutableListOf<Triple<String, Double, Double>>()

        try {
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
        } catch (e: Exception) {
            Log.e("MapFragment", "경찰서 데이터 로드 실패: ${e.message}")
            // 기본 더미 데이터 추가 (테스트용)
            result.addAll(getDummyPoliceStations())
        }

        return result
    }

    // 테스트용 더미 데이터
    private fun getDummyPoliceStations(): List<Triple<String, Double, Double>> {
        return listOf(
            Triple("종로경찰서", 37.5735, 126.9788),
            Triple("중구경찰서", 37.5636, 126.9975),
            Triple("용산경찰서", 37.5326, 126.9909),
            Triple("성동경찰서", 37.5506, 127.0417)
        )
    }

    private fun distanceBetweenKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lon1, lat2, lon2, result)
        return result[0] / 1000.0
    }
}