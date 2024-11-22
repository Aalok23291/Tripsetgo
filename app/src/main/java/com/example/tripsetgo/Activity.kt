package com.example.tripsetgo

data class Activity(
    val time: String,
    val title: String,
    val description: String,
    val location: String,
    val type: ActivityType,
    val duration: String,  // Add duration parameter
    val bookingUrl: String?,
    val photoUrl: String?
)