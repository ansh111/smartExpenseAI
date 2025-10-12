package com.anshul.smartmediaai.core.network

import com.anshul.smartmediaai.data.model.GmailMessageResponse
import com.anshul.smartmediaai.data.model.thread.GmailThreadResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query


interface  ExpenseService {

    @GET("https://gmail.googleapis.com/gmail/v1/users/me/messages")
    suspend fun readEmail(
        @Header("Authorization") authHeader: String,
        @Query("q")  q: String
    ): GmailMessageResponse

    @GET("https://gmail.googleapis.com/gmail/v1/users/me/threads/{threadId}")
    suspend fun readThreads(
        @Header("Authorization") authHeader: String,
        @Path("threadId") threadId: String
    ): GmailThreadResponse

}