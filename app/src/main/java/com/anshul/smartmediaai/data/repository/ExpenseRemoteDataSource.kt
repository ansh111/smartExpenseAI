package com.anshul.smartmediaai.data.repository

import com.anshul.smartmediaai.core.network.ExpenseService

class ExpenseRemoteDataSource(private val service: ExpenseService) {

    suspend fun readEmails(authHeader: String, url : String) {
        return service.readEmail(authHeader, url)
    }

}