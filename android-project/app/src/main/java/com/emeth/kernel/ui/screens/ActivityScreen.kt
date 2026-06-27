package com.emeth.kernel.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.emeth.kernel.watchers.ConditionOp
import com.emeth.kernel.watchers.Watcher
import com.emeth.kernel.watchers.WatcherRegistry
import com.emeth.kernel.watchers.WatcherType

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
                        title = "No automations yet",
                        body = "Create one from Home by telling Emeth what to watch."
                    )
                }
            } else {
                items(automations, key = { it.id }) { watcher ->
                    AutomationCard(
                        title = formatWatcherTitle(watcher),
                        body = watcher.action.payload,
                        onDelete = {
                            registry.removeWatcher(watcher.id)
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
private fun AutomationCard(title: String, body: String, onDelete: (() -> Unit)? = null) {
    Card(
        shape = RoundedCornerShape(16.dp),
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
            }
            if (onDelete != null) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "Delete automation")
                }
            }
        }
    }
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
        ConditionOp.GREATER_THAN_EQUAL -> "reaches"
        ConditionOp.LESS_THAN_EQUAL -> "drops to"
        ConditionOp.EQUAL -> "is"
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
