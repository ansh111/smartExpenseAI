package com.anshul.smartmediaai.util

import android.util.Base64
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

}