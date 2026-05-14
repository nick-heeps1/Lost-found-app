package com.example.lost_found_app.ui

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.lost_found_app.R
import com.example.lost_found_app.databinding.FragmentMapsBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LostFoundViewModel by activityViewModels()
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLocation: Location? = null
    private var radiusKm: Double = 10.0
    private var showAll: Boolean = false

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            enableMyLocation()
            fetchCurrentLocation()
        } else {
            Toast.makeText(requireContext(), "Location permission denied – showing all items", Toast.LENGTH_LONG).show()
            showAll = true
            updateMarkers()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_container) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.seekbarRadius.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                radiusKm = maxOf(1, progress).toDouble()
                if (!showAll) {
                    binding.tvRadiusLabel.text = "Show items within: ${radiusKm.toInt()} km"
                    updateMarkers()
                }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar) { showAll = false }
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })

        binding.btnShowAll.setOnClickListener {
            showAll = true
            binding.tvRadiusLabel.text = "Showing all items"
            updateMarkers()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap.setOnMarkerClickListener { marker ->
            marker.showInfoWindow()
            true
        }

        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            enableMyLocation()
            fetchCurrentLocation()
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        viewModel.allItems.observe(viewLifecycleOwner) {
            updateMarkers()
        }
    }

    @Suppress("MissingPermission")
    private fun enableMyLocation() {
        googleMap.isMyLocationEnabled = true
    }

    @Suppress("MissingPermission")
    private fun fetchCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLocation = location
                googleMap.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(LatLng(location.latitude, location.longitude), 12f)
                )
                updateMarkers()
            } else {
                Toast.makeText(requireContext(), "Could not get location. Ensure GPS is on.", Toast.LENGTH_SHORT).show()
                showAll = true
                updateMarkers()
            }
        }
    }

    private fun updateMarkers() {
        if (!::googleMap.isInitialized) return
        googleMap.clear()

        val items = viewModel.allItems.value ?: return

        val filtered = if (showAll || currentLocation == null) {
            items
        } else {
            items.filter { item ->
                if (item.latitude == 0.0 && item.longitude == 0.0) return@filter false
                val result = FloatArray(1)
                Location.distanceBetween(
                    currentLocation!!.latitude, currentLocation!!.longitude,
                    item.latitude, item.longitude,
                    result
                )
                (result[0] / 1000.0) <= radiusKm
            }
        }

        for (item in filtered) {
            if (item.latitude == 0.0 && item.longitude == 0.0) continue
            val color = if (item.postType == "Lost")
                BitmapDescriptorFactory.HUE_RED else BitmapDescriptorFactory.HUE_GREEN
            googleMap.addMarker(
                MarkerOptions()
                    .position(LatLng(item.latitude, item.longitude))
                    .title("${item.postType}: ${item.name}")
                    .snippet("${item.description}  |  ${item.date}")
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}