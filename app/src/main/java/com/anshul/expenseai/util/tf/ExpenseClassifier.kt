package com.anshul.expenseai.util.tf

import android.content.Context
import org.json.JSONObject
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ExpenseClassifier(context: Context) {

    private val interpreter: Interpreter
    private val vocab: Map<String, Int>
    private val uiMapping: Map<Int, String>

    init {
        vocab = loadVocab(context)
        uiMapping = loadLabels(context)

        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }

        interpreter = Interpreter(loadModel(context), options)
    }

    private fun loadModel(context: Context): MappedByteBuffer {
        return context.assets.openFd("expense_categorizer.tflite").use {
            FileInputStream(it.fileDescriptor).channel.map(
                FileChannel.MapMode.READ_ONLY,
                it.startOffset,
                it.declaredLength
            )
        }
    }

    fun loadVocab(context: Context): Map<String, Int> {
        return context.assets.open("vocab.txt")
            .bufferedReader()
            .readLines()
            .mapIndexed { index, token -> token to index }
            .toMap()
    }

    fun loadLabels(context: Context): Map<Int, String> {
        val json = context.assets.open("labels.json")
            .bufferedReader()
            .use { it.readText() }

        val root = JSONObject(json)
        val uiMapping = root.getJSONObject("ui_mapping")

        return uiMapping.keys().asSequence().associate { key ->
            key.toInt() to uiMapping.getString(key)
        }
    }

    fun tokenize(
        text: String,
        vocab: Map<String, Int>,
        maxLen: Int = 40
    ): IntArray {

        val boosted = boostMerchants(text)

        val tokens = boosted
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), " ")
            .split("\\s+".toRegex())
            .filter { it.isNotBlank() }

        val tokenIds = tokens.map { vocab[it] ?: 1 } // 1 = <unk>

        return (tokenIds + List(maxLen) { 0 })
            .take(maxLen)
            .toIntArray()
    }

    private fun boostMerchants(text: String): String {
        val t = text.lowercase()
        return when {
            t.contains("swiggy") -> "swiggy swiggy swiggy $t"
            t.contains("zomato") -> "zomato zomato zomato $t"
            t.contains("ekart") -> "ekart ekart $t"
            t.contains("amazon") -> "amazon amazon $t"
            t.contains("flipkart") -> "flipkart flipkart $t"
            t.contains("airtel") -> "airtel airtel $t"
            t.contains("jio") -> "jio jio $t"
            else -> t
        }
    }


    fun classify(text: String): Pair<String, Float> {
        val input = Array(1) { tokenize(text, vocab) }

        // 2️⃣ Model has 8 output classes
        val output = Array(1) { FloatArray(8) }

        interpreter.run(input, output)

        val probs = output[0]

        // 3️⃣ Argmax
        val maxIdx = probs.indices.maxBy { probs[it] }
        val confidence = probs[maxIdx]

        // 4️⃣ Map to UI category (safe)
        val mappedCategory = uiMapping[maxIdx] ?: "Other"

        // 5️⃣ Confidence gating
        val finalCategory =
            if (confidence < 0.55f) "Other" else mappedCategory

        return finalCategory to confidence
    }


}
