package com.anshul.expenseai.data.model.thread

data class Payload(val headers: List<HeadersItem>?,
                   val filename: String = "",
                   val partId: String = "",
                   val mimeType: String = "",
                   val body: Body,
                   val parts: List<Payload>? )