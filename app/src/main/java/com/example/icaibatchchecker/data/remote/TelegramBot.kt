package com.example.icaibatchchecker.data.remote

import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

object TelegramBot {

    private const val BASE_URL = "https://api.telegram.org/bot"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .build()

    fun sendMessage(
        botToken: String,
        chatId: String,
        message: String
    ): Result<Unit> {
        return try {
            val url = "$BASE_URL$botToken/sendMessage"

            val body = FormBody.Builder()
                .add("chat_id", chatId)
                .add("text", message)
                .add("parse_mode", "HTML")
                .build()

            val request = Request.Builder()
                .url(url)
                .post(body)
                .build()

            val response = client.newCall(request).execute()
            val responseBody = response.body?.string() ?: ""

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("Telegram API error: $responseBody"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun sendTestMessage(botToken: String, chatId: String): Result<Unit> {
        val message = """
            |<b>ICAI Batch Checker</b>
            |
            |Test message received! 
            |The app is configured correctly.
        """.trimMargin()
        return sendMessage(botToken, chatId, message)
    }

    fun sendBatchAlert(
        botToken: String,
        chatId: String,
        courseName: String,
        batchName: String,
        seats: Int
    ): Result<Unit> {
        val message = """
            |<b>🚨 ICAI Batch Alert!</b>
            |
            |Course: <code>$courseName</code>
            |Batch: <code>$batchName</code>
            |Seats: <code>$seats</code>
        """.trimMargin()
        return sendMessage(botToken, chatId, message)
    }
}
