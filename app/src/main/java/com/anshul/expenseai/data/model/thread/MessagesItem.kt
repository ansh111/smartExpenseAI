package com.anshul.expenseai.data.model.thread

data class MessagesItem(val threadId: String = "",
                        val snippet: String = "",
                        val labelIds: List<String>?,
                        val payload: Payload,
                        val historyId: String = "",
                        val id: String = "",
                        val sizeEstimate: Int = 0,
                        val internalDate: String = "")