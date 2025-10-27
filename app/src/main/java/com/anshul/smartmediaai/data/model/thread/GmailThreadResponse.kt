package com.anshul.smartmediaai.data.model.thread

data class GmailThreadResponse(val historyId: String = "",
                               val messages: List<MessagesItem>?,
                               val id: String = "")


data class DecodeMessages (val messageId: String ="", val message: String ="")