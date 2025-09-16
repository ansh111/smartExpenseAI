package com.anshul.smartmediaai.data.repository

interface ReadSmsRepo {
    suspend fun readSms(lastSyncTimestamp: Long):  List<String>
}