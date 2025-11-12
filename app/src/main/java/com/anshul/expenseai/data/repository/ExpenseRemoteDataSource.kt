package com.anshul.expenseai.data.repository

import com.anshul.expenseai.core.network.ExpenseService
import com.anshul.expenseai.data.model.GmailMessageResponse
import com.anshul.expenseai.data.model.thread.GmailThreadResponse

class ExpenseRemoteDataSource(private val service: ExpenseService) {

    suspend fun readEmails(authHeader: String, query : String): GmailMessageResponse {
        return service.readEmail(authHeader, query)
    }

    suspend fun readThreads(authHeader: String, threadId: String): GmailThreadResponse {
        return service.readThreads(authHeader, threadId)
    }



}