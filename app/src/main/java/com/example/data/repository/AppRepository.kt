package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.data.database.AppDatabase
import com.example.data.model.ApiKeyEntity
import com.example.data.model.ChatItemEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.GeneratedMediaEntity
import com.example.data.model.PromptTemplateEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AppRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val apiKeyDao = database.apiKeyDao()
    private val chatDao = database.chatDao()
    private val mediaDao = database.mediaDao()
    private val promptDao = database.promptDao()

    init {
        // Seed standard prompts if empty
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val count = promptDao.getPromptsCount()
                if (count == 0) {
                    Log.d("AppRepository", "Seeding ${DefaultPrompts.list.size} prompts...")
                    for (prompt in DefaultPrompts.list) {
                        promptDao.insertPrompt(prompt)
                    }
                    Log.d("AppRepository", "Prompt Seeding complete.")
                }
            } catch (e: Exception) {
                Log.e("AppRepository", "Failed to seed prompts: ${e.message}", e)
            }
        }
    }

    // --- API KEYS ---
    val allApiKeys: Flow<List<ApiKeyEntity>> = apiKeyDao.getAllApiKeys()

    suspend fun getActiveApiKey(): ApiKeyEntity? = withContext(Dispatchers.IO) {
        apiKeyDao.getActiveApiKey()
    }

    suspend fun selectActiveApiKey(id: Long, providerId: String) = withContext(Dispatchers.IO) {
        // Deactivate all for provider first to ensure 1 active key per provider, or global activation toggles
        apiKeyDao.deactivateAllForProvider(providerId)
        val key = apiKeyDao.getApiKeyById(id)
        if (key != null) {
            apiKeyDao.updateApiKey(key.copy(isActive = true))
        }
    }

    suspend fun insertApiKey(key: ApiKeyEntity): Long = withContext(Dispatchers.IO) {
        // If it's the first key of this provider, make it active
        val existing = apiKeyDao.getApiKeysByProvider(key.providerId)
        val shouldBeActive = existing.isEmpty() || key.isActive
        if (shouldBeActive) {
            apiKeyDao.deactivateAllForProvider(key.providerId)
        }
        apiKeyDao.insertApiKey(key.copy(isActive = shouldBeActive))
    }

    suspend fun updateApiKey(key: ApiKeyEntity) = withContext(Dispatchers.IO) {
        apiKeyDao.updateApiKey(key)
    }

    suspend fun deleteApiKey(id: Long) = withContext(Dispatchers.IO) {
        apiKeyDao.deleteApiKeyById(id)
    }

    suspend fun getKeysByProvider(providerId: String): List<ApiKeyEntity> = withContext(Dispatchers.IO) {
        apiKeyDao.getApiKeysByProvider(providerId)
    }

    suspend fun clearAllKeys() = withContext(Dispatchers.IO) {
        apiKeyDao.deleteAllKeys()
    }

    // --- CHATS & MESSAGES ---
    val allChats: Flow<List<ChatItemEntity>> = chatDao.getAllChats()

    fun getMessagesForChat(chatId: Long): Flow<List<ChatMessageEntity>> {
        return chatDao.getMessagesForChat(chatId)
    }

    suspend fun createNewChat(title: String, providerId: String, modelName: String): Long = withContext(Dispatchers.IO) {
        chatDao.insertChat(ChatItemEntity(title = title, providerId = providerId, modelName = modelName))
    }

    suspend fun deleteChat(chatId: Long) = withContext(Dispatchers.IO) {
        chatDao.deleteChatById(chatId)
        chatDao.deleteMessagesForChat(chatId)
    }

    suspend fun insertMessage(chatId: Long, role: String, content: String): Long = withContext(Dispatchers.IO) {
        // Update chat's updated time could be tracked, but here we just append the message
        chatDao.insertMessage(ChatMessageEntity(chatId = chatId, role = role, content = content))
    }

    // --- MEDIA ---
    val allMedia: Flow<List<GeneratedMediaEntity>> = mediaDao.getAllMedia()

    fun getMediaByType(type: String): Flow<List<GeneratedMediaEntity>> {
        return mediaDao.getMediaByType(type)
    }

    suspend fun insertMedia(type: String, prompt: String, url: String, aspectRatio: String = "1:1", style: String = "Standard", durationSec: Int = 0, voiceId: String = ""): Long = withContext(Dispatchers.IO) {
        mediaDao.insertMedia(
            GeneratedMediaEntity(
                type = type,
                prompt = prompt,
                url = url,
                aspectRatio = aspectRatio,
                style = style,
                durationSec = durationSec,
                voiceId = voiceId
            )
        )
    }

    suspend fun toggleFavoriteMedia(id: Long, isFav: Boolean) = withContext(Dispatchers.IO) {
        mediaDao.toggleFavorite(id, isFav)
    }

    suspend fun deleteMedia(id: Long) = withContext(Dispatchers.IO) {
        mediaDao.deleteMediaById(id)
    }

    suspend fun clearAllMedia() = withContext(Dispatchers.IO) {
        mediaDao.clearAllMedia()
    }

    // --- PROMPTS ---
    val allPrompts: Flow<List<PromptTemplateEntity>> = promptDao.getAllPrompts()
    val customPrompts: Flow<List<PromptTemplateEntity>> = promptDao.getCustomPrompts()

    suspend fun insertPrompt(prompt: PromptTemplateEntity): Long = withContext(Dispatchers.IO) {
        promptDao.insertPrompt(prompt)
    }

    suspend fun toggleFavoritePrompt(id: Long, isFav: Boolean) = withContext(Dispatchers.IO) {
        promptDao.toggleFavorite(id, isFav)
    }

    suspend fun deletePrompt(id: Long) = withContext(Dispatchers.IO) {
        promptDao.deletePromptById(id)
    }
}
