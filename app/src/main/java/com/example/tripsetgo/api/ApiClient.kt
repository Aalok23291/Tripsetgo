package com.tripsetgo.api

import com.example.tripsetgo.LocationDetailsResponse
import com.example.tripsetgo.LocationPhotosResponse
import com.example.tripsetgo.TripadvisorResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface TripadvisorApiService {

    // Existing method for searching location
    @GET("location/search")
    suspend fun searchLocation(
        @Query("key") apiKey: String,
        @Query("searchQuery") searchQuery: String,
        @Query("language") language: String = "en"
    ): TripadvisorResponse

    // New method for fetching location details
    @GET("location/{locationId}/details")
    suspend fun getLocationDetails(
        @Path("locationId") locationId: String,
        @Query("key") apiKey: String,
        @Query("language") language: String = "en"
    ): LocationDetailsResponse // Create a data class for this response

    // New method for fetching location photos
    @GET("location/{locationId}/photos")
    suspend fun getLocationPhotos(
        @Path("locationId") locationId: String,
        @Query("key") apiKey: String,
        @Query("language") language: String = "en"
    ): LocationPhotosResponse // Create a data class for this response
}
object ApiClient {
    private const val BASE_URL = "https://api.content.tripadvisor.com/api/v1/"

    val tripadvisorApiService: TripadvisorApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(TripadvisorApiService::class.java)
    }
}
