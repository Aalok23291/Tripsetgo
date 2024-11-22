package com.example.tripsetgo

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

// TripAdvisorApiService.kt
interface TripAdvisorApiService {
    companion object {
        const val BASE_URL = "https://api.content.tripadvisor.com/api/v1/"
        const val API_KEY = "B38FFB32B51A43FDA3624100F62F3774"
    }

    @GET("location/search")
    suspend fun searchLocations(
        @Query("key") apiKey: String = API_KEY,
        @Query("searchQuery") searchQuery: String,
        @Query("category") category: String? = null,
        @Query("language") language: String = "en"
    ): SearchResponse

    @GET("location/{locationId}/details")
    suspend fun getLocationDetails(
        @Path("locationId") locationId: String,
        @Query("key") apiKey: String = API_KEY
    ): LocationDetails

    @GET("location/{locationId}/photos")
    suspend fun getLocationPhotos(
        @Path("locationId") locationId: String,
        @Query("key") apiKey: String = API_KEY
    ): PhotoResponse

    @GET("location/nearby_search")
    suspend fun getNearbyLocations(
        @Query("key") apiKey: String = API_KEY,
        @Query("latLong") latLong: String,
        @Query("category") category: String? = null,
        @Query("radius") radius: String? = null,
        @Query("radiusUnit") radiusUnit: String? = null,
        @Query("language") language: String = "en",
        @Query("limit") limit: Int = 50
    ): SearchResponse
}