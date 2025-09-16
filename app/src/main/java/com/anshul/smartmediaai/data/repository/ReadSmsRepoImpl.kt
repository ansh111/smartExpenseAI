package com.anshul.smartmediaai.data.repository

import android.content.ContentResolver
import android.provider.Telephony
import androidx.core.graphics.isSrgb
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReadSmsRepoImpl @Inject constructor(private val contentResolver: ContentResolver): ReadSmsRepo {
    override suspend fun readSms(lastSyncTimestamp: Long): List<String> = withContext(Dispatchers.IO) {
        val messages = mutableListOf<String>()
        val thirtyDaysAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
        var selectionArgs: Array<String> = arrayOf("")
        selectionArgs = if(lastSyncTimestamp == 0L){
            arrayOf("%debit%", "%debited%", "%sent%", thirtyDaysAgo.toString())
        }else{
            arrayOf("%debit%", "%debited%", "%sent%", lastSyncTimestamp.toString())
        }
        val cursor = contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            arrayOf(Telephony.Sms.BODY, Telephony.Sms.ADDRESS, Telephony.Sms.DATE),

            "(${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.BODY} LIKE ? OR ${Telephony.Sms.BODY} LIKE ?) AND ${Telephony.Sms.DATE} >= ?",
            selectionArgs,
            "${Telephony.Sms.DATE} DESC"
        )

        cursor?.use {
            val bodyIndex = it.getColumnIndex(Telephony.Sms.BODY)
            if (bodyIndex != -1) {
                while (it.moveToNext()) {
                    val body = it.getString(bodyIndex)
                    if (body.contains("spent", ignoreCase = true) ||
                        body.contains("debited", ignoreCase = true) ||
                        body.contains("transaction", ignoreCase = true) ||
                        body.contains("INR", ignoreCase = true) ||
                        body.contains("Rs.", ignoreCase = true) ||
                        body.contains("sent", ignoreCase = true)
                    ) {
                        messages.add(body)
                    }
                }
            }
        }

        return@withContext messages.take(100)

    }
}