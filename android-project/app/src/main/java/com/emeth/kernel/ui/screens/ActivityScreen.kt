package com.emeth.kernel.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.emeth.kernel.watchers.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ActivityScreen() {
    val context = LocalContext.current
    val registry = remember { WatcherRegistry(context) }
    var automations by remember { mutableStateOf(listOf<Watcher>()) }

    var selectedWatcher by remember { mutableStateOf<Watcher?>(null) }
    var showSheet by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        automations = registry.getAllWatchers()
    }

    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 24.dp)
        ) {
            Spacer(modifier = Modifier.height(48.dp))
            Text(
                "Automations",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Rules Emeth watches in the background.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
            )

            Spacer(modifier = Modifier.height(32.dp))

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                if (automations.isEmpty()) {
                    item {
                        AutomationCard(
                            watcher = null,
                            title = "No automations yet",
                            body = "Create one from Home by telling Emeth what to watch.",
                            onLongPress = {}
                        )
                    }
                } else {
                    items(automations, key = { it.id }) { watcher ->
                        AutomationCard(
                            watcher = watcher,
                            title = formatWatcherTitle(watcher),
                            body = watcher.action.payload,
                            onLongPress = {
                                selectedWatcher = watcher
                                showSheet = true
                            }
                        )
                    }
                }
            }
        }
    }

    if (showSheet && selectedWatcher != null) {
        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            AutomationMenuSheet(
                watcher = selectedWatcher!!,
                onUpdate = { updated ->
                    registry.addWatcher(updated)
                    automations = registry.getAllWatchers()
                    showSheet = false
                },
                onDelete = {
                    registry.removeWatcher(selectedWatcher!!.id)
                    automations = registry.getAllWatchers()
                    showSheet = false
                }
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun AutomationCard(
    watcher: Watcher?,
    title: String,
    body: String,
    onLongPress: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(if (isPressed) 0.96f else 1f, label = "scale")

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f))
            .combinedClickable(
                onLongClick = {
                    haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                    onLongPress()
                },
                onClick = {}
            )
            .padding(20.dp)
    ) {
        Column {
            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.SemiBold),
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                if (watcher != null) {
                    val recurrenceText = when (watcher.recurrence) {
                        WatcherRecurrence.ONCE -> "Once"
                        WatcherRecurrence.DAILY -> "Daily"
                        WatcherRecurrence.SELECTIVE_DAYS -> "Custom"
                    }
                    Surface(
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = recurrenceText,
                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                body,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun AutomationMenuSheet(
    watcher: Watcher,
    onUpdate: (Watcher) -> Unit,
    onDelete: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "Schedule",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ScheduleOption(
                title = "Once",
                selected = watcher.recurrence == WatcherRecurrence.ONCE,
                onClick = { onUpdate(watcher.copy(recurrence = WatcherRecurrence.ONCE)) },
                modifier = Modifier.weight(1f)
            )
            ScheduleOption(
                title = "Daily",
                selected = watcher.recurrence == WatcherRecurrence.DAILY,
                onClick = { onUpdate(watcher.copy(recurrence = WatcherRecurrence.DAILY)) },
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onDelete,
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Icon(
                Icons.Filled.Delete,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Delete Automation",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onErrorContainer
            )
        }
    }
}

@Composable
private fun ScheduleOption(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
    val textColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant

    Surface(
        onClick = onClick,
        color = bgColor,
        shape = RoundedCornerShape(16.dp),
        modifier = modifier.height(64.dp)
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = textColor
            )
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
