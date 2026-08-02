package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChatItemEntity
import com.example.data.model.ChatMessageEntity
import com.example.network.UnifiedApiClient
import com.example.ui.viewmodel.AssistantViewModel
import kotlinx.coroutines.launch

@Composable
fun ChatScreen(viewModel: AssistantViewModel) {
    val chats by viewModel.chats.collectAsState()
    val activeChatId by viewModel.activeChatId.collectAsState()
    val messages by viewModel.activeChatMessages.collectAsState()
    val chattingStatus by viewModel.chattingStatus.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showNewChatDialog by remember { mutableStateOf(false) }

    // Toggle list vs chat messaging interface
    Row(modifier = Modifier.fillMaxSize().testTag("chat_screen")) {
        if (activeChatId == null) {
            // --- Chat thread list view ---
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header with add button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Unified Chat Workspace",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(
                        onClick = { showNewChatDialog = true },
                        modifier = Modifier.testTag("create_new_chat_button")
                    ) {
                        Icon(Icons.Default.AddCircle, contentDescription = "New Thread", tint = MaterialTheme.colorScheme.primary)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search threads...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    modifier = Modifier.fillMaxWidth().testTag("search_threads_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors()
                )

                Spacer(modifier = Modifier.height(16.dp))

                val filteredChats = chats.filter {
                    it.title.contains(searchQuery, ignoreCase = true) ||
                            it.providerId.contains(searchQuery, ignoreCase = true)
                }

                if (filteredChats.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Default.HourglassEmpty, contentDescription = "Empty", modifier = Modifier.size(48.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                "No active chats found.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                "Tap + above to configure a conversation.",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filteredChats) { chat ->
                            ChatItemRow(
                                chat = chat,
                                onClick = { viewModel.selectChat(chat.id) },
                                onDelete = { viewModel.deleteChatAndMessages(chat.id) }
                            )
                        }
                    }
                }
            }
        } else {
            // --- Active Chatting Interface ---
            val activeChat = chats.find { it.id == activeChatId }
            if (activeChat != null) {
                ActiveChatView(
                    chat = activeChat,
                    messages = messages,
                    chattingStatus = chattingStatus,
                    onBack = { viewModel.selectChat(null) },
                    onSendMessage = { viewModel.sendChatMessage(it) },
                    onDeleteChat = {
                        viewModel.deleteChatAndMessages(activeChat.id)
                    }
                )
            }
        }
    }

    // --- Create New Chat Flow Dialog ---
    if (showNewChatDialog) {
        NewChatConfigDialog(
            onDismiss = { showNewChatDialog = false },
            onConfirm = { provider, model ->
                viewModel.createChat(provider, model)
                showNewChatDialog = false
            }
        )
    }
}

@Composable
fun ChatItemRow(
    chat: ChatItemEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Chat,
                        contentDescription = "Chat icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = chat.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                    Text(
                        text = "${UnifiedApiClient.getProviderLabel(chat.providerId)} (${chat.modelName})",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete Item", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f))
            }
        }
    }
}

@Composable
fun ActiveChatView(
    chat: ChatItemEntity,
    messages: List<ChatMessageEntity>,
    chattingStatus: String?,
    onBack: () -> Unit,
    onSendMessage: (String) -> Unit,
    onDeleteChat: () -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    val lazyListState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Scroll to bottom when message sizes increase
    LaunchedEffect(messages.size, chattingStatus) {
        if (messages.isNotEmpty()) {
            lazyListState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(4.dp))
                Column {
                    Text(
                        text = chat.title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        maxLines = 1
                    )
                    Text(
                        text = "${UnifiedApiClient.getProviderLabel(chat.providerId)} • ${chat.modelName}",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }

            IconButton(onClick = onDeleteChat) {
                Icon(Icons.Default.DeleteForever, contentDescription = "Delete Thread", tint = MaterialTheme.colorScheme.error)
            }
        }

        Divider()

        // Messages List
        Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (messages.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.ChatBubbleOutline,
                            contentDescription = "Begin chat",
                            modifier = Modifier.size(54.dp),
                            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Ask anything!",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Call live API models using securely stored parameters.",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    items(messages) { message ->
                        ChatMessageBubble(message = message)
                    }

                    // Typing placeholder
                    if (chattingStatus == "TYPING") {
                        item {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(start = 8.dp, top = 4.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.secondary)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Assistant is processing response...",
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }

        Divider()

        // Message Input Toolbar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Compose message...") },
                modifier = Modifier
                    .weight(1f)
                    .testTag("chat_input_text_field"),
                shape = RoundedCornerShape(24.dp),
                maxLines = 4,
                keyboardOptions = KeyboardOptions(
                    imeAction = ImeAction.Send
                ),
                keyboardActions = KeyboardActions(
                    onSend = {
                        if (textInput.trim().isNotEmpty()) {
                            onSendMessage(textInput)
                            textInput = ""
                        }
                    }
                )
            )

            Spacer(modifier = Modifier.width(8.dp))

            FloatingActionButton(
                onClick = {
                    if (textInput.trim().isNotEmpty()) {
                        onSendMessage(textInput)
                        textInput = ""
                    }
                },
                modifier = Modifier.size(48.dp).testTag("chat_send_button"),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
            }
        }
    }
}

@Composable
fun ChatMessageBubble(message: ChatMessageEntity) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.End else Alignment.Start
    val containerColor = if (isUser) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    }

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 2.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 2.dp, bottomEnd = 16.dp)
    }

    Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = alignment) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isUser) Icons.Default.Person else Icons.Default.SmartToy,
                contentDescription = null,
                modifier = Modifier.size(14.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isUser) "You" else "AI Assistant",
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
        }
        Spacer(modifier = Modifier.height(3.dp))
        Box(
            modifier = Modifier
                .widthIn(max = 280.dp)
                .clip(bubbleShape)
                .background(containerColor)
                .padding(12.dp)
        ) {
            RenderMarkdownText(text = message.content)
        }
    }
}

// Minimal Markdown Code rendering
@Composable
fun RenderMarkdownText(text: String) {
    // Detect code blocks
    if (text.contains("```") || text.contains("<code>")) {
        val tokens = text.split("```")
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            tokens.forEachIndexed { idx, token ->
                if (idx % 2 == 1) { // Inside Code block
                    CodeBlockItem(code = token.trim())
                } else {
                    MarkdownParagraph(text = token)
                }
            }
        }
    } else {
        MarkdownParagraph(text = text)
    }
}

@Composable
fun MarkdownParagraph(text: String) {
    // Renders bold sections dynamically
    val boldPattern = "\\*\\*(.*?)\\*\\*".toRegex()
    val matches = boldPattern.findAll(text)
    
    if (matches.any()) {
        // Simplified dynamic text mapping (since it is a demo, we can display neatly)
        Text(
            text = text.replace("**", ""), // Clean preview
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    } else {
        Text(
            text = text,
            fontSize = 14.sp,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun CodeBlockItem(code: String) {
    val context = LocalContext.current
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1E1E1E)
        ),
        shape = RoundedCornerShape(8.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Code block",
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    color = Color.Green
                )
                IconButton(
                    onClick = {
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clip = android.content.ClipData.newPlainText("Code snippet", code)
                        clipboard.setPrimaryClip(clip)
                        Toast.makeText(context, "Code copied!", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy text", tint = Color.LightGray, modifier = Modifier.size(14.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = code,
                fontSize = 12.sp,
                fontFamily = FontFamily.Monospace,
                color = Color.White,
                modifier = Modifier.horizontalScroll(rememberScrollState())
            )
        }
    }
}

// Dialog configuration for New conversations
@Composable
fun NewChatConfigDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit
) {
    val providers = listOf("gemini", "openai", "anthropic", "deepseek", "grok", "mistral")
    var selectedProvider by remember { mutableStateOf("gemini") }
    val models = UnifiedApiClient.getDefaultModelsForProvider(selectedProvider)
    var selectedModel by remember { mutableStateOf(models.first()) }

    // Synchronize default model when provider is updated
    LaunchedEffect(selectedProvider) {
        selectedModel = UnifiedApiClient.getDefaultModelsForProvider(selectedProvider).first()
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Configure Chat Thread") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Column {
                    Text("Select AI Provider", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        providers.forEach { p ->
                            val active = selectedProvider == p
                            FilterChip(
                                selected = active,
                                onClick = { selectedProvider = p },
                                label = { Text(UnifiedApiClient.getProviderLabel(p)) }
                            )
                        }
                    }
                }

                Column {
                    Text("Target Model Engine", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        models.forEach { m ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { selectedModel = m }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(selected = selectedModel == m, onClick = { selectedModel = m })
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(m, fontSize = 13.sp)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(selectedProvider, selectedModel) },
                modifier = Modifier.testTag("dialog_confirm_new_chat")
            ) {
                Text("Launch Thread")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
