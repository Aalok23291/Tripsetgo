package com.example.tripsetgo

data class LocationDetailsResponse(
    val id: String,
    val name: String,
    val description: String
) {
    fun toLocationDetails(): LocationDetails {
        return LocationDetails(
            id = id,
            name = name,
            description = description
        )
    }
}
