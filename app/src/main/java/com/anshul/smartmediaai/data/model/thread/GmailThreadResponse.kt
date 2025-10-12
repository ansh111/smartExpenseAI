package com.anshul.smartmediaai.data.model.thread

data class GmailThreadResponse(val historyId: String = "",
                               val messages: List<MessagesItem>?,
                               val id: String = "")