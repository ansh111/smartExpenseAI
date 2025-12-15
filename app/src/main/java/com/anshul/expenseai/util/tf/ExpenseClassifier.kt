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
    private val labels: Map<Int, String>

    init {
        vocab = loadVocab(context)
        labels = loadLabels(context)

        val options = Interpreter.Options().apply {
            setNumThreads(4)
        }

        interpreter = Interpreter(loadModel(context), options)
    }

    private fun loadModel(context: Context): MappedByteBuffer {
        return context.assets.openFd("expense_classifier.tflite").use {
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
            .readText()

        val obj = JSONObject(json)
        return obj.keys().asSequence().associate { key ->
            obj.getInt(key) to key
        }
    }

    fun tokenize(
        text: String,
        vocab: Map<String, Int>,
        maxLen: Int = 40
    ): IntArray {
        val tokens = text
            .lowercase()
            .replace(Regex("[^a-z0-9 ]"), "")
            .split(" ")
            .filter { it.isNotBlank() }

        val tokenIds = tokens.map { vocab[it] ?: 1 } // 1 = <unk>

        return (tokenIds + List(maxLen) { 0 })
            .take(maxLen)
            .toIntArray()
    }

    fun classify(text: String): Pair<String, Float> {
        val input = Array(1) { tokenize(text, vocab) }
        val output = Array(1) { FloatArray(5) }

        interpreter.run(input, output)

        val probs = output[0]
        val maxIndex = probs.indices.maxByOrNull { probs[it] } ?: -1
        val confidence = probs[maxIndex]

        return labels[maxIndex]!! to confidence
    }

}
