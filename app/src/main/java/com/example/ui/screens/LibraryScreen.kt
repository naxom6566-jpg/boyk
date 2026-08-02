package com.example.ui.screens

import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PromptTemplateEntity
import com.example.ui.viewmodel.AssistantViewModel

@Composable
fun LibraryScreen(viewModel: AssistantViewModel) {
    val prompts by viewModel.prompts.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var showCreatePromptDialog by remember { mutableStateOf(false) }

    val categories = listOf("All", "Writing", "Coding", "Business", "Productivity", "Education", "Design", "Social Media", "AI Creation")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("library_screen")
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Prompt Engineering Library",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${prompts.size} Templates preconfigured & ready.",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }

            IconButton(
                onClick = { showCreatePromptDialog = true },
                modifier = Modifier.testTag("custom_prompt_add_button")
            ) {
                Icon(Icons.Default.PostAdd, contentDescription = "Add Custom Prompt", tint = MaterialTheme.colorScheme.primary)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search engineering templates...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            modifier = Modifier.fillMaxWidth().testTag("prompt_search_bar"),
            shape = RoundedCornerShape(12.dp)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Category Pills row
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(categories) { category ->
                val selected = selectedCategory == category
                FilterChip(
                    selected = selected,
                    onClick = { selectedCategory = category },
                    label = { Text(category) }
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Prompts Filters Execution
        val filteredPrompts = prompts.filter {
            (selectedCategory == "All" || it.category.equals(selectedCategory, ignoreCase = true)) &&
                    (it.title.contains(searchQuery, ignoreCase = true) ||
                            it.description.contains(searchQuery, ignoreCase = true) ||
                            it.prompt.contains(searchQuery, ignoreCase = true))
        }

        if (filteredPrompts.isEmpty()) {
            Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Source,
                        contentDescription = "Empty list",
                        modifier = Modifier.size(48.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "No prompts fit filter criteria.",
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(filteredPrompts) { prompt ->
                    PromptTemplateCard(
                        prompt = prompt,
                        onToggleFav = { viewModel.togglePromptFav(prompt.id, !prompt.isFavorite) },
                        onDeleteCustom = { viewModel.deleteCustomPrompt(prompt.id) }
                    )
                }
            }
        }
    }

    if (showCreatePromptDialog) {
        CreateCustomPromptDialog(
            categories = categories.filter { it != "All" },
            onDismiss = { showCreatePromptDialog = false },
            onSave = { title, desc, promptText, category ->
                viewModel.makeCustomPrompt(title, desc, promptText, category)
                showCreatePromptDialog = false
            }
        )
    }
}

@Composable
fun PromptTemplateCard(
    prompt: PromptTemplateEntity,
    onToggleFav: () -> Unit,
    onDeleteCustom: () -> Unit
) {
    val context = LocalContext.current
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { isExpanded = !isExpanded },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = prompt.category,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        if (prompt.isCustom) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.12f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "Custom User",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.secondary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = prompt.title,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row {
                    IconButton(onClick = onToggleFav) {
                        Icon(
                            imageVector = if (prompt.isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                            contentDescription = "Fav",
                            tint = if (prompt.isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline
                        )
                    }

                    if (prompt.isCustom) {
                        IconButton(onClick = onDeleteCustom) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete Prompt", tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f))
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = prompt.description,
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f),
                maxLines = if (isExpanded) Int.MAX_VALUE else 2,
                overflow = TextOverflow.Ellipsis
            )

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    Divider()
                    Spacer(modifier = Modifier.height(10.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = prompt.prompt,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Button(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = android.content.ClipData.newPlainText("Prompt template", prompt.prompt)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, "Prompt copied to clipboard!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.align(Alignment.End),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = "Copy text")
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy Snippet Payload")
                    }
                }
            }
        }
    }
}

@Composable
fun CreateCustomPromptDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onSave: (title: String, desc: String, text: String, cat: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var text by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories.first()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Compile Custom Prompt Template") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    placeholder = { Text("Template Title (e.g. My SEO Builder)") },
                    modifier = Modifier.fillMaxWidth().testTag("custom_prompt_title_input")
                )

                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    placeholder = { Text("Brief Description of usage...") },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    placeholder = { Text("AI Prompt Text (Use placeholders like <topic> for modular actions)") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    maxLines = 6
                )

                Column {
                    Text("Select Category", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        categories.forEach { c ->
                            val selected = selectedCategory == c
                            ElevatedFilterChip(
                                selected = selected,
                                onClick = { selectedCategory = c },
                                label = { Text(c) }
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.trim().isNotEmpty() && text.trim().isNotEmpty()) {
                        onSave(title, desc, text, selectedCategory)
                    }
                },
                modifier = Modifier.testTag("custom_prompt_confirm_button")
            ) {
                Text("Register Prompt")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
