package com.emeth.kernel.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import com.emeth.kernel.watchers.ConditionOp
import com.emeth.kernel.watchers.Watcher
import com.emeth.kernel.watchers.WatcherRegistry
import com.emeth.kernel.watchers.WatcherType
import com.emeth.kernel.watchers.WatcherRecurrence

@Composable
fun ActivityScreen() {
    val context = LocalContext.current
    val registry = remember { WatcherRegistry(context) }
    var automations by remember { mutableStateOf(listOf<Watcher>()) }

    LaunchedEffect(Unit) {
        automations = registry.getAllWatchers()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Automations", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Rules Emeth can watch in the background.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            if (automations.isEmpty()) {
                item {
                    AutomationCard(
                        watcher = null,
                        title = "No automations yet",
                        body = "Create one from Home by telling Emeth what to watch."
                    )
                }
            } else {
                items(automations, key = { it.id }) { watcher ->
                    AutomationCard(
                        watcher = watcher,
                        title = formatWatcherTitle(watcher),
                        body = watcher.action.payload,
                        onDelete = {
                            registry.removeWatcher(watcher.id)
                            automations = registry.getAllWatchers()
                        },
                        onUpdate = { updated ->
                            registry.addWatcher(updated)
                            automations = registry.getAllWatchers()
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "Automations shown here are real saved rules. Delete removes the rule from Emeth.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun AutomationCard(
    watcher: Watcher?,
    title: String,
    body: String,
    onDelete: (() -> Unit)? = null,
    onUpdate: ((Watcher) -> Unit)? = null
) {
    val haptics = LocalHapticFeedback.current
    var showDropdown by remember { mutableStateOf(false) }
    var showCustomDaysDialog by remember { mutableStateOf(false) }

    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    body,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                if (watcher != null && onUpdate != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Box {
                        Text(
                            text = when (watcher.recurrence) {
                                WatcherRecurrence.ONCE -> "Runs Once"
                                WatcherRecurrence.DAILY -> "Runs Daily"
                                WatcherRecurrence.SELECTIVE_DAYS -> "Runs on Custom Days"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .clickable { showDropdown = true }
                                .padding(vertical = 4.dp)
                        )
                        DropdownMenu(
                            expanded = showDropdown,
                            onDismissRequest = { showDropdown = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Once") },
                                onClick = {
                                    onUpdate(watcher.copy(recurrence = WatcherRecurrence.ONCE))
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Daily") },
                                onClick = {
                                    onUpdate(watcher.copy(recurrence = WatcherRecurrence.DAILY))
                                    showDropdown = false
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Custom...") },
                                onClick = {
                                    showDropdown = false
                                    showCustomDaysDialog = true
                                }
                            )
                        }
                    }
                }
            }
            if (onDelete != null) {
                IconButton(onClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onDelete()
                }) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete automation")
                }
            }
        }
    }

    if (showCustomDaysDialog && watcher != null && onUpdate != null) {
        CustomDaysDialog(
            initialDays = watcher.selectedDays,
            onDismiss = { showCustomDaysDialog = false },
            onConfirm = { days ->
                onUpdate(watcher.copy(recurrence = WatcherRecurrence.SELECTIVE_DAYS, selectedDays = days))
                showCustomDaysDialog = false
            }
        )
    }
}

@Composable
private fun CustomDaysDialog(
    initialDays: List<Int>,
    onDismiss: () -> Unit,
    onConfirm: (List<Int>) -> Unit
) {
    val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
    var selected by remember { mutableStateOf(initialDays.toSet()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select Days") },
        text = {
            Column {
                daysOfWeek.forEachIndexed { index, day ->
                    val dayValue = index + 1
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable {
                                selected = if (selected.contains(dayValue)) {
                                    selected - dayValue
                                } else {
                                    selected + dayValue
                                }
                            }
                            .padding(vertical = 8.dp),
                        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = selected.contains(dayValue),
                            onCheckedChange = null // handled by row click
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(day)
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selected.toList()) }) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

private fun formatWatcherTitle(watcher: Watcher): String {
    return when (watcher.type) {
        WatcherType.TIME_OF_DAY -> "At ${formatMinutes(watcher.condition.targetValue)}"
        WatcherType.STEP_COUNT -> "When steps ${formatOp(watcher.condition.op)} ${watcher.condition.targetValue?.toInt() ?: 0}"
        WatcherType.BATTERY_LEVEL -> "When battery ${formatOp(watcher.condition.op)} ${watcher.condition.targetValue?.toInt() ?: 0}%"
        WatcherType.STORAGE_LEVEL -> "When storage ${formatOp(watcher.condition.op)} ${watcher.condition.targetValue ?: 0f} GB"
        WatcherType.WIFI_DISCONNECT -> "When Wi-Fi disconnects"
        WatcherType.BLUETOOTH_DISCONNECT -> "When Bluetooth disconnects"
        WatcherType.MISSED_CALL -> "When there is a missed call"
        WatcherType.CALENDAR_EVENT_START -> "When a calendar event starts"
        WatcherType.SCREEN_TIME -> "When screen time reaches ${watcher.condition.targetValue?.toInt() ?: 0} minutes"
    }
}

private fun formatOp(op: ConditionOp): String {
    return when (op) {
        ConditionOp.GREATER_THAN_EQUAL -> "reach"
        ConditionOp.LESS_THAN_EQUAL -> "drop to"
        ConditionOp.EQUAL -> "are"
        ConditionOp.TRIGGERED -> "triggers"
    }
}

private fun formatMinutes(value: Float?): String {
    val total = value?.toInt() ?: return "the chosen time"
    val hour24 = total / 60
    val minute = total % 60
    val ampm = if (hour24 >= 12) "PM" else "AM"
    val hour12 = when {
        hour24 == 0 -> 12
        hour24 > 12 -> hour24 - 12
        else -> hour24
    }
    return "$hour12:${minute.toString().padStart(2, '0')} $ampm"
}
