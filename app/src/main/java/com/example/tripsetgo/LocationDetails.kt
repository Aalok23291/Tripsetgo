package com.example.tripsetgo

data class LocationDetails(
    val id: String,
    val name: String,
    val description: String
)

val defaultDetails = LocationDetails(
    id = "defaultId",
    name = "Default Location",
    description = "No description available."
)
