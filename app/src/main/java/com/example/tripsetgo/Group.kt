package com.example.tripsetgo

import java.io.Serializable

data class Group(
    var id: String = "", // Ensure this property exists
    var name: String = "",
    var members: MutableList<String> = mutableListOf()
) : Serializable
