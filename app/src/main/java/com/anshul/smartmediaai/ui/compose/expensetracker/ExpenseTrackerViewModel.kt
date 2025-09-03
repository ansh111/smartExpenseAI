package com.anshul.smartmediaai.ui.compose.expensetracker

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.anshul.smartmediaai.data.entities.ExpenseEntity
import com.anshul.smartmediaai.data.repository.ExpenseRepo
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseItem
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerState
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
import org.orbitmvi.orbit.Container
import org.orbitmvi.orbit.ContainerHost
import org.orbitmvi.orbit.viewmodel.container
import javax.inject.Inject



@HiltViewModel
class ExpenseTrackerViewModel @Inject constructor(
    @ApplicationContext private val context: Context ,
    private val gson: Gson,
    private val repo: ExpenseRepo
) : ContainerHost<ExpenseTrackerState, ExpenseTrackerSideEffect>, ViewModel() {

    override val container: Container<ExpenseTrackerState, ExpenseTrackerSideEffect> =
        container(ExpenseTrackerState())

    private val contentResolver: ContentResolver = context.contentResolver

    private fun checkSmsPermission() = intent {
        val hasPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_SMS
        ) == PackageManager.PERMISSION_GRANTED
        reduce { state.copy(permissionGranted = hasPermission) }
        if (!hasPermission) {
            postSideEffect(ExpenseTrackerSideEffect.RequestSmsPermission)
        }
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
        if (!state.permissionGranted) {
            checkSmsPermission() // Re-trigger permission check if not granted
            postSideEffect(ExpenseTrackerSideEffect.ShowToast("SMS Permission not granted."))
            return@intent
        }

        reduce { state.copy(isLoading = true, errorMessage = null, expenses = emptyList()) }

        try {
            val smsMessages = readSmsMessages()
            if (smsMessages.isEmpty()) {
                reduce { state.copy(isLoading = false) }
                postSideEffect(ExpenseTrackerSideEffect.ShowToast("No relevant SMS found."))
                return@intent
            }

            val refinedExpenses = mutableListOf<ExpenseItem>()
            val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI()).generativeModel("gemini-2.0-flash") // Or your preferred model
            val batchSize = 10
            for ((index,batch) in smsMessages.chunked(batchSize).withIndex()) {

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
                val response = generativeModel.generateContent(requestContent) // Or generateContentStream

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
                         refinedExpenses.addAll(parsedExpenses)
                        println("Batch ${index + 1} processed: $finalJson")
                       // println("AI Response for SMS '$sms': $jsonResponse") // Log for debugging
                    } catch (e: Exception) {
                        println("Error parsing AI response: ${e.message}")
                    }
                }
            }

            reduce {
                state.copy(
                    isLoading = false,
                    expenses = refinedExpenses // Add the parsed expenses here
                )
            }
            val expenseEntities = refinedExpenses.map { item ->
                ExpenseEntity(
                    description = item.merchant,
                    amount = item.amount,
                    date = item.date,
                    category = item.category
                )

            }
            repo.insertAllExpenses(expenseEntities)

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

    private suspend fun readSmsMessages(): List<String> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<String>()
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000) // 30 days in ms

        val cursor = contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE),
            "(${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.BODY} LIKE ?) AND ${Telephony.Sms.DATE} >= ?",
            arrayOf("%debit%", "%debited%","%sent%", thirtyDaysAgo.toString()),
            "${Telephony.Sms.DATE} DESC" // Sort latest first
        )


        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            //val addressIndex = it.getColumnIndex(Telephony.Sms.ADDRESS) // If you need sender

            if (bodyIndex != -1) { // Ensure column exists
                while (it.moveToNext()) {
                    val body = it.getString(bodyIndex)
                    // You might want more sophisticated filtering here based on keywords
                    if (body.contains("spent", ignoreCase = true) ||
                        body.contains("debited", ignoreCase = true) ||
                        body.contains("transaction", ignoreCase = true) ||
                        body.contains("INR", ignoreCase = true) || // Common currency symbol
                        body.contains("Rs.", ignoreCase = true) ||
                        body.contains("sent",ignoreCase = true)
                    ) {
                        messages.add(body)
                    }
                }
            }
        }
        return@withContext messages.take(100) // Process a smaller batch first for testing
    }

    private suspend fun analyzeExpensesAndRecommend(expenses: List<ExpenseItem>): String? {
        return withContext(Dispatchers.IO) {
            try {
                val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI())
                    .generativeModel("gemini-2.0-flash") // Use PRO model for reasoning

                val expensesJson = gson.toJson(expenses)

                val prompt = """
                You are an AI financial assistant.
                The user’s recent expenses are as follows (JSON format):
                $expensesJson

                Tasks:
                1. Identify spending patterns (high shopping, food, travel, bills, etc).
                2. For shopping-related expenses: recommend cheaper alternatives, online platforms, or stores compare the cost also
                3. For miscellaneous expenses: suggest suitable equity mutual funds for disciplined investing.
                4. Provide actionable recommendations in a friendly, concise format.
                5. Recommendation should be divided into various actionables 
                6. If possible define the exact mutual fund to invest along with an past performance i.e CAGR also add the other expense values and compare its value growth wrt recommended fund 
                Overall restrict it to 50 words only
            """.trimIndent()

                val requestContent = content { text(prompt) }
                val response = generativeModel.generateContent(requestContent)

                return@withContext response.text
            } catch (e: Exception) {
                e.printStackTrace()
                return@withContext null
            }
        }
    }

}
