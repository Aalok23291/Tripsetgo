package com.example.tripsetgo


data class TravelItinerary(
    val id: String = System.currentTimeMillis().toString(), // Generate unique ID
    val destination: String,
    val durationDays: Int,
    val mainHotel: LocationDetails,
    val description: String,
    val days: List<ItineraryDay>,
    val highlights: List<String>,
    val photos: List<PhotoData>,
    val totalEstimate: String,
    val bookingUrl: String,
    val mainImage: String?
)