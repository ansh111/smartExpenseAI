package com.anshul.smartmediaai.core.network

import androidx.room.Query
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Url

interface  ExpenseService {

    @GET
    suspend fun readEmail(
        @Header("Authorization") authHeader: String,
        @Url url: String
    )

}