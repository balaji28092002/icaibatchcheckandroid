package com.example.icaibatchchecker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.icaibatchchecker.data.local.SettingsStore
import com.example.icaibatchchecker.data.local.dataStore
import com.example.icaibatchchecker.data.model.Batch
import com.example.icaibatchchecker.data.remote.IcaiScraper
import com.example.icaibatchchecker.data.remote.TelegramBot
import com.example.icaibatchchecker.notification.NotificationHelper
import com.example.icaibatchchecker.ui.HomeScreen
import com.example.icaibatchchecker.ui.Navigation
import com.example.icaibatchchecker.ui.SettingsScreen
import com.example.icaibatchchecker.ui.theme.IcaiAppTheme
import com.example.icaibatchchecker.worker.BatchCheckWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        NotificationHelper.createNotificationChannel(this)

        setContent {
            IcaiAppTheme {
                val scope = rememberCoroutineScope()
                val settings by SettingsStore.observe(this@MainActivity).collectAsState(
                    initial = com.example.icaibatchchecker.data.model.AppSettings()
                )

                var isChecking by remember { mutableStateOf(false) }
                var lastCheckTime by remember { mutableStateOf<String?>(null) }
                var availableBatches by remember { mutableStateOf<List<Batch>>(emptyList()) }
                var errorMessage by remember { mutableStateOf<String?>(null) }
                var isTestingTelegram by remember { mutableStateOf(false) }

                Navigation(
                    homeContent = {
                        HomeScreen(
                            isChecking = isChecking,
                            lastCheckTime = lastCheckTime,
                            availableBatches = availableBatches,
                            errorMessage = errorMessage,
                            onCheckNow = {
                                scope.launch {
                                    isChecking = true
                                    errorMessage = null

                                    val result = withContext(Dispatchers.IO) {
                                        IcaiScraper.checkBatches(
                                            regionValue = settings.regionValue,
                                            pouText = settings.pouText,
                                            courseValue = settings.courseValue
                                        )
                                    }

                                    result.fold(
                                        onSuccess = { batches ->
                                            availableBatches = batches.filter { it.seats > 0 }
                                            lastCheckTime = SimpleDateFormat(
                                                "HH:mm:ss, dd MMM",
                                                Locale.getDefault()
                                            ).format(Date())

                                            if (availableBatches.isNotEmpty()) {
                                                if (settings.notificationsEnabled) {
                                                    for (batch in availableBatches) {
                                                        NotificationHelper.showBatchAlert(
                                                            context = this@MainActivity,
                                                            batchName = batch.name,
                                                            seats = batch.seats
                                                        )
                                                    }
                                                }

                                                if (settings.telegramEnabled &&
                                                    settings.telegramBotToken.isNotBlank() &&
                                                    settings.telegramChatId.isNotBlank()
                                                ) {
                                                    for (batch in availableBatches) {
                                                        TelegramBot.sendBatchAlert(
                                                            botToken = settings.telegramBotToken,
                                                            chatId = settings.telegramChatId,
                                                            courseName = settings.courseValue,
                                                            batchName = batch.name,
                                                            seats = batch.seats
                                                        )
                                                    }
                                                }
                                            }
                                        },
                                        onFailure = { error ->
                                            errorMessage = error.message ?: "Unknown error"
                                        }
                                    )

                                    isChecking = false
                                }
                            }
                        )
                    },
                    settingsContent = {
                        SettingsScreen(
                            regionValue = settings.regionValue,
                            pouText = settings.pouText,
                            courseValue = settings.courseValue,
                            checkIntervalMinutes = settings.checkIntervalMinutes,
                            telegramBotToken = settings.telegramBotToken,
                            telegramChatId = settings.telegramChatId,
                            notificationsEnabled = settings.notificationsEnabled,
                            telegramEnabled = settings.telegramEnabled,
                            isTestingTelegram = isTestingTelegram,
                            onRegionChange = { value ->
                                scope.launch {
                                    SettingsStore.update(this@MainActivity) {
                                        copy(regionValue = value)
                                    }
                                }
                            },
                            onPouChange = { value ->
                                scope.launch {
                                    SettingsStore.update(this@MainActivity) {
                                        copy(pouText = value)
                                    }
                                }
                            },
                            onCourseChange = { value ->
                                scope.launch {
                                    SettingsStore.update(this@MainActivity) {
                                        copy(courseValue = value)
                                    }
                                }
                            },
                            onCheckIntervalChange = { value ->
                                scope.launch {
                                    SettingsStore.update(this@MainActivity) {
                                        copy(checkIntervalMinutes = value)
                                    }
                                }
                            },
                            onTelegramTokenChange = { value ->
                                scope.launch {
                                    SettingsStore.update(this@MainActivity) {
                                        copy(telegramBotToken = value)
                                    }
                                }
                            },
                            onTelegramChatIdChange = { value ->
                                scope.launch {
                                    SettingsStore.update(this@MainActivity) {
                                        copy(telegramChatId = value)
                                    }
                                }
                            },
                            onNotificationsEnabledChange = { value ->
                                scope.launch {
                                    SettingsStore.update(this@MainActivity) {
                                        copy(notificationsEnabled = value)
                                    }
                                }
                            },
                            onTelegramEnabledChange = { value ->
                                scope.launch {
                                    SettingsStore.update(this@MainActivity) {
                                        copy(telegramEnabled = value)
                                    }
                                }
                            },
                            onTestTelegram = {
                                scope.launch {
                                    isTestingTelegram = true
                                    val result = withContext(Dispatchers.IO) {
                                        TelegramBot.sendTestMessage(
                                            botToken = settings.telegramBotToken,
                                            chatId = settings.telegramChatId
                                        )
                                    }
                                    isTestingTelegram = false

                                    result.fold(
                                        onSuccess = {
                                            errorMessage = null
                                        },
                                        onFailure = { error ->
                                            errorMessage = "Telegram test failed: ${error.message}"
                                        }
                                    )
                                }
                            }
                        )
                    }
                )
            }
        }
    }
}
