package com.example.icaibatchchecker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.icaibatchchecker.data.model.COURSE_CATALOG
import com.example.icaibatchchecker.data.model.REGION_CATALOG

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    regionValue: String,
    pouText: String,
    courseValue: String,
    checkIntervalMinutes: Int,
    telegramBotToken: String,
    telegramChatId: String,
    notificationsEnabled: Boolean,
    telegramEnabled: Boolean,
    isTestingTelegram: Boolean,
    onRegionChange: (String) -> Unit,
    onPouChange: (String) -> Unit,
    onCourseChange: (String) -> Unit,
    onCheckIntervalChange: (Int) -> Unit,
    onTelegramTokenChange: (String) -> Unit,
    onTelegramChatIdChange: (String) -> Unit,
    onNotificationsEnabledChange: (Boolean) -> Unit,
    onTelegramEnabledChange: (Boolean) -> Unit,
    onTestTelegram: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Course Settings
        Text(
            text = "Course Settings",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Region Dropdown
        var regionExpanded by remember { mutableStateOf(false) }
        val regionName = REGION_CATALOG.entries.firstOrNull { it.value == regionValue }?.key ?: "Unknown"

        ExposedDropdownMenuBox(
            expanded = regionExpanded,
            onExpandedChange = { regionExpanded = it }
        ) {
            OutlinedTextField(
                value = regionName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Region") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = regionExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = regionExpanded,
                onDismissRequest = { regionExpanded = false }
            ) {
                REGION_CATALOG.forEach { (name, value) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onRegionChange(value)
                            regionExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // POU Text Field
        OutlinedTextField(
            value = pouText,
            onValueChange = onPouChange,
            label = { Text("Place of Utilization (POU)") },
            placeholder = { Text("e.g., Chennai, Mumbai") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Course Dropdown
        var courseExpanded by remember { mutableStateOf(false) }
        val courseName = COURSE_CATALOG.entries.firstOrNull { it.value == courseValue }?.key ?: "Unknown"

        ExposedDropdownMenuBox(
            expanded = courseExpanded,
            onExpandedChange = { courseExpanded = it }
        ) {
            OutlinedTextField(
                value = courseName,
                onValueChange = {},
                readOnly = true,
                label = { Text("Course") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = courseExpanded) },
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = courseExpanded,
                onDismissRequest = { courseExpanded = false }
            ) {
                COURSE_CATALOG.forEach { (name, value) ->
                    DropdownMenuItem(
                        text = { Text(name) },
                        onClick = {
                            onCourseChange(value)
                            courseExpanded = false
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Check Interval
        OutlinedTextField(
            value = checkIntervalMinutes.toString(),
            onValueChange = { value ->
                value.toIntOrNull()?.let { if (it in 10..60) onCheckIntervalChange(it) }
            },
            label = { Text("Check Interval (minutes)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Notifications
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Push Notifications",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "Get notified when seats are available",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                    )
                }
                Switch(
                    checked = notificationsEnabled,
                    onCheckedChange = onNotificationsEnabledChange
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Telegram Settings
        Text(
            text = "Telegram Notifications",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Enable Telegram",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "Send alerts to your Telegram chat",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    }
                    Switch(
                        checked = telegramEnabled,
                        onCheckedChange = onTelegramEnabledChange
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = telegramBotToken,
                    onValueChange = onTelegramTokenChange,
                    label = { Text("Bot Token") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = telegramChatId,
                    onValueChange = onTelegramChatIdChange,
                    label = { Text("Chat ID") },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onTestTelegram,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isTestingTelegram &&
                            telegramBotToken.isNotBlank() &&
                            telegramChatId.isNotBlank()
                ) {
                    if (isTestingTelegram) {
                        CircularProgressIndicator(
                            modifier = Modifier.height(20.dp).width(20.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Test Connection")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
