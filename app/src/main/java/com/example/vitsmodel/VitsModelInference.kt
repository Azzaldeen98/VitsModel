//package com.example.myapplication
//
//
import ai.onnxruntime.*
import android.annotation.SuppressLint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.LongBuffer
import java.util.Collections

class VitsModelInference(modelPath: String) {
    private val env: OrtEnvironment = OrtEnvironment.getEnvironment()

    private val session: OrtSession = env.createSession(modelPath, OrtSession.SessionOptions())

    // دالة لإجراء الاستدلال
    @SuppressLint("SuspiciousIndentation")
    suspend fun runInference(inputTextTokens: IntArray): FloatArray {
//        if (!::session.isInitialized) {
//            throw OrtException("OrtSession is not initialized.")
//        }

        // تحويل IntArray إلى LongArray
        val inputTextTokensLong = LongArray(inputTextTokens.size) { inputTextTokens[it].toLong() }

        // إعداد tensor الإدخال
        val inputShape = longArrayOf(1, inputTextTokensLong.size.toLong()) // حجم الدفعة 1
        val inputTensor = OnnxTensor.createTensor(env, LongBuffer.wrap(inputTextTokensLong), inputShape)

        return withContext(Dispatchers.IO) {
            inputTensor.use {
                // إجراء الاستدلال

                val output = session.run(Collections.singletonMap("input", inputTensor))
                output.use {
                    val outputTensor = output[0].value as Array<FloatArray>
                    outputTensor[0] // إرجاع النتيجة
                }}
        }

    }

    // دالة لإغلاق الجلسة والبيئة
    fun close() {
        session.close()
        env.close()
    }
}