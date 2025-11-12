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
        return String(decodedBytes, Charsets.UTF_8)

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