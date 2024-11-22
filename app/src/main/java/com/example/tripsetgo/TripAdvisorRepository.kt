package com.example.tripsetgo

class TripAdvisorRepository(private val apiService: TripAdvisorApiService) {
    suspend fun searchLocations(query: String): Result<List<LocationSearchResult>> {
        return try {
            val response = apiService.searchLocations(searchQuery = query)
            Result.success(response.data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}