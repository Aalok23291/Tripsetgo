package com.example.tripsetgo


data class ItineraryDay(
    val dayNumber: Int,
    val title: String,
    val description: String,
    val activities: List<Activity>
)