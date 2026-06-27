package com.emeth.kernel.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.emeth.kernel.permissions.PermissionCockpit
import com.emeth.kernel.permissions.PermissionSetup
import com.emeth.kernel.permissions.PermissionStatus
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

@Composable
fun PermissionScreen() {
    val context = LocalContext.current
    var statuses by remember { mutableStateOf(PermissionCockpit.current(context)) }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        statuses = PermissionCockpit.current(context)
        val denied = statuses.filterNot { it.enabled && it.setup == PermissionSetup.RUNTIME_PERMISSION }
        resultMessage = if (denied.any { it.setup == PermissionSetup.RUNTIME_PERMISSION }) {
            "Some access is still missing. If Android does not show a prompt again, open App settings."
        } else {
            "Permission state updated."
        }
    }

    LaunchedEffect(Unit) {
        statuses = PermissionCockpit.current(context)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp)
    ) {
        Spacer(modifier = Modifier.height(48.dp))
        Text("Access", style = MaterialTheme.typography.headlineLarge)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "${statuses.count { it.enabled }}/${statuses.size} enabled",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.secondary
        )
        if (resultMessage != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                resultMessage!!,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.secondary
            )
        }
        Spacer(modifier = Modifier.height(24.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(statuses, key = { it.id }) { status ->
                PermissionCard(
                    status = status,
                    onSetup = {
                        if (status.setup == PermissionSetup.RUNTIME_PERMISSION && status.permissions.isNotEmpty()) {
                            permissionLauncher.launch(status.permissions.toTypedArray())
                        } else {
                            PermissionCockpit.openSetup(context, status.setup)
                        }
                        statuses = PermissionCockpit.current(context)
                    },
                    onSettings = {
                        PermissionCockpit.appSettings(context)
                    }
                )
            }
            item {
                Text(
                    "This page shows real Android access state. Enable missing access only when you want Emeth to use that phone surface.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun PermissionCard(
    status: PermissionStatus,
    onSetup: () -> Unit,
    onSettings: () -> Unit
) {
    val haptics = LocalHapticFeedback.current
    Card(
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(status.label, style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    if (status.enabled) "Enabled" else "Missing",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (status.enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    status.unlocks,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
            if (!status.enabled) {
                Column(horizontalAlignment = Alignment.End) {
                    Button(onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onSetup()
                    }) {
                        Text(if (status.setup == PermissionSetup.RUNTIME_PERMISSION) "Allow" else "Setup")
                    }
                    if (status.setup == PermissionSetup.RUNTIME_PERMISSION) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Button(onClick = {
                            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                            onSettings()
                        }) {
                            Text("Settings")
                        }
                    }
                }
            }
        }
    }
}
