package com.anshul.smartmediaai.ui.compose.expensetracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Base64
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.anshul.smartmediaai.data.entities.ExpenseEntity
import com.anshul.smartmediaai.data.repository.ExpenseRepo
import com.anshul.smartmediaai.data.repository.ReadSmsRepo
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseItem
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerState
import com.anshul.smartmediaai.util.ExpenseDataFetcher
import com.google.android.gms.location.LocationServices
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.core.content.edit
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist


@HiltViewModel
class ExpenseTrackerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val repo: ExpenseRepo,
    private val readSmsRepo: ReadSmsRepo,
    private val expenseDataFetcher: ExpenseDataFetcher,
    private val preferences: SharedPreferences
) : ContainerHost<ExpenseTrackerState, ExpenseTrackerSideEffect>, ViewModel() {

    override val container: Container<ExpenseTrackerState, ExpenseTrackerSideEffect> =
        container(ExpenseTrackerState())

    companion object {
        const val LAST_SYNC_TIME = "last_sync_time"
        const val TAG = "ExpenseTrackerViewModel"
        const val GMAIL_SCOPE = "oauth2:https://www.googleapis.com/auth/gmail.readonly"
    }

    fun onPermissionResult(granted: Boolean) = intent {
        reduce { state.copy(permissionGranted = granted) }
        if (granted) {
            scanSmsForExpenses()
        } else {
            reduce { state.copy(errorMessage = "SMS permission denied. Cannot scan expenses.") }
            postSideEffect(ExpenseTrackerSideEffect.ShowToast("SMS permission denied."))
        }
    }

    fun scanSmsForExpenses() = intent {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            postSideEffect(ExpenseTrackerSideEffect.RequestSmsPermission)
            return@intent
        }

        reduce { state.copy(isLoading = true, errorMessage = null, expenses = emptyList()) }

        try {

            val firstExpense = repo.getAllExpenses().first()
            val refinedExpenses: List<ExpenseItem> = (if (firstExpense.isNotEmpty()) {
                firstExpense.map {
                    ExpenseItem(
                        merchant = it.description,
                        amount = it.amount,
                        date = it.date,
                        category = it.category.toString()
                    )
                }
            } else {

                val smsMessages = readSmsRepo.readSms(0L)
                if (smsMessages.isEmpty()) {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(ExpenseTrackerSideEffect.ShowToast("No relevant SMS found."))
                    return@intent
                }
                analyseExpenseData(smsMessages)
            }) as List<ExpenseItem>

            buildRefinedExpenseData(refinedExpenses)

        } catch (e: Exception){

        }
    }

    @SuppressLint("SuspiciousIndentation")
   suspend fun analyseExpenseData(messages : List<String>) : List<ExpenseItem> {
        val tempExpenses = mutableListOf<ExpenseItem>()
        try {
                val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI())
                    .generativeModel("gemini-2.5-pro") // Or your preferred model
                val batchSize = 10
                for ((index, batch) in messages.chunked(batchSize).withIndex()) {

                    val prompt = """
                    Extract expense details from each of these SMS messages.
                    For each SMS, identify the merchant, amount, date, and categorize the expense (e.g., Food, Shopping, Bills, Travel, Other).
                    If you cannot determine a value, use "N/A".
                    Return the output as a JSON array of objects. Each object should have keys:
                    "merchant", "amount" (as a number), "date" (YYYY-MM-DD if possible, else original), "category".
            
                    SMS messages:
                    ${batch.joinToString("\n\n")}
                """.trimIndent()

                    val requestContent = content { text(prompt) }
                    val response =
                        generativeModel.generateContent(requestContent) // Or generateContentStream

                    response.text?.let { jsonResponse ->
                        // TODO: Parse the JSON response from Gemini into your ExpenseItem data class
                        // You might need a JSON parsing library like kotlinx.serialization or Gson
                        // For simplicity, this is a placeholder
                        try {
                            // Example (very basic, needs proper JSON parsing and error handling):
                            val finalJson = jsonResponse.replace(Regex("```json|```"), "").trim()
                            // Parse the batch as a list of ExpenseItem
                            val parsedExpenses: List<ExpenseItem> = gson.fromJson(
                                finalJson,
                                object : TypeToken<List<ExpenseItem>>() {}.type
                            )
                            tempExpenses.addAll(parsedExpenses)
                            println("Batch ${index + 1} processed: $finalJson")
                            // println("AI Response for SMS '$sms': $jsonResponse") // Log for debugging
                        } catch (e: Exception) {
                            println("Error parsing AI response: ${e.message}")
                        }
                    }

                val expenseEntities = tempExpenses.map { item ->
                    ExpenseEntity(
                        description = item.merchant,
                        amount = item.amount,
                        date = item.date,
                        category = item.category
                    )

                }
                repo.insertAllExpenses(expenseEntities)
                preferences.edit { putLong(LAST_SYNC_TIME, System.currentTimeMillis()) }

            }

        } catch (e: Exception) {
            e.printStackTrace()
          /*  reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error processing SMS"
                )
            }
            postSideEffect(ExpenseTrackerSideEffect.ShowToast(e.message ?: "Unknown error"))*/
        }
        return tempExpenses
    }

    fun buildRefinedExpenseData(refinedExpenses: List<ExpenseItem>) =  intent{

        val nativeChart = generateNativeChart(refinedExpenses)

        reduce {
            state.copy(
                isLoading = false,
                expenses = refinedExpenses,
                nativeChart = nativeChart
            )
        }



        if (refinedExpenses.isNotEmpty()) {
            val recommendations = analyzeExpensesAndRecommend(refinedExpenses)
            reduce {
                state.copy(
                    recommendation = recommendations
                )
            }
            postSideEffect(ExpenseTrackerSideEffect.ShowToast("Expenses extracted.Recommendations ready"))
        } else {
            postSideEffect(ExpenseTrackerSideEffect.ShowToast("Could not extract details from SMS."))
        }

    }

    suspend fun fetchCurrentLocationSuspend(context: Context): JSONObject =
        suspendCoroutine { cont ->
            fetchCurrentLocation(context) { json ->
                cont.resume(json)
            }
        }

    private suspend fun analyzeExpensesAndRecommend(expenses: List<ExpenseItem>): String? {
        return withContext(Dispatchers.IO) {
            try {
                val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI())
                    .generativeModel("gemini-2.5-pro") // Use PRO model for reasoning

                val expensesJson = gson.toJson(expenses)
                val locationJson = fetchCurrentLocationSuspend(context)
                val lat = locationJson.optDouble("latitude")
                val lon = locationJson.optDouble("longitude")
                val currentLocation = locationJson.optString("city")
                val restaurantsJson = expenseDataFetcher.fetchNearbyRestaurants(lat, lon)
                val servicesJson = expenseDataFetcher.fetchNearbyServices(lat, lon)

                val prompt = """
                You are an AI financial assistant.
                User’s recent expenses (JSON): $expensesJson
                User’s location: $currentLocation
                
                Nearby restaurants: $restaurantsJson
                Nearby services: $servicesJson
            
                
                Tasks:
                1. Identify spending patterns.
                2. Recommend cheaper local/online alternatives from API data.
                3. Suggest exact mutual funds with past CAGR.
                4. Compare potential fund growth vs current expense values.
                5. Give concise, actionable recommendations in ≤50 words.
                """.trimIndent()

                Log.i("prompt", prompt)


                val requestContent = content { text(prompt) }
                val response = generativeModel.generateContent(requestContent)

                return@withContext response.text
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

    private fun aggregateExpensesByCategory(expenses: List<ExpenseItem>): Map<String, Double> {
        return expenses.groupBy { it.category }
            .mapValues { entry -> entry.value.sumOf { it.amount } }
    }

    private fun generateNativeChart(expenseItem: List<ExpenseItem>): Map<String, Double> {
        if (expenseItem.isEmpty()) return emptyMap()
        val aggregatedData = aggregateExpensesByCategory(expenseItem)
        return aggregatedData

    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchCurrentLocation(context: Context, onResult: (JSONObject) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses =
                        geocoder.getFromLocation(location.latitude, location.longitude, 1)

                    val address = addresses?.firstOrNull()
                    val locationJson = JSONObject().apply {
                        put("latitude", location.latitude)
                        put("longitude", location.longitude)
                        put("city", address?.locality ?: "Unknown City")
                        put("state", address?.adminArea ?: "Unknown State")
                        put("country", address?.countryName ?: "Unknown Country")
                        put("postalCode", address?.postalCode ?: "Unknown PostalCode")
                    }

                    onResult(locationJson)
                } catch (e: Exception) {
                    onResult(JSONObject().put("error", "Error resolving location"))
                }
            } else {
                onResult(JSONObject().put("error", "Location not available"))
            }
        }.addOnFailureListener {
            onResult(JSONObject().put("error", "Error fetching location"))
        }
    }

    internal fun fetchGmailAccessToken(
        context: Context,
        email: String
    ) {
        intent {

            reduce {
                state.copy(isLoading = true)
            }
            try {
                val token = GoogleAuthUtil.getToken(context, email, GMAIL_SCOPE)
                Log.d(TAG, "Access Token: $token")
                val bearerToken = "Bearer $token"
                val response = repo.readEmails(bearerToken, "(\"debited from account\" OR \"withdrawn from account\") -SIP -EMI -AutoPay -mutual -insurance newer_than:30d")
                val allDecodedTexts = coroutineScope {
                    response.messages
                        ?.distinctBy { it.threadId } // avoid duplicate calls
                        ?.map { messageItem ->
                            async(Dispatchers.IO) {
                                val threadResponse =
                                    repo.readThreads(bearerToken, messageItem.threadId)
                                threadResponse.messages?.flatMap { payload ->
                                    val payloadParts = payload.payload
                                    if (payloadParts.parts.isNullOrEmpty()) {
                                        listOf(
                                            extractPlainTextFromHtml(decodeString(payloadParts.body.data))
                                        )
                                    } else {
                                        payloadParts.parts.map {
                                            extractPlainTextFromHtml(decodeString(it.body.data))
                                        }
                                    }
                                } ?: emptyList()
                            }
                        }?.awaitAll()?.flatten()
                } ?: emptyList()
                Log.d("Anshul", "Fetched ${allDecodedTexts.size} messages")

                val expenseList = analyseExpenseData(allDecodedTexts)
                buildRefinedExpenseData(expenseList)
            } catch (e: UserRecoverableAuthException) {
                Log.w(TAG, "Need user consent to access Gmail", e)
                // Launch consent screen on main thread
                withContext(Dispatchers.Main) {
                    reduce {
                        state.copy(
                            gmailConsentIntent = e.intent
                        )
                    }
                }
            } catch (e: GoogleAuthException) {
                Log.e(TAG, "GoogleAuthException while fetching token", e)
            } catch (e: Exception) {
                Log.e(TAG, "Unknown error fetching token", e)
            }
        }
    }

    fun decodeString(encoded: String): String {
        val normalized = encoded.replace('-', '+').replace('_', '/').let {
            val mod = it.length % 4
            if (mod == 0) it else it + "=".repeat(4 - mod)
        }
        val decodedBytes = Base64.decode(normalized, Base64.DEFAULT)
        return String(decodedBytes, Charsets.UTF_8)

    }

    fun extractPlainTextFromHtml(html: String): String {
        // Remove scripts/styles and convert to readable text
        val doc = Jsoup.parse(html)
        doc.select("script, style, footer, img, nav").remove() // remove noise
        return Jsoup.clean(doc.body().html(), Safelist.none())
            .replace(Regex("\\s+"), " ") // collapse whitespace
            .trim()
    }

}
