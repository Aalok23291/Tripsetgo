package com.example.tripsetgo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class ItineraryViewModel : ViewModel() {
    private val apiService = NetworkModule.provideApiService()
    private val _itineraryState = MutableLiveData<ItineraryState>()
    val itineraryState: LiveData<ItineraryState> = _itineraryState

    private suspend fun getLocationWithPhotos(locationId: String): Pair<String?, List<PhotoData>> {
        return try {
            val photos = apiService.getLocationPhotos(locationId).data
            Pair(photos.firstOrNull()?.images?.large?.url, photos)
        } catch (e: Exception) {
            Pair(null, emptyList())
        }
    }

    fun searchLocation(query: String) {
        viewModelScope.launch {
            try {
                _itineraryState.value = ItineraryState.Loading

                // First get hotels specifically
                val hotels = apiService.searchLocations(
                    searchQuery = query,
                    category = "hotels"
                ).data.sortedByDescending {
                    it.rating?.toFloatOrNull() ?: 0f
                }

                // Get the best rated hotel
                val mainHotel = hotels.firstOrNull()
                    ?: throw Exception("No hotels found in this location")

                // Get hotel details
                val hotelDetails = apiService.getLocationDetails(mainHotel.location_id)
                val (hotelPhotoUrl, hotelPhotos) = getLocationWithPhotos(mainHotel.location_id)

                // Get nearby attractions and restaurants using hotel's coordinates
                val latLong = "${hotelDetails.latitude},${hotelDetails.longitude}"

                // Increase radius and try to get attractions
                val attractionsWithPhotos = tryGetNearbyPlaces(
                    latLong = latLong,
                    category = "attractions",
                    radius = "50" // Increased radius to 20km
                )

                // Get restaurants with increased radius
                val restaurantsWithPhotos = tryGetNearbyPlaces(
                    latLong = latLong,
                    category = "restaurants",
                    radius = "30" // 10km for restaurants
                )

                // Create itinerary with available places
                val itinerary = createItinerary(
                    mainLocation = hotelDetails,
                    mainLocationPhoto = hotelPhotoUrl,
                    attractionsWithPhotos = attractionsWithPhotos,
                    restaurantsWithPhotos = restaurantsWithPhotos,
                    allPhotos = hotelPhotos
                )

                _itineraryState.value = ItineraryState.Success(itinerary)

            } catch (e: Exception) {
                _itineraryState.value = ItineraryState.Error(
                    "Unable to create itinerary for $query. Please try another location."
                )
            }
        }
    }
    private suspend fun tryGetNearbyPlaces(
        latLong: String,
        category: String,
        radius: String
    ): List<Pair<LocationSearchResult, String?>> {
        return try {
            val places = apiService.getNearbyLocations(
                latLong = latLong,
                category = category,
                radiusUnit = "km",
                radius = "50"  // Increased radius significantly
            ).data
                .distinctBy { it.name }  // Remove duplicates
                .sortedByDescending { it.rating?.toFloatOrNull() ?: 0f }
                .take(10)  // Take more places
                .map { place ->
                    val (photoUrl, _) = getLocationWithPhotos(place.location_id)
                    Pair(place, photoUrl)
                }

            if (places.isEmpty() && radius.toInt() < 100) {  // Try even larger radius
                tryGetNearbyPlaces(latLong, category, (radius.toInt() + 20).toString())
            } else {
                places
            }
        } catch (e: Exception) {
            Log.e("TripSetGo", "Error fetching nearby places: ${e.message}")
            emptyList()
        }
    }

    private fun createItinerary(
        mainLocation: LocationDetails,
        mainLocationPhoto: String?,
        attractionsWithPhotos: List<Pair<LocationSearchResult, String?>>,
        restaurantsWithPhotos: List<Pair<LocationSearchResult, String?>>,
        allPhotos: List<PhotoData>
    ): TravelItinerary {
        return TravelItinerary(
            destination = mainLocation.name,
            durationDays = 3,
            mainHotel = mainLocation,
            description = mainLocation.description ?: "Explore the beautiful ${mainLocation.name}",
            days = createDays(mainLocation, mainLocationPhoto, attractionsWithPhotos, restaurantsWithPhotos),
            highlights = listOf(
                "Hotel Stay at ${mainLocation.name}",
                "Local Attractions",
                "Cultural Experiences",
                "Local Cuisine",
                "Shopping"
            ),
            photos = allPhotos,
            totalEstimate = mainLocation.price_level ?: "$",
            bookingUrl = mainLocation.web_url,
            mainImage = mainLocationPhoto
        )
    }

    private fun createDays(
        hotel: LocationDetails,
        hotelPhoto: String?,
        attractionsWithPhotos: List<Pair<LocationSearchResult, String?>>,
        restaurantsWithPhotos: List<Pair<LocationSearchResult, String?>>
    ): List<ItineraryDay> {
        return listOf(
            // Day 1
            ItineraryDay(
                dayNumber = 1,
                title = "Arrival & City Exploration",
                description = "Begin your journey with hotel check-in and city exploration",
                activities = buildList {
                    add(Activity(
                        time = "09:00 AM",
                        title = "Hotel Check-in",
                        description = "Check in at ${hotel.name}",
                        location = hotel.name,
                        type = ActivityType.HOTEL,
                        duration = "1 hour",
                        bookingUrl = hotel.web_url,
                        photoUrl = hotelPhoto
                    ))

                    attractionsWithPhotos.getOrNull(0)?.let { (attraction, photoUrl) ->
                        add(Activity(
                            time = "11:00 AM",
                            title = "Visit ${attraction.name}",
                            description = "Explore one of the top attractions",
                            location = attraction.name,
                            type = ActivityType.ATTRACTION,
                            duration = "2 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }

                    restaurantsWithPhotos.getOrNull(0)?.let { (restaurant, photoUrl) ->
                        add(Activity(
                            time = "01:00 PM",
                            title = "Lunch at ${restaurant.name}",
                            description = "Enjoy local cuisine",
                            location = restaurant.name,
                            type = ActivityType.RESTAURANT,
                            duration = "1.5 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }

                    attractionsWithPhotos.getOrNull(1)?.let { (attraction, photoUrl) ->
                        add(Activity(
                            time = "03:00 PM",
                            title = "Visit ${attraction.name}",
                            description = "Explore second attraction",
                            location = attraction.name,
                            type = ActivityType.ATTRACTION,
                            duration = "2 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }

                    restaurantsWithPhotos.getOrNull(1)?.let { (restaurant, photoUrl) ->
                        add(Activity(
                            time = "07:00 PM",
                            title = "Dinner at ${restaurant.name}",
                            description = "Evening dining experience",
                            location = restaurant.name,
                            type = ActivityType.RESTAURANT,
                            duration = "2 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }
                }
            ),

            // Day 2
            ItineraryDay(
                dayNumber = 2,
                title = "Cultural Exploration",
                description = "Immerse yourself in local culture and experiences",
                activities = buildList {
                    add(Activity(
                        time = "09:00 AM",
                        title = "Breakfast at Hotel",
                        description = "Start your day with breakfast",
                        location = hotel.name,
                        type = ActivityType.RESTAURANT,
                        duration = "1 hour",
                        bookingUrl = null,
                        photoUrl = hotelPhoto
                    ))

                    attractionsWithPhotos.getOrNull(2)?.let { (attraction, photoUrl) ->
                        add(Activity(
                            time = "10:30 AM",
                            title = "Visit ${attraction.name}",
                            description = "Morning cultural experience",
                            location = attraction.name,
                            type = ActivityType.ATTRACTION,
                            duration = "2.5 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }

                    restaurantsWithPhotos.getOrNull(2)?.let { (restaurant, photoUrl) ->
                        add(Activity(
                            time = "01:00 PM",
                            title = "Lunch at ${restaurant.name}",
                            description = "Local lunch experience",
                            location = restaurant.name,
                            type = ActivityType.RESTAURANT,
                            duration = "1.5 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }

                    add(Activity(
                        time = "03:00 PM",
                        title = "Shopping & Local Market Visit",
                        description = "Explore local markets and shopping areas",
                        location = "Local Markets",
                        type = ActivityType.SHOPPING,
                        duration = "3 hours",
                        bookingUrl = null,
                        photoUrl = null
                    ))

                    restaurantsWithPhotos.getOrNull(3)?.let { (restaurant, photoUrl) ->
                        add(Activity(
                            time = "07:30 PM",
                            title = "Dinner at ${restaurant.name}",
                            description = "Evening dining",
                            location = restaurant.name,
                            type = ActivityType.RESTAURANT,
                            duration = "2 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }
                }
            ),

            // Day 3
            ItineraryDay(
                dayNumber = 3,
                title = "Leisure & Departure",
                description = "Wrap up your trip with final experiences",
                activities = buildList {
                    add(Activity(
                        time = "09:00 AM",
                        title = "Breakfast at Hotel",
                        description = "Final breakfast",
                        location = hotel.name,
                        type = ActivityType.RESTAURANT,
                        duration = "1 hour",
                        bookingUrl = null,
                        photoUrl = hotelPhoto
                    ))

                    attractionsWithPhotos.getOrNull(3)?.let { (attraction, photoUrl) ->
                        add(Activity(
                            time = "10:30 AM",
                            title = "Visit ${attraction.name}",
                            description = "Final sightseeing",
                            location = attraction.name,
                            type = ActivityType.ATTRACTION,
                            duration = "2 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }

                    restaurantsWithPhotos.getOrNull(4)?.let { (restaurant, photoUrl) ->
                        add(Activity(
                            time = "01:00 PM",
                            title = "Farewell Lunch at ${restaurant.name}",
                            description = "Final meal",
                            location = restaurant.name,
                            type = ActivityType.RESTAURANT,
                            duration = "1.5 hours",
                            bookingUrl = null,
                            photoUrl = photoUrl
                        ))
                    }

                    add(Activity(
                        time = "03:00 PM",
                        title = "Hotel Check-out",
                        description = "Check out and departure",
                        location = hotel.name,
                        type = ActivityType.HOTEL,
                        duration = "1 hour",
                        bookingUrl = hotel.web_url,
                        photoUrl = hotelPhoto
                    ))
                }
            )
        )
    }
}

sealed class ItineraryState {
    object Initial : ItineraryState()
    object Loading : ItineraryState()
    data class Success(val itinerary: TravelItinerary) : ItineraryState()
    data class Error(val message: String) : ItineraryState()
}