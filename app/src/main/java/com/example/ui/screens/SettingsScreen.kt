package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ApiKeyEntity
import com.example.network.UnifiedApiClient
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun SettingsScreen(viewModel: AssistantViewModel) {
    val apiKeys by viewModel.apiKeys.collectAsState()
    val darkMode by viewModel.darkMode.collectAsState()
    val importExportMessage by viewModel.importExportMessage.collectAsState()

    var showAddKeyDialog by remember { mutableStateOf(false) }
    var showImportExportDialog by remember { mutableStateOf(false) }

    var localOnlyPrivacyToggle by remember { mutableStateOf(true) }
    var analyticConsentToggle by remember { mutableStateOf(false) }

    val context = LocalContext.current

    // Synchronize snackbar notifications
    LaunchedEffect(importExportMessage) {
        if (importExportMessage != null) {
            Toast.makeText(context, importExportMessage, Toast.LENGTH_LONG).show()
            viewModel.clearImportExportMessage()
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("settings_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // --- 1. BYO Credentials Dashboard Core ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "API Key Management",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Encrypted locally inside your SQLite sandbox.",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }

                Button(
                    onClick = { showAddKeyDialog = true },
                    modifier = Modifier.testTag("add_provider_key_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Key")
                }
            }
        }

        if (apiKeys.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.VpnKey, contentDescription = null, modifier = Modifier.size(36.dp), tint = MaterialTheme.colorScheme.outline)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No keys entered. Configure deepseek or gemini.",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        } else {
            items(apiKeys) { key ->
                ApiKeySettingsCard(
                    key = key,
                    onToggleActive = { viewModel.makeKeyActive(key.id, key.providerId) },
                    onDelete = { viewModel.deleteApiKey(key.id) },
                    onTest = { viewModel.testApiKey(key) }
                )
            }
        }

        // --- 2. Custom Visual Apperance Settings ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Application Theme Preferences",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val modeOptions = listOf("system", "light", "dark")
                    modeOptions.forEach { mode ->
                        val selected = darkMode == mode
                        OutlinedButton(
                            onClick = { viewModel.selectTheme(mode) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("theme_chip_$mode"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent
                            )
                        ) {
                            Text(mode.capitalize())
                        }
                    }
                }
            }
        }

        // --- 3. Privacy-First Sandbox Options ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Security & Local Sandbox Protocols",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.EnhancedEncryption, contentDescription = "Encrypted", tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Local-Only Isolation Mode", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("API queries request direct model endpoints.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(checked = localOnlyPrivacyToggle, onCheckedChange = { localOnlyPrivacyToggle = it })
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.BarChart, contentDescription = "Usage", tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text("Anonymous Telemetry", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("Contribute to diagnostic reports.", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Switch(checked = analyticConsentToggle, onCheckedChange = { analyticConsentToggle = it })
                        }
                    }
                }
            }
        }

        // --- 4. Database Cost Control & Utilities ---
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = "Maintenance & Disaster Protocols",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = { showImportExportDialog = true },
                        modifier = Modifier.weight(1f).testTag("settings_import_export_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Backup, contentDescription = "Backup")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Credential Sync")
                    }

                    Button(
                        onClick = {
                            viewModel.clearAppDataCache()
                            Toast.makeText(context, "All localized caches deleted safely.", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.weight(1f).testTag("settings_clear_cache_button"),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                    ) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "Wipe")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear All Cache")
                    }
                }
            }
        }
    }

    // --- Add Key AlertDialog Panel ---
    if (showAddKeyDialog) {
        AddNewKeyDialog(
            onDismiss = { showAddKeyDialog = false },
            onConfirm = { provider, nickname, secret ->
                viewModel.addApiKey(provider, nickname, secret)
                showAddKeyDialog = false
            }
        )
    }

    // --- Import / Export Backup Dialog Panel ---
    if (showImportExportDialog) {
        ImportExportJsonDialog(
            onDismiss = { showImportExportDialog = false },
            onExport = {
                val json = viewModel.exportLocalDataToJson() ?: ""
                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = android.content.ClipData.newPlainText("BYOA Backup", json)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(context, "Backup JSON copied to clipboard!", Toast.LENGTH_SHORT).show()
            },
            onImport = { json ->
                val success = viewModel.importLocalDataFromJson(json)
                if (success) showImportExportDialog = false
            }
        )
    }
}

@Composable
fun ApiKeySettingsCard(
    key: ApiKeyEntity,
    onToggleActive: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    val statusColor = when (key.status.uppercase()) {
        "ACTIVE" -> Color(0xFF2E7D32)
        "LIMITED" -> Color(0xFFF9A825)
        "ERROR" -> Color(0xFFC62828)
        else -> MaterialTheme.colorScheme.outline
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(statusColor))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = key.nickname, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (key.isActive) "Default Provider" else "Select Default",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (key.isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (key.isActive) MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
                                else Color.Transparent
                            )
                            .clickable { onToggleActive() }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                    )

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete key", tint = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Provider Reference: ${UnifiedApiClient.getProviderLabel(key.providerId)}",
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Protected masked characters
            Text(
                text = "Key: ••••••••••••••••${key.apiKey.takeLast(4)}",
                fontFamily = FontFamily.Monospace,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row {
                    if (key.latencyMs > 0) {
                        Text(
                            text = "Latency: ${key.latencyMs}ms",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Button(
                    onClick = onTest,
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primaryContainer, contentColor = MaterialTheme.colorScheme.onPrimaryContainer),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Icon(Icons.Default.OfflineBolt, contentDescription = "Diagnostic", modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Validate Status", fontSize = 11.sp)
                }
            }
        }
    }
}

@Composable
fun AddNewKeyDialog(
    onDismiss: () -> Unit,
    onConfirm: (provider: String, nickname: String, secret: String) -> Unit
) {
    var selectedProvider by remember { mutableStateOf("gemini") }
    var nickname by remember { mutableStateOf("") }
    var secretKeyInput by remember { mutableStateOf("") }

    val providerOptions = listOf("gemini", "openai", "anthropic", "deepseek", "grok", "mistral", "together", "openrouter", "stability", "elevenlabs", "fal")

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Provider Key") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Selector
                Column {
                    Text("Select Provider ID", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(44.dp).horizontalScroll(rememberScrollState())) {
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            providerOptions.forEach { p ->
                                val selected = selectedProvider == p
                                FilterChip(
                                    selected = selected,
                                    onClick = { selectedProvider = p },
                                    label = { Text(UnifiedApiClient.getProviderLabel(p)) }
                                )
                            }
                        }
                    }
                }

                OutlinedTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    placeholder = { Text("Nickname (e.g. My OpenAI Key)") },
                    modifier = Modifier.fillMaxWidth().testTag("add_key_nickname_input")
                )

                OutlinedTextField(
                    value = secretKeyInput,
                    onValueChange = { secretKeyInput = it },
                    placeholder = { Text("API Secret Key (BYOA)") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth().testTag("add_key_secret_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (secretKeyInput.trim().isNotEmpty()) {
                        onConfirm(selectedProvider, nickname, secretKeyInput)
                    }
                },
                modifier = Modifier.testTag("add_key_confirm_button")
            ) {
                Text("Save Credentials")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ImportExportJsonDialog(
    onDismiss: () -> Unit,
    onExport: () -> Unit,
    onImport: (String) -> Unit
) {
    var rawJsonText by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Credential Portability") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Text("You can export your credentials block to keep values across multiple sandboxes, or paste an existing block to import keys instantly.", fontSize = 12.sp)

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onExport,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export To Clipboard")
                    }
                }

                Divider()

                Column {
                    Text("Paste Import Block JSON", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = rawJsonText,
                        onValueChange = { rawJsonText = it },
                        placeholder = { Text("{\n  \"keys\": [...]\n}") },
                        modifier = Modifier.fillMaxWidth().height(100.dp),
                        maxLines = 5
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (rawJsonText.trim().isNotEmpty()) {
                        onImport(rawJsonText)
                    }
                },
                modifier = Modifier.testTag("import_confirm_button")
            ) {
                Text("Execute Import")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}
