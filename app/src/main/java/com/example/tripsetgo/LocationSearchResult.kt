package com.example.tripsetgo

data class LocationSearchResult(
    val location_id: String,
    val name: String,
    val address_obj: AddressObject,
    val rating: String? = null,  // Add this field
    val web_url: String? = null
)