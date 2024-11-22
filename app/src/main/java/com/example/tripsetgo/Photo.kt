package com.example.tripsetgo

data class Photo(
    val id: String = "",
    val capsuleId: String = "",
    val url: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val ownerId: String = ""
)