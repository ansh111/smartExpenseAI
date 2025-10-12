package com.anshul.smartmediaai.data.model

data class GmailMessageResponse(val nextPageToken: String = "",
                                val messages: List<MessagesItem>?,
                                val resultSizeEstimate: Int = 0)