package com.anshul.expenseai.util.tf

import com.anshul.expenseai.ui.compose.expensetracker.UserOnboardingInfo

object SMSClassifierUtility {

    fun isSelfTransfer(text: String): Boolean {
        val keywords = listOf(
            "to self",
            "self transfer",
            "own account",
            "saved beneficiary"
        )

        if (keywords.any { text.contains(it) }) return true

        // same bank transfer heuristic
        if (text.contains("from hdfc") && text.contains("to hdfc")) return true

        return false
    }

    fun extractDate(text: String): String? {
        val regex = Regex("""(\d{2})[/. -](\d{2})[/. -](\d{2,4})""")
        val match = regex.find(text) ?: return null

        val day = match.groupValues[1]
        val month = match.groupValues[2]
        var year = match.groupValues[3]

        if (year.length == 2) year = "20$year"

        return "$year-$month-$day"
    }

    fun extractMerchant(text: String): String? {

        // 1️⃣ Card spend pattern: "spent on <merchant> Credit Card"
        val cardSpentRegex = Regex(
            "(?i)spent\\s+on\\s+" +
                    "([a-z][a-z0-9\\s'.&\\-]{2,}?)" +
                    "\\s+(?:credit\\s+card|debit\\s+card)"
        )

        cardSpentRegex.find(text)?.let {
            return it.groupValues[1].trim()
        }

        // 2️⃣ Generic debit-style merchant extraction
        val debitRegex = Regex(
            "(?i)(?:to|at|via)\\s+" +
                    "([a-z][a-z0-9\\s'.&\\-]{2,}?)" +
                    "\\s*(?:\\.\\s*|,\\s*|on\\s|ref\\s|txn\\s|upi[:\\s]|imps|neft|rtgs|$)"
        )

        debitRegex.find(text)?.let {
            return it.groupValues[1].trim().removeSuffix(".")
        }

        // 3️⃣ Credit-style (salary / refunds)
        val creditedRegex = Regex(
            "(?i)([A-Z][A-Z\\s]{2,})\\s+credited"
        )

        creditedRegex.find(text)?.let {
            return it.groupValues[1].trim()
        }

        return null
    }




    fun extractAmount(text: String): Double? {

        // 1️⃣ INR / Rs prefixed amounts (Rs. 1200, INR 500)
        val prefixedRegex =
            Regex("(?i)(rs\\.?|inr)\\s*([0-9]{1,9}(?:\\.[0-9]{1,2})?)")

        prefixedRegex.find(text)?.let {
            return it.groupValues[2].toDoubleOrNull()
        }

        // 2️⃣ Bare amount before keywords (203.0 spent / 450 paid / 1200 used)
        val bareAmountRegex =
            Regex("(?i)([0-9]{1,9}(?:\\.[0-9]{1,2})?)\\s*(spent|paid|used|debited|charged)")

        bareAmountRegex.find(text)?.let {
            return it.groupValues[1].toDoubleOrNull()
        }

        // 3️⃣ Fallback: amount after keywords (spent 203.0)
        val postKeywordRegex =
            Regex("(?i)(spent|paid|used|debited|charged)\\s*(rs\\.?|inr)?\\s*([0-9]{1,9}(?:\\.[0-9]{1,2})?)")

        postKeywordRegex.find(text)?.let {
            return it.groupValues[3].toDoubleOrNull()
        }

        return null
    }



    fun isWalletTopUp(text: String): Boolean =
        listOf("wallet", "paytm topup", "phonepe top up", "amazon pay balance")
            .any { text.contains(it) }


    fun isCollectRequest(text: String): Boolean =
        listOf("collect request", "request sent", "payment request")
            .any { text.contains(it) }


    fun isSelfMerchant(
        text: String,
        userInfo: UserOnboardingInfo
    ): Boolean {
        val normalizedText = text.lowercase()

        // 1️⃣ Name check (full name + split parts)
        userInfo.fullName
            .lowercase()
            .split(" ")
            ?.filter { it.length > 2 } // avoid false positives like "an", "ra"
            ?.forEach { namePart ->
                if (normalizedText.contains(namePart)) {
                    return true
                }
            }

        // 2️⃣ Phone number check (full + last 4 digits)
        userInfo.phoneNumber.let { phone ->
            val cleanPhone = phone.filter { it.isDigit() }

            if (cleanPhone.length >= 10) {
                val last4 = cleanPhone.takeLast(4)

                if (
                    normalizedText.contains(cleanPhone) ||
                    normalizedText.contains(last4)
                ) {
                    return true
                }
            }
        }

        // 3️⃣ Email check (full + username part)
        userInfo.email.lowercase()?.let { email ->
            val emailUserName = email.substringBefore("@")

            if (
                normalizedText.contains(email) ||
                normalizedText.contains(emailUserName)
            ) {
                return true
            }
        }

        return false
    }


}