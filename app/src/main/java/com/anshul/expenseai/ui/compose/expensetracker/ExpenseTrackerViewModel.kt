package com.anshul.expenseai.ui.compose.expensetracker

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Geocoder
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.anshul.expenseai.data.entities.ExpenseEntity
import com.anshul.expenseai.data.repository.ExpenseRepo
import com.anshul.expenseai.data.repository.ReadSmsRepo
import com.anshul.expenseai.ui.compose.expensetracker.state.ExpenseItem
import com.anshul.expenseai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect
import com.anshul.expenseai.ui.compose.expensetracker.state.ExpenseTrackerState
import com.anshul.expenseai.util.ExpenseDataFetcher
import com.google.android.gms.location.LocationServices
import com.google.common.reflect.TypeToken
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
import com.anshul.expenseai.data.model.ExpenseCategoryUI
import com.anshul.expenseai.data.repository.GmailRepo
import com.anshul.expenseai.ui.compose.expensetracker.bottomsheet.GoogleSignInBottomSheet
import com.anshul.expenseai.util.HelperFunctions.useExponentialBackoffRetry
import com.anshul.expenseai.util.tf.SMSClassifierUtility.extractAmount
import com.anshul.expenseai.util.tf.SMSClassifierUtility.extractDate
import com.anshul.expenseai.util.tf.SMSClassifierUtility.extractMerchant
import com.anshul.expenseai.util.tf.SMSClassifierUtility.isCollectRequest
import com.anshul.expenseai.util.tf.SMSClassifierUtility.isSelfTransfer
import com.anshul.expenseai.util.tf.SMSClassifierUtility.isWalletTopUp
import com.anshul.expenseai.util.constants.ExpenseConstant.FIRST_GMAIL_SIGN_DONE
import com.anshul.expenseai.util.constants.ExpenseConstant.RECOMMENDATION_SAVED_RESPONSE
import com.anshul.expenseai.util.tf.ExpenseClassifier
import com.google.android.gms.auth.GoogleAuthException
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.firebase.ai.GenerativeModel
import com.google.firebase.ai.type.FirebaseAIException
import com.google.firebase.util.nextAlphanumericString
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.first
import kotlin.collections.addAll
import kotlin.random.Random


@HiltViewModel
class ExpenseTrackerViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson,
    private val repo: ExpenseRepo,
    private val readSmsRepo: ReadSmsRepo,
    private val expenseDataFetcher: ExpenseDataFetcher,
    private val preferences: SharedPreferences,
    private val gmailRepo : GmailRepo,
    private val generativeModel: GenerativeModel
) : ContainerHost<ExpenseTrackerState, ExpenseTrackerSideEffect>, ViewModel() {

    override val container: Container<ExpenseTrackerState, ExpenseTrackerSideEffect> =
        container(ExpenseTrackerState())

    private var isSignInOccurred = false
    companion object {
        const val LAST_SYNC_TIME = "last_sync_time"
        const val TAG = "ExpenseTrackerViewModel"
    }

    internal suspend fun delete30DaysOldExpenses() {
        repo.delete30DaysOldExpenses()
    }

    fun checkForSMSPermission() = intent {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            postSideEffect(ExpenseTrackerSideEffect.RequestSMSPermission)
            return@intent
        }else{
            scanSmsForExpenses()
        }
    }

    fun onPermissionResult(granted: Boolean) = intent {
        if (granted) {
            fetchGmailData(context)
        } else {
            reduce { state.copy(errorMessage = "Location permission denied Please enable it from settings.") }
            postSideEffect(ExpenseTrackerSideEffect.ShowToast("Location permission denied."))
        }
    }

    fun onPermissionResultSMS(granted: Boolean) = intent {
        if (granted) {
            scanSmsForExpenses()
        } else {
            if (preferences.getBoolean(FIRST_GMAIL_SIGN_DONE, false)) {
                postSideEffect(ExpenseTrackerSideEffect.SkipGmailSignInFlow)
            } else {
                postSideEffect(ExpenseTrackerSideEffect.ShowGmailBottomSheet)

            }
        }
    }

    /**
     * Function to rescrit calling GoogleSignIn more than once in view model lifecycle
     */
    internal fun firstTimeSignInOccurred(action:() -> Unit) {
         if(!isSignInOccurred){
             isSignInOccurred = true
             action()
         }
    }

    internal fun setIsFirstTimeSignInFromGoogleButton(isFirstSignIn: Boolean){
        isSignInOccurred = isFirstSignIn
    }

    fun scanSmsForExpenses() = intent {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        if (!hasPermission) {
            postSideEffect(ExpenseTrackerSideEffect.RequestSMSPermission)
            return@intent
        }

        reduce { state.copy(isLoading = true, errorMessage = null, expenses = emptyList()) }

        try {

            val firstExpense = repo.getAllExpenses().first()
            val  lastSyncTime =  preferences.getLong(LAST_SYNC_TIME,0L)
            var isSMSApiCallHappened = false

            val refinedExpenses: List<ExpenseItem> = (if (firstExpense.isNotEmpty()) {
                if(lastSyncTime != System.currentTimeMillis()){
                    val smsMessage =  readSmsRepo.readSms(lastSyncTime)
                    analyseExpenseData(smsMessage)
                    if(smsMessage.isNotEmpty())
                        isSMSApiCallHappened = true
                }
                firstExpense.map {
                    ExpenseItem(
                        merchant = it.description,
                        amount = it.amount,
                        date = it.date,
                        category = it.category.toString(),
                        messageId = it.messageId
                    )
                }
            } else {

                val smsMessages = readSmsRepo.readSms(0L)
                isSMSApiCallHappened = true
                if (smsMessages.isEmpty()) {
                    reduce { state.copy(isLoading = false) }
                    postSideEffect(ExpenseTrackerSideEffect.ShowToast("No relevant SMS found."))
                    return@intent
                }

               // analyseExpenseData(smsMessages)
                analyseUsingTfLite(smsMessages)
            }) as List<ExpenseItem>

            buildRefinedExpenseData(refinedExpenses, isSMSApiCallHappened)

        } catch (e: Exception){

        }
    }

    suspend fun analyseUsingTfLite(
        smsMessages: List<String>
    ): List<ExpenseItem> = coroutineScope {

        val classifier = ExpenseClassifier(context)
        val resultExpense = mutableListOf<ExpenseItem>()

        try {
            smsMessages.forEach { msg ->

                val normalized = msg.lowercase()

                if (isSelfTransfer(normalized)) return@forEach
                if (isWalletTopUp(normalized)) return@forEach
                if (isCollectRequest(normalized)) return@forEach

                val amount = extractAmount(msg) ?: return@forEach

                val merchant = extractMerchant(msg) ?: return@forEach

                val date = extractDate(msg)

                // 🧠 ML classification AFTER validation
                val (category, confidence) = classifier.classify(msg)

                Log.d("ExpenseAI", "msg=$msg")
                Log.d("ExpenseAI", "category=$category confidence=$confidence")
                Log.d("ExpenseAI", "amount=$amount merchant=$merchant date=$date")

                resultExpense.add(
                    ExpenseItem(
                        merchant = merchant,
                        amount = amount,
                        date = date ?: "NA",
                        category = category,
                        messageId = Random.nextAlphanumericString(10)
                    )
                )
            }

            syncSMSWithDB(resultExpense)

        } catch (e: Exception) {
            Log.e("ExpenseAI", "analyseUsingTfLite failed", e)
        }

        return@coroutineScope resultExpense
    }


    /**
     * Not removing this since it is used for sms scanning will first make it generic then remove
     */
    @SuppressLint("SuspiciousIndentation")
    suspend fun analyseExpenseData(messages: List<String>): List<ExpenseItem> = coroutineScope {
        val tempExpenses = mutableListOf<ExpenseItem>()

        try {
            Log.d("ThreadCheck", "analyseExpenseData() running on: ${Thread.currentThread().name}")

            val batchSize = 10
            val batches = messages.chunked(batchSize)

            // Limit concurrency to 3 parallel Gemini API requests
            val dispatcher = Dispatchers.IO.limitedParallelism(3)

            // Process all batches concurrently
            val deferredResults = batches.mapIndexed { index, batch ->
                async(dispatcher) {
                    try {
                        val safeBatch = batch.map { sanitizeSms(it) }
                        val prompt = """
                        You extract structured expense data from SMS messages. Output JSON only. No explanations.
                        
                        FIELDS:
                        - merchant
                        - amount (number)
                        - date (YYYY-MM-DD if possible)
                        - category (Food | Shopping | Bills | Travel | Other)
                        
                        RULES:
                        - ONLY include records that represent REAL EXPENSES.
                        - EXCLUDE any of the following:
                          - Self transfers
                          - Transfers to own bank accounts
                          - Wallet top-ups
                          - Credit card bill payments
                          - UPI collect requests with no debit
                        - Detect self-transfer using keywords such as:
                          "to self", "own account", "self transfer", "saved beneficiary",
                          your own name, or same sender & receiver bank.
                        - If an SMS is excluded, DO NOT return any object for it.
                        - Use "Other" when uncertain.
                        - No text before or after JSON.
                        - Return an empty array [] if no valid expenses exist.
                        
                        FORMAT:
                        [
                          {"merchant": "", "amount": 0, "date": "", "category": ""}
                        ]
                        
                        SMS:
                        <BEGIN_SMS>
                        ${safeBatch.joinToString("\n---\n")}
                        <END_SMS>
                        """.trimIndent()



                        val requestContent = content { text(prompt) }
                        val response = generativeModel.generateContent(requestContent)

                        response.text?.let { jsonResponse ->
                            val finalJson = jsonResponse.replace(Regex("```json|```"), "").trim()
                            val parsedExpenses: List<ExpenseItem> = gson.fromJson(
                                finalJson,
                                object : TypeToken<List<ExpenseItem>>() {}.type
                            )
                            println("Batch ${index + 1} processed successfully (${parsedExpenses.size} items)")
                            parsedExpenses
                        } ?: emptyList()
                    } catch (e: Exception) {
                        println("Error in batch ${index + 1}: ${e.message}")
                        emptyList<ExpenseItem>()
                    }
                }
            }

            // Wait for all concurrent Gemini calls
            val allExpenses = deferredResults.awaitAll().flatten()

            // Safely update DB and preferences on IO dispatcher
            syncSMSWithDB(allExpenses)

            tempExpenses.addAll(allExpenses)

        } catch (e: Exception) {
            e.printStackTrace()
            println("analyseExpenseData failed: ${e.message}")
        }

        return@coroutineScope tempExpenses
    }

    fun syncSMSWithDB(allExpenses: List<ExpenseItem>) = intent {
        if (allExpenses.isNotEmpty()) {
            val expenseEntities = allExpenses.map { item ->
                ExpenseEntity(
                    description = item.merchant,
                    amount = item.amount,
                    date = item.date,
                    category = item.category,
                    messageId = Random.nextAlphanumericString(10)

                )
            }

            repo.insertAllExpenses(expenseEntities)
            preferences.edit { putLong(LAST_SYNC_TIME, System.currentTimeMillis()) }

            println("💾 Saved ${expenseEntities.size} expenses to DB")
        }
    }

    fun sanitizeSms(text: String): String =
        text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", " ")
            .replace("\r", " ")
            .replace("\t", " ")
            .replace("<", "")
            .replace(">", "")



    fun buildRefinedExpenseData(refinedExpenses: List<ExpenseItem>, isApiCallHappened: Boolean) =  intent{

        val nativeChart = generateNativeChart(refinedExpenses)

        reduce {
            state.copy(
                isLoading = false,
                expenses = refinedExpenses,
                nativeChart = nativeChart
            )
        }



        if (refinedExpenses.isNotEmpty()) {
            reduce {
                state.copy(isRecommendationLoading = true)
            }
            var recommendations: String? = null
            if (isApiCallHappened) {
                recommendations = useExponentialBackoffRetry(
                    shouldRetry = { e ->
                        e is FirebaseAIException && e.message?.contains("Resource exhausted") == true
                    }
                ) {
                    analyzeExpensesAndRecommend(refinedExpenses)
                }
            } else {
                recommendations = preferences.getString(RECOMMENDATION_SAVED_RESPONSE, "")
            }
            reduce {
                state.copy(
                    isRecommendationLoading = false,
                    recommendation = recommendations
                )
            }
            postSideEffect(ExpenseTrackerSideEffect.ShowToast("Expenses extracted.Recommendations ready"))
        } else {
            postSideEffect(ExpenseTrackerSideEffect.ShowToast("Could not extract details from provided data may be its empty."))
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
                preferences.edit { putString(RECOMMENDATION_SAVED_RESPONSE, response.text) }

                return@withContext response.text
            } catch (e: Exception) {
                e.printStackTrace()
                throw e
            }
        }
    }

    private fun aggregateExpenses(expenses: List<ExpenseItem>): List<ExpenseCategoryUI> {
        if (expenses.isEmpty()) return emptyList()

        // 1. Aggregate amounts per category in a single pass
        val categoryTotals = expenses.groupBy { it.category }
            .mapValues { (_, items) -> items.sumOf { it.amount } }

        // 2. Compute overall sum once
        val totalExpense = categoryTotals.values.sum()

        // 3. Build the UI list
        return categoryTotals.map { (category, amount) ->
            ExpenseCategoryUI(
                name = category,
                percentage = amount.toFloat()*100 / totalExpense.toFloat(), // fraction (0–1)
                amount = amount,
                color = androidx.compose.ui.graphics.Color.Red
            )
        }
    }

    private fun generateNativeChart(expenseItem: List<ExpenseItem>): List<ExpenseCategoryUI> {
        if (expenseItem.isEmpty()) return emptyList()
        val aggregatedData = aggregateExpenses(expenseItem)
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

    internal fun fetchGmailData(context: Context) {


        intent {

            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) {
                postSideEffect(ExpenseTrackerSideEffect.RequestLocationPermission)
                return@intent
            }

            var isApiCallHappened = false

            reduce {
                state.copy(isLoading = true)
            }
            try {
                val firstExpense = repo.getAllExpenses().first()
                val lastSyncTime = preferences.getLong(LAST_SYNC_TIME,0L)
                // use case is when current time is not equal to  saved time then get the delta transactions
                if(lastSyncTime != System.currentTimeMillis() && firstExpense.isNotEmpty()){
                    val result = gmailRepo.readMails(context, lastSyncTime)
                    if(result.isNotEmpty())
                        isApiCallHappened = true
                }

                val refinedExpenses: List<ExpenseItem> = (if (firstExpense.isNotEmpty()) {
                    firstExpense.map {
                        ExpenseItem(
                            merchant = it.description,
                            amount = it.amount,
                            date = it.date,
                            category = it.category.toString(),
                            messageId = it.messageId
                        )
                    }
                } else {
                    gmailRepo.readMails(context, 0L)
                    isApiCallHappened = true
                }) as List<ExpenseItem>
               // val result = gmailRepo.readMails(context, 0L)
                buildRefinedExpenseData(refinedExpenses, isApiCallHappened)
            }catch (e: UserRecoverableAuthException) {
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

    fun showGoogleSignInSheet() = intent {
        reduce {
            state.copy(activeSheet = GoogleSignInBottomSheet.GoogleSignIn)
        }
    }

    fun dismissBottomSheet() = intent {
        reduce {
            state.copy(activeSheet = GoogleSignInBottomSheet.None)
        }
    }

    fun skipGmailSignInFlow() = intent{
        reduce {
            state.copy(
                activeSheet = GoogleSignInBottomSheet.GoogleSignInUsingCred
            )
        }
    }

}
