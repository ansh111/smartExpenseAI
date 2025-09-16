package com.anshul.smartmediaai.util

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class ExpenseDataFetcher(
    private val googleApiKey: String,
    private val amazonApiKey: String? = null // optional if you integrate Amazon/Flipkart API later
) {

    // 🔹 Fetch nearby restaurants using Google Maps Places API
    suspend fun fetchNearbyRestaurants(lat: Double, lon: Double): JSONObject =
        withContext(Dispatchers.IO) {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=$lat,$lon&radius=2000&type=restaurant&key=$googleApiKey"

            val response = httpGet(url)
            JSONObject(response)
        }

    // 🔹 Fetch nearby services/stores using Google Maps Places API
    suspend fun fetchNearbyServices(lat: Double, lon: Double, type: String = "store"): JSONObject =
        withContext(Dispatchers.IO) {
            val url = "https://maps.googleapis.com/maps/api/place/nearbysearch/json" +
                    "?location=$lat,$lon&radius=3000&type=$type&key=$googleApiKey"

            val response = httpGet(url)
            JSONObject(response)
        }

    // 🔹 Stub for Amazon/Flipkart integration (requires API credentials)
    suspend fun fetchShoppingProduct(productName: String): JSONObject =
        withContext(Dispatchers.IO) {
            // TODO: Replace with Amazon Product Advertising API / Flipkart Affiliate API
            JSONObject().apply {
                put("product", productName)
                put("price", "N/A")
                put("platform", "Amazon/Flipkart API not integrated")
            }
        }

    // 🔹 Simple HTTP GET helper
    private fun httpGet(urlString: String): String {
        val url = URL(urlString)
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        return connection.inputStream.bufferedReader().use { it.readText() }
    }
}
