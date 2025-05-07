package com.pcloudy.bankingg

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.model.TypeFilter
import com.google.android.libraries.places.api.net.FetchPlaceRequest
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
import com.google.android.libraries.places.api.net.PlacesClient

class AtmFragment : Fragment() {
    private lateinit var placesClient: PlacesClient
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var zipCodeEditText: EditText
    private lateinit var searchButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var atmAdapter: ATMAdapter

    companion object {
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val API_KEY = "AIzaSyCwnVGa_ECbXENR35uiJ5edhKd_lyw7Qck"
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout
        val view = inflater.inflate(R.layout.fragment_atm, container, false)

        // Initialize Places API
        if (!Places.isInitialized()) {
            Places.initialize(requireContext().applicationContext, API_KEY)
        }
        placesClient = Places.createClient(requireContext())

        // Initialize Fused Location Provider
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Initialize views
        zipCodeEditText = view.findViewById(R.id.zipCodeEditText)
        searchButton = view.findViewById(R.id.searchButton)
        recyclerView = view.findViewById(R.id.recyclerViewAtms)

        // Setup RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        atmAdapter = ATMAdapter()
        recyclerView.adapter = atmAdapter

        // Set up search button click listener
        searchButton.setOnClickListener {
            val zipCode = zipCodeEditText.text.toString()
            if (zipCode.isNotEmpty()) {
                // Clear previous results
                atmAdapter.updateAtms(emptyList())
                searchAtmsByZipCode(zipCode)
            } else {
                Toast.makeText(context, "Please enter a zip code", Toast.LENGTH_SHORT).show()
            }
        }

        return view
    }

    private fun searchAtmsByZipCode(zipCode: String) {
        // Validate zip code format
        if (!zipCode.matches("\\d{6}".toRegex())) {
            Toast.makeText(context, "Invalid zip code format", Toast.LENGTH_SHORT).show()
            return
        }

        // Check location permissions
        if (checkLocationPermission()) {
            findAtmsNearLocation(zipCode)
        }
    }

    private fun displayAtmResults(atmList: List<String>) {
        atmAdapter.updateAtms(atmList)
    }

    private fun findAtmsNearLocation(zipCode: String) {
        try {
            val placeFields = listOf(
                Place.Field.NAME,
                Place.Field.ADDRESS,
                Place.Field.LAT_LNG,
                Place.Field.TYPES,
                Place.Field.BUSINESS_STATUS
            )

            val request = FindAutocompletePredictionsRequest.builder()
                .setTypeFilter(TypeFilter.ESTABLISHMENT)
                .setQuery("ATM in $zipCode")
                .build()

            // Show loading
            val atmList = mutableListOf<String>()

            placesClient.findAutocompletePredictions(request)
                .addOnSuccessListener { response ->
                    response.autocompletePredictions.forEach { prediction ->
                        val placeRequest = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
                        placesClient.fetchPlace(placeRequest)
                            .addOnSuccessListener { fetchResponse ->
                                val place = fetchResponse.place
                                if (place.types?.contains(Place.Type.ATM) == true) {
                                    val atmInfo = """
                                    Bank Name: ${place.name ?: "ATM"}
                                    Location: ${place.address ?: "Address not available"}
                                    Coordinates: ${place.latLng?.latitude}, ${place.latLng?.longitude}
                                    Status: ${if (place.businessStatus?.name == "OPERATIONAL") "Open" else "Closed/Unknown"}
                                    Distance: Within 5km radius
                                """.trimIndent()
                                    atmList.add(atmInfo)
                                    // Update RecyclerView
//                                    displayAtmResults(atmList)
                                    if (atmList.isNotEmpty()) {
                                        displayAtmResults(atmList)
                                    } else {
                                        Toast.makeText(context, "No ATMs found near $zipCode", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                            .addOnFailureListener { exception ->
                                Log.e("ATM", "Error fetching ATM details: ${exception.message}")
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    when (exception) {
                        is ApiException -> {
                            Log.e("ATM", "Place API Error: ${exception.statusCode} - ${exception.message}")
                            Toast.makeText(
                                context,
                                "Error searching ATMs: ${exception.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        else -> {
                            Log.e("ATM", "Unexpected error: ${exception.message}")
                            Toast.makeText(
                                context,
                                "Unexpected error occurred",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

        } catch (e: Exception) {
            Log.e("ATM", "Error: ${e.message}", e)
            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkLocationPermission(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if not granted
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
            )
            return false
        }
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                if ((grantResults.isNotEmpty() &&
                            grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted
                    val zipCode = zipCodeEditText.text.toString()
                    if (zipCode.isNotEmpty()) {
                        searchAtmsByZipCode(zipCode)
                    }
                } else {
                    // Permission denied
                    Toast.makeText(
                        context,
                        "Location permission is required to find ATMs",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                return
            }
        }
    }
}

//package com.pcloudy.bankingg
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
//import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.Place
//import com.google.android.libraries.places.api.model.TypeFilter
//import com.google.android.libraries.places.api.net.FetchPlaceRequest
//import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
//import com.google.android.libraries.places.api.net.PlacesClient
//
//class AtmFragment : Fragment() {
//    private lateinit var placesClient: PlacesClient
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var searchButton: Button
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var atmAdapter: ATMAdapter
//
//    companion object {
//        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
//        private const val API_KEY = "AIzaSyCwnVGa_ECbXENR35uiJ5edhKd_lyw7Qck"
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_atm, container, false)
//
//        // Initialize Places API
//        if (!Places.isInitialized()) {
//            Places.initialize(requireContext().applicationContext, API_KEY)
//        }
//        placesClient = Places.createClient(requireContext())
//
//        // Initialize Fused Location Provider
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
//
//        // Initialize views
//        searchButton = view.findViewById(R.id.searchButton)
//        recyclerView = view.findViewById(R.id.recyclerViewAtms)
//
//        // Setup RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        atmAdapter = ATMAdapter()
//        recyclerView.adapter = atmAdapter
//
//        // Set up search button click listener
//        searchButton.setOnClickListener {
//            if (checkLocationPermission()) {
//                getCurrentLocationAndSearchAtms()
//            }
//        }
//
//        return view
//    }
//
//    private fun getCurrentLocationAndSearchAtms() {
//        try {
//            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
//                .addOnSuccessListener { location ->
//                    if (location != null) {
//                        findNearbyAtmsAndBanks(location.latitude, location.longitude)
//                    } else {
//                        Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        } catch (e: SecurityException) {
//            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun findNearbyAtmsAndBanks(latitude: Double, longitude: Double) {
//        try {
//            val placeFields = listOf(
//                Place.Field.NAME,
//                Place.Field.ADDRESS,
//                Place.Field.LAT_LNG,
//                Place.Field.TYPES,
//                Place.Field.BUSINESS_STATUS
//            )
//
//            val request = FindAutocompletePredictionsRequest.builder()
//                .setTypeFilter(TypeFilter.ESTABLISHMENT)
//                .setQuery("ATM bank near $latitude,$longitude")
//                .build()
//
//            val atmList = mutableListOf<String>()
//
//            placesClient.findAutocompletePredictions(request)
//                .addOnSuccessListener { response ->
//                    response.autocompletePredictions.forEach { prediction ->
//                        val placeRequest = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
//                        placesClient.fetchPlace(placeRequest)
//                            .addOnSuccessListener { fetchResponse ->
//                                val place = fetchResponse.place
//                                if (place.types?.contains(Place.Type.ATM) == true ||
//                                    place.types?.contains(Place.Type.BANK) == true) {
//
//                                    val atmInfo = """
//                                    Name: ${place.name ?: "ATM/Bank"}
//                                    Location: ${place.address ?: "Address not available"}
//                                    Type: ${if (place.types?.contains(Place.Type.ATM) == true) "ATM" else "Bank"}
//                                    Status: ${if (place.businessStatus?.name == "OPERATIONAL") "Open" else "Closed/Unknown"}
//                                    """.trimIndent()
//                                    atmList.add(atmInfo)
//
//                                    if (atmList.isNotEmpty()) {
//                                        displayAtmResults(atmList)
//                                    }
//                                }
//                            }
//                            .addOnFailureListener { exception ->
//                                Log.e("ATM", "Error fetching ATM details: ${exception.message}")
//                            }
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    when (exception) {
//                        is ApiException -> {
//                            Log.e("ATM", "Place API Error: ${exception.statusCode} - ${exception.message}")
//                            Toast.makeText(
//                                context,
//                                "Error searching nearby ATMs/Banks: ${exception.message}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                        else -> {
//                            Log.e("ATM", "Unexpected error: ${exception.message}")
//                            Toast.makeText(
//                                context,
//                                "Unexpected error occurred",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                }
//
//        } catch (e: Exception) {
//            Log.e("ATM", "Error: ${e.message}", e)
//            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun displayAtmResults(atmList: List<String>) {
//        atmAdapter.updateAtms(atmList)
//    }
//
//    private fun checkLocationPermission(): Boolean {
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissions(
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
//            )
//            return false
//        }
//        return true
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
//                if ((grantResults.isNotEmpty() &&
//                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                ) {
//                    getCurrentLocationAndSearchAtms()
//                } else {
//                    Toast.makeText(
//                        context,
//                        "Location permission is required to find nearby ATMs/Banks",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                return
//            }
//        }
//    }
//}

//package com.pcloudy.bankingg
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.location.Geocoder
//import android.location.Location
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.Place
//import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest
//import com.google.android.libraries.places.api.net.FetchPlaceRequest
//import com.google.android.libraries.places.api.net.PlacesClient
//import java.util.Locale
//import kotlin.math.roundToInt
//
//class AtmFragment : Fragment() {
//    private lateinit var placesClient: PlacesClient
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var atmAdapter: ATMAdapter
//    private val HSR_4TH_SECTOR_LOCATION = LatLng(12.9137, 77.6413) // HSR Layout 4th Sector coordinates
//
//    companion object {
//        private const val API_KEY = "AIzaSyCwnVGa_ECbXENR35uiJ5edhKd_lyw7Qck"
//        private const val RADIUS_IN_METERS = 1000 // 1 km radius
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_atm, container, false)
//
//        // Initialize Places API
//        if (!Places.isInitialized()) {
//            Places.initialize(requireContext().applicationContext, API_KEY)
//        }
//        placesClient = Places.createClient(requireContext())
//
//        // Initialize views
//        recyclerView = view.findViewById(R.id.recyclerViewAtms)
//
//        // Setup RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        atmAdapter = ATMAdapter()
//        recyclerView.adapter = atmAdapter
//
//        // Start search immediately
//        findHsrAtms()
//
//        return view
//    }
//
//    private fun findHsrAtms() {
//        try {
//            val query = "ATM in HSR Layout 4th Sector, Bangalore"
//            val request = FindAutocompletePredictionsRequest.builder()
//                .setQuery(query)
//                .setCountries("IN")
//                .build()
//
//            Log.d("ATM_DEBUG", "Searching for ATMs in HSR Layout 4th Sector")
//
//            placesClient.findAutocompletePredictions(request)
//                .addOnSuccessListener { response ->
//                    val atmList = mutableListOf<String>()
//                    val placeFields = listOf(
//                        Place.Field.NAME,
//                        Place.Field.ADDRESS,
//                        Place.Field.LAT_LNG,
//                        Place.Field.TYPES,
//                        Place.Field.BUSINESS_STATUS
//                    )
//
//                    response.autocompletePredictions.forEach { prediction ->
//                        val placeRequest = FetchPlaceRequest.newInstance(prediction.placeId, placeFields)
//                        placesClient.fetchPlace(placeRequest)
//                            .addOnSuccessListener { fetchResponse ->
//                                val place = fetchResponse.place
//
//                                val isAtm = place.types?.contains(Place.Type.ATM) == true
//                                val isBank = place.types?.contains(Place.Type.BANK) == true
//
//                                if ((isAtm || isBank) && place.latLng != null) {
//                                    val distance = calculateDistance(
//                                        HSR_4TH_SECTOR_LOCATION.latitude,
//                                        HSR_4TH_SECTOR_LOCATION.longitude,
//                                        place.latLng!!.latitude,
//                                        place.latLng!!.longitude
//                                    )
//
//                                    // Only include if it's within the target area
//                                    if (distance <= RADIUS_IN_METERS) {
//                                        val type = if (isAtm) "ATM" else "Bank"
//                                        val atmInfo = """
//                                            Name: ${place.name ?: type}
//                                            Type: $type
//                                            Location: ${place.address ?: "Address not available"}
//                                            Distance: ${distance.roundToInt()} meters from HSR 4th Sector center
//                                            Status: ${if (place.businessStatus?.name == "OPERATIONAL") "Open" else "Closed/Unknown"}
//                                        """.trimIndent()
//                                        atmList.add(atmInfo)
//
//                                        // Update UI immediately when we find an ATM
//                                        if (atmList.isNotEmpty()) {
//                                            val sortedAtms = atmList.sortedBy {
//                                                it.substringAfter("Distance: ").substringBefore(" meters").toInt()
//                                            }
//                                            displayAtmResults(sortedAtms)
//
//                                            // Show total count
//                                            Toast.makeText(
//                                                context,
//                                                "Found ${atmList.size} ATMs/Banks in HSR Layout 4th Sector",
//                                                Toast.LENGTH_SHORT
//                                            ).show()
//                                        }
//                                    }
//                                }
//                            }
//                            .addOnFailureListener { exception ->
//                                Log.e("ATM_DEBUG", "Error fetching place: ${exception.message}")
//                            }
//                    }
//
//                    if (response.autocompletePredictions.isEmpty()) {
//                        Toast.makeText(
//                            context,
//                            "No ATMs found in HSR Layout 4th Sector",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    Log.e("ATM_DEBUG", "Error searching for ATMs: ${exception.message}")
//                    Toast.makeText(
//                        context,
//                        "Error searching for ATMs in HSR Layout",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//        } catch (e: Exception) {
//            Log.e("ATM_DEBUG", "Error in findHsrAtms: ${e.message}", e)
//            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun calculateDistance(
//        lat1: Double,
//        lon1: Double,
//        lat2: Double,
//        lon2: Double
//    ): Float {
//        val results = FloatArray(1)
//        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
//        return results[0]
//    }
//
//    private fun displayAtmResults(atmList: List<String>) {
//        atmAdapter.updateAtms(atmList)
//    }
//}

//package com.pcloudy.bankingg
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.location.Geocoder
//import android.location.Location
//import android.os.Bundle
//import android.util.Log
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.Button
//import android.widget.TextView
//import android.widget.Toast
//import androidx.core.app.ActivityCompat
//import androidx.fragment.app.Fragment
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import com.google.android.gms.common.api.ApiException
//import com.google.android.gms.location.FusedLocationProviderClient
//import com.google.android.gms.location.LocationServices
//import com.google.android.gms.location.Priority
//import com.google.android.gms.maps.model.LatLng
//import com.google.android.libraries.places.api.Places
//import com.google.android.libraries.places.api.model.Place
//import com.google.android.libraries.places.api.model.RectangularBounds
//import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest
//import com.google.android.libraries.places.api.net.PlacesClient
//import java.util.Locale
//import kotlin.math.roundToInt
//
//class AtmFragment : Fragment() {
//    private lateinit var placesClient: PlacesClient
//    private lateinit var fusedLocationClient: FusedLocationProviderClient
//    private lateinit var searchButton: Button
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var currentLocationText: TextView
//    private lateinit var atmAdapter: ATMAdapter
//    private var currentLocation: Location? = null
//
//    companion object {
//        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
//        private const val API_KEY = "AIzaSyCwnVGa_ECbXENR35uiJ5edhKd_lyw7Qck"
//        private const val RADIUS_IN_METERS = 1000000000 // 1 km
//    }
//
//    override fun onCreateView(
//        inflater: LayoutInflater,
//        container: ViewGroup?,
//        savedInstanceState: Bundle?
//    ): View? {
//        val view = inflater.inflate(R.layout.fragment_atm, container, false)
//
//        // Initialize Places API
//        if (!Places.isInitialized()) {
//            Places.initialize(requireContext().applicationContext, API_KEY)
//        }
//        placesClient = Places.createClient(requireContext())
//
//        // Initialize Fused Location Provider
//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
//
//        // Initialize views
//        searchButton = view.findViewById(R.id.searchButton)
//        recyclerView = view.findViewById(R.id.recyclerViewAtms)
//        currentLocationText = view.findViewById(R.id.currentLocationText)
//
//        // Setup RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(context)
//        atmAdapter = ATMAdapter()
//        recyclerView.adapter = atmAdapter
//
//        // Set up search button click listener
//        searchButton.setOnClickListener {
//            if (checkLocationPermission()) {
//                getCurrentLocationAndSearch()
//            }
//        }
//
//        return view
//    }
//
//    private fun getCurrentLocationAndSearch() {
//        try {
//            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
//                .addOnSuccessListener { location ->
//                    if (location != null) {
//                        currentLocation = location
//                        displayCurrentLocation(location)
//                        findNearbyAtms()
//                    } else {
//                        Toast.makeText(context, "Unable to get current location", Toast.LENGTH_SHORT).show()
//                    }
//                }
//                .addOnFailureListener { e ->
//                    Toast.makeText(context, "Error getting location: ${e.message}", Toast.LENGTH_SHORT).show()
//                }
//        } catch (e: SecurityException) {
//            Toast.makeText(context, "Location permission required", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun displayCurrentLocation(location: Location) {
//        try {
//            val geocoder = Geocoder(requireContext(), Locale.getDefault())
//            val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
//            val address = addresses?.firstOrNull()
//
//            val locationInfo = """
//                Current Location:
//                Latitude: ${location.latitude}
//                Longitude: ${location.longitude}
//                Address: ${address?.getAddressLine(0) ?: "Address not available"}
//            """.trimIndent()
//
//            currentLocationText.text = locationInfo
//        } catch (e: Exception) {
//            Log.e("ATM", "Error getting address: ${e.message}")
//            val locationInfo = """
//                Current Location:
//                Latitude: ${location.latitude}
//                Longitude: ${location.longitude}
//            """.trimIndent()
//            currentLocationText.text = locationInfo
//        }
//    }
//
//    private fun findNearbyAtms() {
//        try {
//            // Define the place fields to get
//            val placeFields = listOf(
//                Place.Field.NAME,
//                Place.Field.ADDRESS,
//                Place.Field.LAT_LNG,
//                Place.Field.TYPES,
//                Place.Field.BUSINESS_STATUS
//            )
//
//            val request = FindCurrentPlaceRequest.newInstance(placeFields)
//
//            if (ActivityCompat.checkSelfPermission(
//                    requireContext(),
//                    Manifest.permission.ACCESS_FINE_LOCATION
//                ) != PackageManager.PERMISSION_GRANTED) {
//                return
//            }
//
//            placesClient.findCurrentPlace(request)
//                .addOnSuccessListener { response ->
//                    val atmList = mutableListOf<String>()
//
//                    response.placeLikelihoods.forEach { placeLikelihood ->
//                        val place = placeLikelihood.place
//                        val isAtm = place.types?.contains(Place.Type.ATM) == true
//                        val isBank = place.types?.contains(Place.Type.BANK) == true
//
//                        if (isAtm || isBank) {
//                            currentLocation?.let { currentLoc ->
//                                val distance = place.latLng?.let { latLng ->
//                                    calculateDistance(
//                                        currentLoc.latitude,
//                                        currentLoc.longitude,
//                                        latLng.latitude,
//                                        latLng.longitude
//                                    )
//                                } ?: 0f
//
//                                if (distance <= RADIUS_IN_METERS) {
//                                    val type = when {
//                                        isAtm -> "ATM"
//                                        isBank -> "Bank"
//                                        else -> "ATM/Bank"
//                                    }
//
//                                    val atmInfo = """
//                                        Name: ${place.name ?: type}
//                                        Type: $type
//                                        Location: ${place.address ?: "Address not available"}
//                                        Distance: ${distance.roundToInt()} meters
//                                        Status: ${if (place.businessStatus?.name == "OPERATIONAL") "Open" else "Closed/Unknown"}
//                                    """.trimIndent()
//                                    atmList.add(atmInfo)
//                                }
//                            }
//                        }
//                    }
//
//                    if (atmList.isNotEmpty()) {
//                        val sortedAtms = atmList.sortedBy {
//                            it.substringAfter("Distance: ").substringBefore(" meters").toInt()
//                        }
//                        displayAtmResults(sortedAtms)
//                        Toast.makeText(
//                            context,
//                            "Found ${atmList.size} ATMs/Banks within 1km",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    } else {
//                        Toast.makeText(
//                            context,
//                            "No ATMs found within 1km of your location",
//                            Toast.LENGTH_SHORT
//                        ).show()
//                    }
//                }
//                .addOnFailureListener { exception ->
//                    when (exception) {
//                        is ApiException -> {
//                            Log.e("ATM", "Place API Error: ${exception.statusCode} - ${exception.message}")
//                            Toast.makeText(
//                                context,
//                                "Error searching ATMs: ${exception.message}",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                        else -> {
//                            Log.e("ATM", "Error: ${exception.message}")
//                            Toast.makeText(
//                                context,
//                                "Error searching for nearby ATMs",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    }
//                }
//        } catch (e: Exception) {
//            Log.e("ATM", "Error: ${e.message}", e)
//            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun calculateDistance(
//        lat1: Double,
//        lon1: Double,
//        lat2: Double,
//        lon2: Double
//    ): Float {
//        val results = FloatArray(1)
//        Location.distanceBetween(lat1, lon1, lat2, lon2, results)
//        return results[0]
//    }
//
//    private fun displayAtmResults(atmList: List<String>) {
//        atmAdapter.updateAtms(atmList)
//    }
//
//    private fun checkLocationPermission(): Boolean {
//        if (ActivityCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.ACCESS_FINE_LOCATION
//            ) != PackageManager.PERMISSION_GRANTED
//        ) {
//            requestPermissions(
//                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
//                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION
//            )
//            return false
//        }
//        return true
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
//                if ((grantResults.isNotEmpty() &&
//                            grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                ) {
//                    getCurrentLocationAndSearch()
//                } else {
//                    Toast.makeText(
//                        context,
//                        "Location permission is required to find nearby ATMs",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                }
//                return
//            }
//        }
//    }
//}