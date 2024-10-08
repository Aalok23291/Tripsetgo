package com.example.tripsetgo

import android.app.Activity
import android.content.Context
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class RentalRepository(private val context: Context) {

    fun fetchRentals(query: String, onSuccess: (List<RentalLocation>) -> Unit, onFailure: (String) -> Unit) {
        val client = OkHttpClient()

        // Update the API call with the search query
        val request = Request.Builder()
            .url("https://tripadvisor16.p.rapidapi.com/api/v1/rentals/searchLocation?query=$query")
            .get()
            .addHeader("x-rapidapi-key", "your-api-key")
            .addHeader("x-rapidapi-host", "tripadvisor16.p.rapidapi.com")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string()
                if (body != null) {
                    val rentals = parseResponse(body)

                    (context as Activity).runOnUiThread {
                        onSuccess(rentals)
                    }
                } else {
                    (context as Activity).runOnUiThread {
                        onFailure("Empty response")
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                (context as Activity).runOnUiThread {
                    onFailure(e.message ?: "Network Error")
                }
            }
        })
    }

    private fun parseResponse(responseBody: String): List<RentalLocation> {
        val rentals = mutableListOf<RentalLocation>()
        try {
            val jsonResponse = JSONObject(responseBody)
            val dataArray: JSONArray = jsonResponse.getJSONArray("data")

            for (i in 0 until dataArray.length()) {
                val rentalObject: JSONObject = dataArray.getJSONObject(i)
                val locationId = rentalObject.getInt("location_id")
                val name = rentalObject.getString("name")
                val price = rentalObject.getString("price")

                val address = ""
                rentals.add(RentalLocation(locationId, name, price,address))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return rentals
    }
}
