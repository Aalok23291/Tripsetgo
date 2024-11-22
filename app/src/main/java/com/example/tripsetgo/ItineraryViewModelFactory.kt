package com.example.tripsetgo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class ItineraryViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ItineraryViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ItineraryViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}