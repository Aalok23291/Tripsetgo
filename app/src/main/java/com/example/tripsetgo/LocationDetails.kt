package com.example.tripsetgo

data class LocationDetails(
    val location_id: String,
    val name: String,
    val description: String?,
    val web_url: String,
    val price_level: String?,
    val rating: String?,         // Add this field
    val latitude: String,
    val longitude: String
)