package com.example.icaibatchchecker.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.icaibatchchecker.data.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

object SettingsStore {
    private object Keys {
        val REGION_VALUE = stringPreferencesKey("region_value")
        val POU_TEXT = stringPreferencesKey("pou_text")
        val COURSE_VALUE = stringPreferencesKey("course_value")
        val CHECK_INTERVAL = intPreferencesKey("check_interval_minutes")
        val TELEGRAM_TOKEN = stringPreferencesKey("telegram_bot_token")
        val TELEGRAM_CHAT_ID = stringPreferencesKey("telegram_chat_id")
        val NOTIFICATIONS_ENABLED = booleanPreferencesKey("notifications_enabled")
        val TELEGRAM_ENABLED = booleanPreferencesKey("telegram_enabled")
    }

    fun observe(context: Context): Flow<AppSettings> {
        return context.dataStore.data.map { prefs ->
            AppSettings(
                regionValue = prefs[Keys.REGION_VALUE] ?: "4",
                pouText = prefs[Keys.POU_TEXT] ?: "Chennai",
                courseValue = prefs[Keys.COURSE_VALUE] ?: "45",
                checkIntervalMinutes = prefs[Keys.CHECK_INTERVAL] ?: 10,
                telegramBotToken = prefs[Keys.TELEGRAM_TOKEN] ?: "",
                telegramChatId = prefs[Keys.TELEGRAM_CHAT_ID] ?: "",
                notificationsEnabled = prefs[Keys.NOTIFICATIONS_ENABLED] ?: true,
                telegramEnabled = prefs[Keys.TELEGRAM_ENABLED] ?: false
            )
        }
    }

    suspend fun get(context: Context): AppSettings {
        return observe(context).first()
    }

    suspend fun update(context: Context, block: AppSettings.() -> AppSettings) {
        val current = get(context)
        val updated = block(current)
        context.dataStore.edit { prefs ->
            prefs[Keys.REGION_VALUE] = updated.regionValue
            prefs[Keys.POU_TEXT] = updated.pouText
            prefs[Keys.COURSE_VALUE] = updated.courseValue
            prefs[Keys.CHECK_INTERVAL] = updated.checkIntervalMinutes
            prefs[Keys.TELEGRAM_TOKEN] = updated.telegramBotToken
            prefs[Keys.TELEGRAM_CHAT_ID] = updated.telegramChatId
            prefs[Keys.NOTIFICATIONS_ENABLED] = updated.notificationsEnabled
            prefs[Keys.TELEGRAM_ENABLED] = updated.telegramEnabled
        }
    }
}
