package com.example.tripsetgo

data class LocationDetailsResponse(
    val location_id: String,
    val name: String,
    val description: String?,
    val web_url: String,
    val address_obj: AddressObject,
    val latitude: String,
    val longitude: String,
    val timezone: String,
    val rating: String?,
    val price_level: String?,
    val rating_image_url: String?,
    val num_reviews: String?,
    val ranking_data: RankingData?,
    val amenities: List<String>,
    val category: Category
) {
    fun toLocationDetails(): LocationDetails {
        return LocationDetails(
            location_id = location_id,
            name = name,
            description = description ?: "No description available",
            web_url = web_url,
            price_level = price_level ?: "$",
            rating = rating,
            latitude = latitude,
            longitude = longitude
        )
    }
}



data class RankingData(
    val geo_location_id: String,
    val ranking_string: String,
    val ranking_out_of: String,
    val ranking: String
)

data class Category(
    val name: String,
    val localized_name: String
)