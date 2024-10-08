import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response

class NetworkRepository {
    private val client = OkHttpClient()

    fun fetchItineraries(apiKey: String, searchQuery: String): Response {
        val url = "https://api.content.tripadvisor.com/api/v1/location/search?searchQuery=$searchQuery&key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .get()
            .addHeader("accept", "application/json")
            .build()

        return client.newCall(request).execute()
    }
}
