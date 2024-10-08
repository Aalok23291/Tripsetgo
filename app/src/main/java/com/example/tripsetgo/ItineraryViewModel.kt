package com.example.tripsetgo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripsetgo.api.ApiClient
import kotlinx.coroutines.launch

class ItineraryViewModel : ViewModel() {
    private val _locations = MutableLiveData<List<LocationData>>()
    val locations: LiveData<List<LocationData>> get() = _locations

    fun searchLocation(query: String) {
        viewModelScope.launch {
            try {
                // Replace with your actual API call
                val response = ApiClient.tripadvisorApiService.searchLocation("B38FFB32B51A43FDA3624100F62F3774", query)
                _locations.value = response.data // Ensure this maps to your LocationData class
            } catch (e: Exception) {
                Log.e("ItineraryViewModel", "Error fetching locations", e)
                _locations.value = emptyList()
            }
        }
    }
}
