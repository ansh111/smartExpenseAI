package com.anshul.expenseai.util

import android.util.Base64
import kotlinx.coroutines.delay
import org.jsoup.Jsoup
import org.jsoup.safety.Safelist

object HelperFunctions {

    fun extractPlainTextFromHtml(html: String): String {
        // Remove scripts/styles and convert to readable text
        val doc = Jsoup.parse(html)
        doc.select("script, style, footer, img, nav").remove() // remove noise
        return Jsoup.clean(doc.body().html(), Safelist.none())
            .replace(Regex("\\s+"), " ") // collapse whitespace
            .trim()
    }


    fun decodeString(encoded: String): String {
        val normalized = encoded.replace('-', '+').replace('_', '/').let {
            val mod = it.length % 4
            if (mod == 0) it else it + "=".repeat(4 - mod)
        }
        val decodedBytes = Base64.decode(normalized, Base64.DEFAULT)
        val message = String(decodedBytes, Charsets.UTF_8)
        return cleanSmsForLLM(message)

    }

    fun cleanSmsForLLM(raw: String): String {
        // Step 1: Normalize whitespace
        var t = raw.replace("\\s+".toRegex(), " ").trim()

        // Step 2: Remove promotional / disclaimer / footer patterns
        t = t
            .replace("(?i)warm regards.*".toRegex(), "")
            .replace("(?i)safe banking tip.*".toRegex(), "")
            .replace("(?i)exclusive offer.*".toRegex(), "")
            .replace("(?i)please note.*".toRegex(), "")
            .replace("(?i)cashback.*".toRegex(), "")
            .replace("(?i)connect with us.*".toRegex(), "")
            .replace("(?i)to unsubscribe.*".toRegex(), "")
            .replace("(?i)click here.*".toRegex(), "")
            .replace("(?i)this emailer.*".toRegex(), "")
            .replace("(?i)report at.*".toRegex(), "")
            .replace("(?i)visit .*".toRegex(), "")

        // Step 3: Keep only substring that contains the "amount + merchant + date" structure
        // so LLM doesn't see useless tokens
        val regex = Regex("(Rs\\.? ?[0-9,.]+.*?on [0-9\\-/]{6,})", RegexOption.IGNORE_CASE)
        val match = regex.find(t)

        return match?.value?.trim() ?: raw.trim()     // fallback if SMS pattern is different
    }



    /**
     * Retries a suspendable [action] with exponential backoff when a transient error occurs.
     *
     * @param maxAttempts Maximum number of retry attempts.
     * @param initialDelay Initial delay between retries in milliseconds.
     * @param factor Multiplier for exponential backoff (default 2x).
     * @param shouldRetry Function to determine if the exception is retryable.
     * @param action The suspend function to execute.
     */
    suspend fun <T> useExponentialBackoffRetry(
        maxAttempts: Int = 5,
        initialDelay: Long = 1000L,
        factor: Double = 2.0,
        shouldRetry: (Throwable) -> Boolean = { false },
        action: suspend () -> T
    ): T {
        var attempt = 0
        var delayMs = initialDelay

        while (true) {
            try {
                return action()
            } catch (e: Throwable) {
                if (attempt >= maxAttempts || !shouldRetry(e)) {
                    throw e
                }
                delay(delayMs)
                delayMs = (delayMs * factor).toLong()
                attempt++
            }
        }
    }


}