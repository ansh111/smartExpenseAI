package com.anshul.smartmediaai.data.repository

import com.anshul.smartmediaai.core.network.ExpenseService
import com.anshul.smartmediaai.data.model.GmailMessageResponse
import com.anshul.smartmediaai.data.model.thread.GmailThreadResponse

class ExpenseRemoteDataSource(private val service: ExpenseService) {

    suspend fun readEmails(authHeader: String, query : String): GmailMessageResponse {
        return service.readEmail(authHeader, query)
    }

    suspend fun readThreads(authHeader: String, threadId: String): GmailThreadResponse {
        return service.readThreads(authHeader, threadId)
    }



}