package com.emeth.kernel.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.unit.dp
import com.emeth.kernel.planner.Planner
import com.emeth.kernel.skills.SkillResult

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(planner: Planner) {
    var inputText by remember { mutableStateOf("") }
    var actionResult by remember { mutableStateOf<String?>(null) }
    var showDebug by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        
        Text(
            text = "Emeth",
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier.pointerInput(Unit) {
                detectTapGestures(onLongPress = { showDebug = !showDebug })
            }
        )
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            placeholder = { Text("Ask Emeth...") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = OutlinedTextFieldDefaults.colors(
                unfocusedBorderColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.2f)
            ),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        if (inputText.isNotBlank()) {
            Button(
                onClick = {
                    val result = planner.process(inputText)
                    actionResult = if (result != null) {
                        when (result) {
                            is SkillResult.Success -> {
                                result.message ?: "Done."
                            }
                            is SkillResult.Failure -> {
                                result.reason.ifBlank { "I couldn't do that." }
                            }
                            is SkillResult.Partial -> result.message
                        }
                    } else {
                        "No skill is registered for that command."
                    }
                    inputText = ""
                },
                modifier = Modifier.align(Alignment.End)
            ) {
                Text("Go")
            }
        }

        if (actionResult != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = actionResult!!,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.primary
            )
            if (showDebug) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${planner.lastDebugInfo}\nfinal result = ${actionResult!!}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
