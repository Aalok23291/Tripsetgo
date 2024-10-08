package com.example.tripsetgo

data class TripadvisorResponse(
    val data: List<LocationData>
)

data class LocationData(
    val location_id: String,
    val name: String,
    val id: String,
    val description: String,
    val photos: List<LocationPhoto> // Ensure LocationPhoto is defined before this
)

data class AddressObject(
    val street1: String?,
    val street2: String?,
    val city: String?,
    val state: String?,
    val country: String?,
    val postalcode: String?,
    val address_string: String
)

data class LocationPhoto(
    val images: Images // Holds different sizes of image URLs
)

data class Images(
    val large: ImageUrl // Add other sizes if needed
)

data class ImageUrl(
    val url: String // This field holds the actual image URL
)
