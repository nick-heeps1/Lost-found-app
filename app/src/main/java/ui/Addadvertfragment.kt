package com.example.lost_found_app.ui

import android.Manifest
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.lost_found_app.databinding.FragmentAddAdvertBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import java.util.*
import com.example.lost_found_app.R

class AddAdvertFragment : Fragment() {

    private var _binding: FragmentAddAdvertBinding? = null
    private val binding get() = _binding!!

    private val viewModel: LostFoundViewModel by activityViewModels()

    private var selectedImageUri: Uri? = null

    // ── Task 9.1 additions ────────────────────────────────────────────────
    private var selectedLatitude: Double = 0.0
    private var selectedLongitude: Double = 0.0
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val autocompleteLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val place = Autocomplete.getPlaceFromIntent(result.data!!)
            binding.etLocation.setText(place.name ?: place.address ?: "")
            selectedLatitude = place.latLng?.latitude ?: 0.0
            selectedLongitude = place.latLng?.longitude ?: 0.0
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) fetchCurrentLocation()
        else Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show()
    }
    // ── End Task 9.1 additions ────────────────────────────────────────────

    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            selectedImageUri = result.data?.data
            binding.ivImagePreview.setImageURI(selectedImageUri)
            binding.ivImagePreview.visibility = View.VISIBLE
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddAdvertBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Task 9.1 – initialise geo clients
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        if (!Places.isInitialized()) {
            Places.initialize(requireContext().applicationContext, getString(R.string.maps_api_key))
        }

        setupCategorySpinner()
        setupDatePicker()
        setupImagePicker()
        setupSaveButton()

        // Task 9.1 – wire up location field and button
        setupLocationField()
        setupGetCurrentLocationButton()
    }


    private fun setupCategorySpinner() {
        val categories = resources.getStringArray(R.array.categories)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, categories)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }


    private fun setupDatePicker() {
        binding.etDate.setOnClickListener {
            val calendar = Calendar.getInstance()
            DatePickerDialog(
                requireContext(),
                { _, year, month, day ->
                    binding.etDate.setText(String.format("%04d-%02d-%02d", year, month + 1, day))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun setupImagePicker() {
        binding.btnPickImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
                type = "image/*"
            }
            imagePickerLauncher.launch(intent)
        }
    }

    private fun setupSaveButton() {
        binding.btnSave.setOnClickListener {
            val postType = if (binding.rbLost.isChecked) "Lost" else "Found"

            val name = binding.etName.text.toString().trim()
            val phone = binding.etPhone.text.toString().trim()
            val description = binding.etDescription.text.toString().trim()
            val date = binding.etDate.text.toString().trim()
            val location = binding.etLocation.text.toString().trim()
            val category = binding.spinnerCategory.selectedItem.toString()

            if (name.isEmpty() || date.isEmpty()) {
                Toast.makeText(requireContext(), "Name and Date are required", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (description.isEmpty()) {
                Toast.makeText(requireContext(), "Please add a description", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val item = LostFoundItem(
                postType = postType,
                name = name,
                phone = phone,
                description = description,
                date = date,
                location = location,
                category = category,
                imagePath = selectedImageUri?.toString(),
                latitude = selectedLatitude,    // Task 9.1
                longitude = selectedLongitude   // Task 9.1
            )

            viewModel.insert(item)

            Toast.makeText(requireContext(), "Advert posted!", Toast.LENGTH_SHORT).show()

            findNavController().navigateUp()
        }
    }

    // ── Task 9.1 – new functions (no existing code changed) ───────────────

    private fun setupLocationField() {
        val fields = listOf(Place.Field.NAME, Place.Field.ADDRESS, Place.Field.LAT_LNG)
        binding.etLocation.setOnClickListener {
            val intent = Autocomplete
                .IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields)
                .build(requireContext())
            autocompleteLauncher.launch(intent)
        }
    }

    private fun setupGetCurrentLocationButton() {
        binding.btnGetCurrentLocation.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                fetchCurrentLocation()
            } else {
                locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    private fun fetchCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                selectedLatitude = location.latitude
                selectedLongitude = location.longitude
                try {
                    @Suppress("DEPRECATION")
                    val addresses = Geocoder(requireContext(), Locale.getDefault())
                        .getFromLocation(location.latitude, location.longitude, 1)
                    val readable = addresses?.firstOrNull()?.let { addr ->
                        listOfNotNull(addr.thoroughfare, addr.locality, addr.adminArea)
                            .joinToString(", ")
                    }
                    binding.etLocation.setText(
                        readable?.ifEmpty { "${location.latitude}, ${location.longitude}" }
                            ?: "${location.latitude}, ${location.longitude}"
                    )
                } catch (e: Exception) {
                    binding.etLocation.setText("${location.latitude}, ${location.longitude}")
                }
            } else {
                Toast.makeText(requireContext(), "Could not get location. Try again.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ── End Task 9.1 ──────────────────────────────────────────────────────

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}