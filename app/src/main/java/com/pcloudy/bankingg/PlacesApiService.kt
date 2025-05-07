package com.pcloudy.bankingg

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import com.google.gson.GsonBuilder
import com.google.gson.FieldNamingPolicy

interface PlacesApiService {
    @GET("maps/api/place/nearbysearch/json")
    suspend fun searchNearbyATMs(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("type") type: String = "atm",
        @Query("key") apiKey: String = "AIzaSyCG1cz_ahaV9S-GiHXWnwa5ohUl2F6BWZc"
    ): PlacesApiResponse
}

// Response Data Classes
data class PlacesApiResponse(
    val status: String,
    val results: List<PlaceResult>
)

data class PlaceResult(
    val placeId: String,
    val name: String,
    val vicinity: String,
    val geometry: Geometry,
    val openingHours: OpeningHours?
)

data class Geometry(
    val location: Location
)

data class Location(
    val lat: Double,
    val lng: Double
)

data class OpeningHours(
    val openNow: Boolean
)