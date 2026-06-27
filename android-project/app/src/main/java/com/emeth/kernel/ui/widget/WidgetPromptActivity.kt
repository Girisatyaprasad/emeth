package com.emeth.kernel.ui.widget

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.emeth.kernel.intents.IntentResolver
import com.emeth.kernel.intents.Intent
import com.emeth.kernel.intents.ParsedCommand
import com.emeth.kernel.watchers.*
import com.emeth.kernel.planner.Planner
import com.emeth.kernel.DependencyGraph
import com.example.emeth.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class WidgetPromptActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Make the window background transparent
        window.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        
        setContent {
            MaterialTheme {
                Surface(
                    color = Color.Black.copy(alpha = 0.5f),
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { finish() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        WidgetPromptOverlay(
                            onDismiss = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun WidgetPromptOverlay(onDismiss: () -> Unit) {
    val context = LocalContext.current
    var text by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    var parsedCommand by remember { mutableStateOf<ParsedCommand?>(null) }
    var isScheduling by remember { mutableStateOf(false) }
    
    val scope = rememberCoroutineScope()
    val resolver = remember { IntentResolver() }
    val planner = remember { DependencyGraph.providePlanner(context) }
    val registry = remember { WatcherRegistry(context) }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val handleSend = {
        if (text.isNotBlank()) {
            val parsed = resolver.resolve(text)
            parsedCommand = parsed
            
            // Check if it's schedulable (e.g. sending a message, setting a reminder)
            if (isSchedulable(parsed.intentType)) {
                isScheduling = true
            } else {
                // Execute immediately
                scope.launch {
                    withContext(Dispatchers.Default) {
                        planner.process(text)
                    }
                    onDismiss()
                }
            }
        }
    }

    // Keep it from closing when clicking inside the box
    Box(
        modifier = Modifier
            .clickable(enabled = false) {}
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.White)
            .padding(16.dp)
    ) {
        AnimatedContent(targetState = isScheduling, label = "WidgetState") { scheduling ->
            if (!scheduling) {
                // Prompt Bar State
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    BasicTextField(
                        value = text,
                        onValueChange = { text = it },
                        modifier = Modifier
                            .weight(1f)
                            .focusRequester(focusRequester),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                        keyboardActions = KeyboardActions(onSend = { handleSend() }),
                        decorationBox = { innerTextField ->
                            if (text.isEmpty()) {
                                Text("Ask Emeth...", color = Color.Gray)
                            }
                            innerTextField()
                        }
                    )
                    IconButton(
                        onClick = handleSend,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Send, contentDescription = "Send", tint = Color.Black)
                    }
                }
            } else {
                // Scheduling State
                SchedulingUi(
                    command = parsedCommand!!,
                    originalText = text,
                    onSave = { watcher ->
                        registry.addWatcher(watcher)
                        onDismiss()
                    },
                    onCancel = onDismiss,
                    onExecuteNow = {
                        scope.launch {
                            withContext(Dispatchers.Default) {
                                planner.process(text)
                            }
                            onDismiss()
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun SchedulingUi(
    command: ParsedCommand,
    originalText: String,
    onSave: (Watcher) -> Unit,
    onCancel: () -> Unit,
    onExecuteNow: () -> Unit
) {
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var isPm by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf(WatcherRecurrence.ONCE) }
    
    Column(modifier = Modifier.fillMaxWidth()) {
        Text("Schedule this action?", style = MaterialTheme.typography.titleMedium, color = Color.Black)
        Spacer(modifier = Modifier.height(16.dp))
        
        // Very basic time picker UI for the widget
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            OutlinedButton(onClick = { hour = if (hour == 12) 1 else hour + 1 }) { Text("$hour", color = Color.Black) }
            Text(":", color = Color.Black)
            OutlinedButton(onClick = { minute = (minute + 15) % 60 }) { Text(minute.toString().padStart(2, '0'), color = Color.Black) }
            OutlinedButton(onClick = { isPm = !isPm }) { Text(if (isPm) "PM" else "AM", color = Color.Black) }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = recurrence == WatcherRecurrence.ONCE,
                onClick = { recurrence = WatcherRecurrence.ONCE },
                label = { Text("Once", color = Color.Black) }
            )
            FilterChip(
                selected = recurrence == WatcherRecurrence.DAILY,
                onClick = { recurrence = WatcherRecurrence.DAILY },
                label = { Text("Daily", color = Color.Black) }
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
            TextButton(onClick = onExecuteNow) { Text("Run Now") }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {
                val hour24 = if (isPm && hour < 12) hour + 12 else if (!isPm && hour == 12) 0 else hour
                val totalMinutes = hour24 * 60 + minute
                
                val watcher = Watcher(
                    id = java.util.UUID.randomUUID().toString(),
                    type = WatcherType.TIME_OF_DAY,
                    condition = WatcherCondition(
                        op = ConditionOp.EQUAL,
                        targetValue = totalMinutes.toFloat()
                    ),
                    action = WatcherAction(
                        type = "execute_command", // Assume planner can handle this
                        payload = originalText
                    ),
                    recurrence = recurrence
                )
                onSave(watcher)
            }) {
                Text("Schedule")
            }
        }
    }
}

private fun isSchedulable(intent: Intent): Boolean {
    return when (intent) {
        Intent.SEND_WHATSAPP,
        Intent.SMS_CONTACT,
        Intent.CALL_CONTACT,
        Intent.CREATE_REMINDER,
        Intent.SET_ALARM,
        Intent.TOGGLE_FLASHLIGHT,
        Intent.TOGGLE_BLUETOOTH,
        Intent.TOGGLE_WIFI -> true
        else -> false
    }
}
