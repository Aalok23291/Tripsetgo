package com.example.tripsetgo

import com.tripsetgo.api.ApiClient
import retrofit2.HttpException

class ItineraryRepository {

    suspend fun searchItinerary(searchQuery: String): List<LocationData> {
        try {
            val response = ApiClient.tripadvisorApiService.searchLocation(
                apiKey = "B38FFB32B51A43FDA3624100F62F3774",
                searchQuery = searchQuery
            )
            return response.data
        } catch (e: HttpException) {
            throw Exception("Network error: ${e.message()}")
        } catch (e: Exception) {
            throw Exception("Unexpected error: ${e.message}")
        }
    }
}
