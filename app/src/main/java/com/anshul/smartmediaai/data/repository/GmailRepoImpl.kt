package com.anshul.smartmediaai.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.anshul.smartmediaai.data.entities.ExpenseEntity
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseTrackerViewModel.Companion.GMAIL_SCOPE
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseTrackerViewModel.Companion.LAST_SYNC_TIME
import com.anshul.smartmediaai.ui.compose.expensetracker.ExpenseTrackerViewModel.Companion.TAG
import com.anshul.smartmediaai.ui.compose.expensetracker.state.ExpenseItem
import com.anshul.smartmediaai.util.HelperFunctions.decodeString
import com.anshul.smartmediaai.util.HelperFunctions.extractPlainTextFromHtml
import com.anshul.smartmediaai.util.constants.ExpenseConstant.EMAIL_PREFS
import com.anshul.smartmediaai.util.constants.ExpenseConstant.EXPENSE_SHARED_PREFS
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.common.reflect.TypeToken
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.gson.Gson
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

class GmailRepoImpl @Inject constructor(val repo: ExpenseRepo, val gson: Gson): GmailRepo {
    override suspend fun readMails(context: Context, lastSyncTimestamp: Long): List<ExpenseItem> = withContext(Dispatchers.IO) {
            val sp = context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE)

            val token = GoogleAuthUtil.getToken(context,
                sp.getString(EMAIL_PREFS,"").toString(), GMAIL_SCOPE)
            Log.d(TAG, "Access Token: $token")
            val bearerToken = "Bearer $token"
        val response = if (lastSyncTimestamp == 0L) {
            repo.readEmails(
                bearerToken,
                "(\"debited from account\" OR \"withdrawn from account\") -SIP -EMI -AutoPay -mutual -insurance newer_than:30d"
            )
        } else {
            repo.readEmails(
                bearerToken,
                "(\"debited from account\" OR \"withdrawn from account\") -SIP -EMI -AutoPay -mutual -insurance after:${lastSyncTimestamp} before:${System.currentTimeMillis()}"
            )
        }
           // val response = repo.readEmails(bearerToken, "(\"debited from account\" OR \"withdrawn from account\") -SIP -EMI -AutoPay -mutual -insurance newer_than:30d")
            val allDecodedTexts = withContext(Dispatchers.Default) {
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
        val refinedExpenses  = analyseExpenseData(allDecodedTexts,sp)
        return@withContext refinedExpenses
    }

    @SuppressLint("SuspiciousIndentation")
    suspend fun analyseExpenseData(messages: List<String>, sp: SharedPreferences): List<ExpenseItem> {
        val tempExpenses = mutableListOf<ExpenseItem>()
        try {
            Log.d("ThreadCheck", "loadUserData() on thread: ${Thread.currentThread().name}")
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
                        category = item.category,
                        timestamp = System.currentTimeMillis()
                    )

                }
                repo.insertAllExpenses(expenseEntities)
                sp.edit { putLong(LAST_SYNC_TIME, System.currentTimeMillis()) }

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


}