package com.anshul.smartmediaai.ui.compose.expensetracker

import android.Manifest
import android.content.ContentResolver
import android.content.Context
import android.content.pm.PackageManager
import android.provider.Telephony
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseItem
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerSideEffect
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseTrackerState
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
    private val gson: Gson
) : ContainerHost<ExpenseTrackerState, ExpenseTrackerSideEffect>, ViewModel() {

    override val container: Container<ExpenseTrackerState, ExpenseTrackerSideEffect> =
        container(ExpenseTrackerState())

    private val contentResolver: ContentResolver = context.contentResolver

    fun checkSmsPermission() = intent {
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
            postSideEffect(ExpenseTrackerSideEffect.ShowToast("SMS Permission not granted."))
            checkSmsPermission() // Re-trigger permission check if not granted
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
            // Initialize your Generative Model (similar to VideoSummarisationViewModel)
            val generativeModel = Firebase.ai(backend = GenerativeBackend.googleAI()).generativeModel("gemini-1.5-flash") // Or your preferred model

            for (sms in smsMessages) {
                // TODO: Craft a good prompt
                val prompt = """
                    Extract expense details from this SMS.
                    Identify the merchant, amount, date, and categorize the expense (e.g., Food, Shopping, Bills, Travel, Other).
                    If you cannot determine a value, use "N/A".
                    Format the output as a JSON object with keys: "merchant", "amount" (as a number), "date" (YYYY-MM-DD if possible, else original), "category".

                    SMS:
                    $sms
                """.trimIndent()

                val requestContent = content { text(prompt) }
                val response = generativeModel.generateContent(requestContent) // Or generateContentStream

                response.text?.let { jsonResponse ->
                    // TODO: Parse the JSON response from Gemini into your ExpenseItem data class
                    // You might need a JSON parsing library like kotlinx.serialization or Gson
                    // For simplicity, this is a placeholder
                    try {
                        // Example (very basic, needs proper JSON parsing and error handling):
                        val jsonResponse = jsonResponse.replace(Regex("```json|```"), "").trim()
                         val parsedExpense = gson.fromJson(jsonResponse, ExpenseItem::class.java)
                         refinedExpenses.add(parsedExpense)
                        println("AI Response for SMS '$sms': $jsonResponse") // Log for debugging
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
            if (refinedExpenses.isNotEmpty()) {
                postSideEffect(ExpenseTrackerSideEffect.ShowToast("Expenses extracted."))
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
        val cursor = contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE),
            "${Telephony.Sms.ADDRESS} LIKE ? OR ${Telephony.Sms.ADDRESS} LIKE ?", // Example: Filter by bank sender IDs
            arrayOf("%-MYBANK", "%-URBANK"), // Replace with actual sender IDs
            "${Telephony.Sms.DATE} DESC LIMIT 50" // Get recent 50 messages, adjust as needed
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
                        body.contains("Rs.", ignoreCase = true)
                    ) {
                        messages.add(body)
                    }
                }
            }
        }
        return@withContext messages.take(10) // Process a smaller batch first for testing
    }

    // You would need a robust JSON parsing function here
    // private fun parseExpenseFromJson(jsonString: String): ExpenseItem { ... }
}
