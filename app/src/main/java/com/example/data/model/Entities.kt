package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "api_keys")
data class ApiKeyEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val providerId: String, // e.g. "openai", "gemini", "anthropic", "deepseek", "grok"
    val nickname: String,
    val apiKey: String, // User's BYOA key (stored locally only)
    val isActive: Boolean = false,
    val status: String = "PENDING", // ACTIVE, LIMITED, ERROR, PENDING
    val lastChecked: Long = 0,
    val latencyMs: Long = 0,
    val availableModels: String = "" // Comma-separated or JSON list of models
)

@Entity(tableName = "chat_items")
data class ChatItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val providerId: String,
    val modelName: String,
    val createdAt: Long = System.currentTimeMillis(),
    val folderName: String? = null
)

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val chatId: Long,
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "generated_media")
data class GeneratedMediaEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String, // "IMAGE", "VIDEO", "AUDIO"
    val prompt: String,
    val url: String, // Base64 or local filepath/simulated mock output url
    val aspectRatio: String = "1:1",
    val style: String = "Standard",
    val durationSec: Int = 0,
    val voiceId: String = "",
    val isFavorite: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "prompt_templates")
data class PromptTemplateEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val description: String,
    val prompt: String,
    val category: String, // Writing, Coding, Business, Productivity, Education, Design, Social Media, AI Creation
    val isFavorite: Boolean = false,
    val isCustom: Boolean = false
)
