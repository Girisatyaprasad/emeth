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
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.animation.animateContentSize
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

import android.widget.Toast
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.GlobalScope

class WidgetPromptActivity : ComponentActivity() {
    @OptIn(ExperimentalAnimationApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.decorView.setBackgroundColor(android.graphics.Color.TRANSPARENT)
        
        setContent {
            MaterialTheme {
                Surface(
                    color = Color.Transparent,
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { finish() }
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 32.dp),
                        contentAlignment = Alignment.TopCenter
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
            
            if (isSchedulable(parsed.intentType)) {
                isScheduling = true
            } else {
                val commandText = text
                onDismiss()
                GlobalScope.launch(Dispatchers.Default) {
                    val result = planner.process(commandText)
                    val message = when (result) {
                        is com.emeth.kernel.skills.SkillResult.Success -> result.message ?: "Task completed"
                        is com.emeth.kernel.skills.SkillResult.Failure -> result.reason.ifBlank { "Failed to execute" }
                        is com.emeth.kernel.skills.SkillResult.Partial -> result.message
                        null -> "Unknown command"
                    }
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .clickable(enabled = false) {}
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .animateContentSize()
            .padding(16.dp)
    ) {
        // Prompt Bar
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth().height(24.dp)
        ) {
            BasicTextField(
                value = text,
                onValueChange = { 
                    text = it
                    if (isScheduling) { isScheduling = false }
                },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.Black),
                modifier = Modifier
                    .weight(1f)
                    .focusRequester(focusRequester),
                keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { handleSend() }),
                decorationBox = { innerTextField ->
                    if (text.isEmpty()) {
                        Text("Ask Emeth...", color = Color.Gray, style = MaterialTheme.typography.bodyLarge)
                    }
                    innerTextField()
                }
            )
            
            if (!isScheduling) {
                IconButton(
                    onClick = handleSend,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.Black)
                }
            } else {
                IconButton(
                    onClick = {
                        val commandText = text
                        onDismiss()
                        GlobalScope.launch(Dispatchers.Default) {
                            val result = planner.process(commandText)
                            val message = when (result) {
                                is com.emeth.kernel.skills.SkillResult.Success -> result.message ?: "Task completed"
                                is com.emeth.kernel.skills.SkillResult.Failure -> result.reason.ifBlank { "Failed to execute" }
                                is com.emeth.kernel.skills.SkillResult.Partial -> result.message
                                null -> "Unknown command"
                            }
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Run Now", tint = MaterialTheme.colorScheme.primary)
                }
            }
        }
        
        if (isScheduling && parsedCommand != null) {
            Spacer(modifier = Modifier.height(16.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))
            // Scheduling State
            SchedulingUi(
                command = parsedCommand!!,
                originalText = text,
                onSave = { watcher ->
                    registry.addWatcher(watcher)
                    Toast.makeText(context, "Scheduled!", Toast.LENGTH_SHORT).show()
                    onDismiss()
                }
            )
        }
    }
}

@Composable
fun SchedulingUi(
    command: ParsedCommand,
    originalText: String,
    onSave: (Watcher) -> Unit
) {
    var hour by remember { mutableStateOf(12) }
    var minute by remember { mutableStateOf(0) }
    var isPm by remember { mutableStateOf(false) }
    var recurrence by remember { mutableStateOf(WatcherRecurrence.ONCE) }
    var showRecurrenceMenu by remember { mutableStateOf(false) }
    
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "$hour",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { hour = if (hour == 12) 1 else hour + 1 }.padding(4.dp)
                )
                Text(":", color = Color.Black, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = minute.toString().padStart(2, '0'),
                    color = Color.Black,
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.clickable { minute = (minute + 15) % 60 }.padding(4.dp)
                )
                Text(
                    text = if (isPm) "PM" else "AM",
                    color = Color.Black,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickable { isPm = !isPm }.padding(4.dp)
                )
            }
            
            Box {
                Text(
                    text = when (recurrence) {
                        WatcherRecurrence.ONCE -> "Once"
                        WatcherRecurrence.DAILY -> "Daily"
                        WatcherRecurrence.SELECTIVE_DAYS -> "Custom"
                    },
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.clickable { showRecurrenceMenu = true }.padding(4.dp)
                )
                DropdownMenu(expanded = showRecurrenceMenu, onDismissRequest = { showRecurrenceMenu = false }) {
                    DropdownMenuItem(text = { Text("Once") }, onClick = { recurrence = WatcherRecurrence.ONCE; showRecurrenceMenu = false })
                    DropdownMenuItem(text = { Text("Daily") }, onClick = { recurrence = WatcherRecurrence.DAILY; showRecurrenceMenu = false })
                }
            }
            
            TextButton(
                onClick = {
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
                            type = "execute_command",
                            payload = originalText
                        ),
                        recurrence = recurrence
                    )
                    onSave(watcher)
                }
            ) {
                Text("Schedule")
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
