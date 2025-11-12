package com.anshul.expenseai.data.repository

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.anshul.expenseai.data.entities.ExpenseEntity
import com.anshul.expenseai.data.model.thread.DecodeMessages
import com.anshul.expenseai.ui.compose.expensetracker.state.ExpenseItem
import com.anshul.expenseai.util.HelperFunctions.decodeString
import com.anshul.expenseai.util.HelperFunctions.extractPlainTextFromHtml
import com.anshul.expenseai.util.constants.ExpenseConstant.EMAIL_PREFS
import com.anshul.expenseai.util.constants.ExpenseConstant.EXPENSE_SHARED_PREFS
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
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.time.measureTime

class GmailRepoImpl @Inject constructor(val repo: ExpenseRepo, val gson: Gson) : GmailRepo {

    data class ExpenseResult(val result: List<ExpenseItem>, val time: String)
    companion object {
        const val TAG = "GmailRepoImpl"
        const val LAST_SYNC_TIME = "last_sync_time"
        const val GMAIL_SCOPE = "oauth2:https://www.googleapis.com/auth/gmail.readonly"
    }
    override suspend fun readMails(context: Context, lastSyncTimestamp: Long): List<ExpenseItem> =
        withContext(Dispatchers.IO) {
            val sp = context.getSharedPreferences(EXPENSE_SHARED_PREFS, Context.MODE_PRIVATE)

            val token = GoogleAuthUtil.getToken(
                context,
                sp.getString(EMAIL_PREFS, "").toString(), GMAIL_SCOPE
            )
            Log.d(TAG, "Access Token: $token")
            val bearerToken = "Bearer $token"
            val response = if (lastSyncTimestamp == 0L) {
                val q = """
                    (\"debited from account\" OR \"withdrawn from account\") OR
(category:updates
(subject:debit OR subject:transaction)
(\"has been debited\" OR \"withdrawn from account\"))
-SIP -EMI -AutoPay -mutual -insurance
newer_than:30d
""".trimIndent()
                repo.readEmails(
                    bearerToken,
                    q
                )
            } else {
                val q = """
                   (\"debited from account\" OR \"withdrawn from account\") OR
(category:updates
(subject:debit OR subject:transaction)
(\"has been debited\" OR \"withdrawn from account\"))
-SIP -EMI -AutoPay -mutual -insurance
                after:${lastSyncTimestamp / 1000} before:${System.currentTimeMillis() / 1000}
                """.trimIndent()
                repo.readEmails(
                    bearerToken,
                    q
                )
            }
            // val response = repo.readEmails(bearerToken, "(\"debited from account\" OR \"withdrawn from account\") -SIP -EMI -AutoPay -mutual -insurance newer_than:30d")
            val allDecodedTexts: List<DecodeMessages> = withContext(Dispatchers.Default) {
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
                                        DecodeMessages(
                                            messageId = payload.id,
                                            message = extractPlainTextFromHtml(
                                                decodeString(
                                                    payloadParts.body.data
                                                )
                                            )
                                        )
                                        //  extractPlainTextFromHtml(decodeString(payloadParts.body.data))
                                    )
                                } else {
                                    payloadParts.parts.map {
                                        DecodeMessages(
                                            messageId = payload.id,
                                            message = extractPlainTextFromHtml(decodeString(it.body.data))
                                        )
                                        //  extractPlainTextFromHtml(decodeString(it.body.data))
                                    }
                                }
                            } ?: emptyList<DecodeMessages>()
                        }
                    }?.awaitAll()?.flatten()
            } ?: emptyList()
            Log.d("Anshul", "Fetched ${allDecodedTexts.size} messages")

            val refinedExpenses = analyseExpenseData1(allDecodedTexts, sp)
            Log.d("AnshulNigam", refinedExpenses.time)
            return@withContext refinedExpenses.result
        }


    @SuppressLint("SuspiciousIndentation")
    suspend fun analyseExpenseData1(
        messages: List<DecodeMessages>,
        sp: SharedPreferences
    ): ExpenseResult = coroutineScope {
        val tempExpenses = mutableListOf<ExpenseItem>()
        val time = measureTime {

            try {
                Log.d(
                    "ThreadCheck",
                    "analyseExpenseData() running on: ${Thread.currentThread().name}"
                )

                val generativeModel = Firebase.ai(backend = GenerativeBackend.vertexAI())
                    .generativeModel("gemini-2.5-pro")

                val batchSize = 10
                val batches = messages.chunked(batchSize)

                // Limit concurrency to 3 parallel Gemini API requests
                val dispatcher = Dispatchers.IO.limitedParallelism(3)

                // Process all batches concurrently
                val deferredResults = batches.mapIndexed { index, batch ->
                    async(dispatcher) {
                        try {
                            val prompt = """
            You are a data extraction assistant.
            Extract expense details from each message below.
            For each message, read its "text" and use the provided "messageId" to return consistent results.
            
            For each message, identify:
            - merchant
            - amount (as number)
            - date (YYYY-MM-DD if possible)
            - category (Food, Shopping, Bills, Travel, Other)
            If not found, use "N/A".
            
            Return ONLY a valid JSON array of objects.
            Each object must have exactly these keys:
            "messageId", "merchant", "amount", "date", "category".
            
            Messages:
            [
            ${
                                batch.joinToString(",\n") { msg ->
                                    """{"messageId": "${msg.messageId}", "text": "${
                                        msg.message.replace(
                                            "\"",
                                            "\\\""
                                        )
                                    }"}"""
                                }
                            }
            ]
            """.trimIndent()

                            val requestContent = content { text(prompt) }
                            val response = generativeModel.generateContent(requestContent)

                            response.text?.let { jsonResponse ->
                                val finalJson =
                                    jsonResponse.replace(Regex("```json|```"), "").trim()
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
                withContext(Dispatchers.IO) {
                    if (allExpenses.isNotEmpty()) {
                        val expenseEntities = allExpenses.map { item ->
                            ExpenseEntity(
                                description = item.merchant,
                                amount = item.amount,
                                date = item.date,
                                category = item.category,
                                messageId = item.messageId
                            )
                        }

                        repo.insertAllExpenses(expenseEntities)
                        sp.edit { putLong(LAST_SYNC_TIME, System.currentTimeMillis()) }

                        println("Saved ${expenseEntities.size} expenses to DB")
                    }
                }

                tempExpenses.addAll(allExpenses)

            } catch (e: Exception) {
                e.printStackTrace()
                println("analyseExpenseData failed: ${e.message}")
            }
        }

        return@coroutineScope ExpenseResult(tempExpenses, time.toString())
    }


}