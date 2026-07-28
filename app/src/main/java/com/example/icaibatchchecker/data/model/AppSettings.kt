package com.example.icaibatchchecker.data.model

data class AppSettings(
    val regionValue: String = "4",
    val pouText: String = "Chennai",
    val courseValue: String = "45",
    val checkIntervalMinutes: Int = 10,
    val telegramBotToken: String = "",
    val telegramChatId: String = "",
    val notificationsEnabled: Boolean = true,
    val telegramEnabled: Boolean = false
)

val REGION_CATALOG = mapOf(
    "Northern" to "1",
    "Western" to "2",
    "Eastern" to "3",
    "Southern" to "4",
    "Central" to "5"
)

val COURSE_CATALOG = mapOf(
    "AICITSS - MCS" to "45",
    "AICITSS - Advanced Information Technology" to "48"
)
