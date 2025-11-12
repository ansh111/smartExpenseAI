package com.anshul.expenseai.data.repository

interface ReadSmsRepo {
    suspend fun readSms(lastSyncTimestamp: Long):  List<String>
}