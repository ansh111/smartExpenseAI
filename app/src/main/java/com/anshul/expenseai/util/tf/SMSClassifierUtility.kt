package com.anshul.expenseai.util.tf

object SMSClassifierUtility {

    fun isSelfTransfer(text: String): Boolean {
        val keywords = listOf(
            "to self",
            "self transfer",
            "own account",
            "saved beneficiary"
        )

        if (keywords.any { text.contains(it) }) return true
       // if (userName != null && text.contains(userName.lowercase())) return true

        // same bank transfer heuristic
        if (text.contains("from hdfc") && text.contains("to hdfc")) return true

        return false
    }

    fun extractDate(text: String): String? {
        val regex = Regex("(\\d{2})/(\\d{2})/(\\d{2,4})")
        val match = regex.find(text) ?: return null

        val day = match.groupValues[1]
        val month = match.groupValues[2]
        var year = match.groupValues[3]

        if (year.length == 2) year = "20$year"

        return "$year-$month-$day"
    }

    fun extractMerchant(text: String): String? {
        val regex = Regex(
            "(?i)(?:to|at|via)\\s+([a-z0-9][a-z0-9@._\\-\\s']+?)\\s+(on|ref|txn|using|$)"
        )

        return regex.find(text)
            ?.groupValues
            ?.get(1)
            ?.trim()
    }

    fun extractAmount(text: String): Double? {
        val regex = Regex("(?i)(rs\\.?|inr)\\s*([0-9]+(?:\\.[0-9]{1,2})?)")
        return regex.find(text)
            ?.groupValues
            ?.get(2)
            ?.toDoubleOrNull()
    }


    fun isWalletTopUp(text: String): Boolean =
        listOf("wallet", "paytm topup", "phonepe top up", "amazon pay balance")
            .any { text.contains(it) }


    fun isCollectRequest(text: String): Boolean =
        listOf("collect request", "request sent", "payment request")
            .any { text.contains(it) }


}