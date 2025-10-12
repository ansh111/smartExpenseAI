package com.anshul.smartmediaai.ui.compose.expensetracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.provider.Telephony
import android.util.Base64
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
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
import org.json.JSONArray
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import java.util.Locale
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.anshul.smartmediaai.BuildConfig.WEB_CLIENT_ID
import com.anshul.smartmediaai.util.constants.ApiConstants.GMAIL_READ_URL
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.UUID


@HiltViewModel
class ExpenseTrackerViewModel @Inject constructor(
    @ApplicationContext private val context: Context ,
    private val gson: Gson,
    private val repo: ExpenseRepo,
    private val readSmsRepo: ReadSmsRepo,
    private val expenseDataFetcher: ExpenseDataFetcher,
    private  val preferences: SharedPreferences
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

    @SuppressLint("SuspiciousIndentation")
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
            val refinedExpenses:  List<ExpenseItem> = if(firstExpense.isNotEmpty()){
                firstExpense.map {
                    ExpenseItem(
                        merchant = it.description,
                        amount = it.amount,
                        date = it.date,
                        category = it.category.toString()
                    )
                }
            }else {

                val smsMessages = readSmsRepo.readSms(0L)
                if (smsMessages.isEmpty()) {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(ExpenseTrackerSideEffect.ShowToast("No relevant SMS found."))
                    return@intent
                }

                val tempExpenses = mutableListOf<ExpenseItem>()
                val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI())
                    .generativeModel("gemini-2.5-pro") // Or your preferred model
                val batchSize = 10
                for ((index, batch) in smsMessages.chunked(batchSize).withIndex()) {

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
                tempExpenses
            }


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

        } catch (e: Exception) {
            e.printStackTrace()
            reduce {
                state.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Error processing SMS"
                )
            }
            postSideEffect(ExpenseTrackerSideEffect.ShowToast(e.message ?: "Unknown error"))
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
                val restaurantsJson = expenseDataFetcher.fetchNearbyRestaurants(lat,lon)
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

                Log.i("prompt",prompt)


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
        if(expenseItem.isEmpty()) return emptyMap()
        val aggregatedData = aggregateExpensesByCategory(expenseItem)
        return aggregatedData

    }

    private fun generateCategoryChartHtml(expenseItems: List<ExpenseItem>): String {
        if(expenseItems.isEmpty()) return ""
        val aggregatedData = aggregateExpensesByCategory(expenseItems)
        if (aggregatedData.isEmpty()) return ""
        val labels = JSONArray(aggregatedData.keys.toList())
        val dataValues = JSONArray(aggregatedData.values.toList())
        // Simple background colors for pie chart segments
        val backgroundColors = JSONArray(listOf(
            "#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0", "#9966FF", "#FF9F40",
            "#FFCD56", "#C9CBCF", "#3FC77D", "#E7E9ED" // Add more if you expect more categories
        ).take(aggregatedData.size))
        return """
           <!DOCTYPE html>
           <html>
           <head>
               <title>Expense Category Chart</title>
               <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
               <style>
                   body { margin: 0; display: flex; justify-content: center; align-items: center; height: 100vh; background-color: #f0f0f0; }
                   #categoryChart { max-width: 95%; max-height: 95%; } /* Responsive chart size */
               </style>
           </head>
           <body>
               <canvas id="categoryChart"></canvas>
               <script>
                   const ctx = document.getElementById('categoryChart').getContext('2d');
                   new Chart(ctx, {
                       type: 'pie', // You can change to 'bar', 'doughnut', etc.
                       data: {
                           labels: $labels,
                           datasets: [{
                               label: 'Expenses by Category',
                               data: $dataValues,
                               backgroundColor: $backgroundColors,
                               hoverOffset: 4
                           }]
                       },
                       options: {
                           responsive: true,
                           maintainAspectRatio: false, // Allows chart to fill container better
                           plugins: {
                               legend: {
                                   position: 'top', // Or 'bottom', 'left', 'right'
                               },
                               title: {
                                   display: true,
                                   text: 'Expense Distribution by Category'
                               }
                           }
                       }
                   });
               </script>
           </body>
           </html>
           """.trimIndent()
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    fun fetchCurrentLocation(context: Context, onResult: (JSONObject) -> Unit) {
        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                try {
                    val geocoder = Geocoder(context, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)

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

    internal fun createGoogleSignInWithButton() {
        val signInWithGoogleOption: GetSignInWithGoogleOption = GetSignInWithGoogleOption.Builder(
            serverClientId = WEB_CLIENT_ID
        ).setNonce(UUID.randomUUID().toString())
            .build()

        val credentialManager = CredentialManager.create(context)

        val request: GetCredentialRequest = GetCredentialRequest.Builder()
            .addCredentialOption(signInWithGoogleOption)
            .build()

        intent {
                try {
                    val result = credentialManager.getCredential(
                        request = request,
                        context = context,
                    )
                    handleSignInWithGoogleOption(context,result)
                } catch (e: GetCredentialException) {
                    e.printStackTrace()
                }
        }


    }

   private fun handleSignInWithGoogleOption(context: Context,
                                            result: GetCredentialResponse) = intent {
        // Handle the successfully returned credential.
        val credential = result.credential

        when (credential) {
            is CustomCredential -> {
                if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                    try {
                        // Use googleIdTokenCredential and extract id to validate and
                        // authenticate on your server.
                        val googleIdTokenCredential = GoogleIdTokenCredential
                            .createFrom(credential.data)

                        fetchGmailAccessToken(context, googleIdTokenCredential.id)
                    } catch (e: GoogleIdTokenParsingException) {
                        Log.e(TAG, "Received an invalid google id token response", e)
                    }
                } else {
                    // Catch any unrecognized credential type here.
                    postSideEffect(ExpenseTrackerSideEffect.ShowToast("Unexpected type of credential"))
                }
            }

            else -> {
                // Catch any unrecognized credential type here.
                Log.e(TAG, "Unexpected type of credential")
                postSideEffect(ExpenseTrackerSideEffect.ShowToast("Sign-in failed for user"))
            }
        }
    }

    internal fun fetchGmailAccessToken(
        context: Context,
        email: String
    ) {
        intent {
            try {
                val token = GoogleAuthUtil.getToken(context, email, GMAIL_SCOPE)
                Log.d(TAG, "Access Token: $token")
                val bearerToken = "Bearer $token"
                val response  = repo.readEmails(bearerToken, "debit newer_than:30d")

                response.messages?.forEach { messageItem ->
                    val getThreadResponse = repo.readThreads(bearerToken, messageItem.threadId)
                    getThreadResponse.messages?.get(0)?.payload?.parts?.forEach { item ->
                        val item = item
                        val subject = item.headers?.find { it.name == "Subject" }?.value
                        val bodyEncoded = item.body.data
                        val body = bodyEncoded?.let {
                            String(Base64.decode(it, Base64.URL_SAFE or Base64.NO_WRAP))
                        }
                        println("Subject: $subject\nBody: $body\n")
                    }
                    Log.d(TAG, "message" + messageItem.id)

                }

            } catch (e: UserRecoverableAuthException) {
                Log.w(TAG, "Need user consent to access Gmail", e)
                // Launch consent screen on main thread
                withContext(Dispatchers.Main) {
                    reduce{
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

}
