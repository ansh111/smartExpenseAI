package com.anshul.smartmediaai.core.network

import androidx.room.Query
import retrofit2.http.GET
import retrofit2.http.Header

interface  ExpenseService {

    @GET("https://gmail.googleapis.com/gmail/v1/users/me/messages")
    suspend fun readEmail(
        @Header("Authorization") authHeader: String
    )

}