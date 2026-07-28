package com.example.icaibatchchecker.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.icaibatchchecker.data.local.SettingsStore
import com.example.icaibatchchecker.data.model.COURSE_CATALOG
import com.example.icaibatchchecker.data.remote.IcaiScraper
import com.example.icaibatchchecker.data.remote.TelegramBot
import com.example.icaibatchchecker.notification.NotificationHelper

class BatchCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val settings = SettingsStore.get(applicationContext)

            val result = IcaiScraper.checkBatches(
                regionValue = settings.regionValue,
                pouText = settings.pouText,
                courseValue = settings.courseValue
            )

            result.fold(
                onSuccess = { batches ->
                    val availableBatches = batches.filter { it.seats > 0 }

                    if (availableBatches.isNotEmpty()) {
                        val courseName = COURSE_CATALOG.entries
                            .firstOrNull { it.value == settings.courseValue }
                            ?.key ?: settings.courseValue

                        // Android notification
                        if (settings.notificationsEnabled) {
                            for (batch in availableBatches) {
                                NotificationHelper.showBatchAlert(
                                    context = applicationContext,
                                    batchName = batch.name,
                                    seats = batch.seats,
                                    courseName = courseName
                                )
                            }
                        }

                        // Telegram notification
                        if (settings.telegramEnabled &&
                            settings.telegramBotToken.isNotBlank() &&
                            settings.telegramChatId.isNotBlank()
                        ) {
                            for (batch in availableBatches) {
                                TelegramBot.sendBatchAlert(
                                    botToken = settings.telegramBotToken,
                                    chatId = settings.telegramChatId,
                                    courseName = courseName,
                                    batchName = batch.name,
                                    seats = batch.seats
                                )
                            }
                        }
                    }

                    Result.success()
                },
                onFailure = { error ->
                    Result.failure()
                }
            )
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
