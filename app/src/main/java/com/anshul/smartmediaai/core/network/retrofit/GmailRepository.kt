package com.anshul.smartmediaai.core.network.retrofit

interface GmailRepository {
    suspend fun  readEmail()
}