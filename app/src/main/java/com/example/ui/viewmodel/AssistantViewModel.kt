package com.example.ui.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ApiKeyEntity
import com.example.data.model.ChatItemEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.GeneratedMediaEntity
import com.example.data.model.PromptTemplateEntity
import com.example.data.repository.AppRepository
import com.example.network.UnifiedApiClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class AssistantViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AppRepository(application)

    // Current Tab
    private val _currentTab = MutableStateFlow("home")
    val currentTab = _currentTab.asStateFlow()

    // Database Flows
    val apiKeys: StateFlow<List<ApiKeyEntity>> = repository.allApiKeys
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val chats: StateFlow<List<ChatItemEntity>> = repository.allChats
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val prompts: StateFlow<List<PromptTemplateEntity>> = repository.allPrompts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val media: StateFlow<List<GeneratedMediaEntity>> = repository.allMedia
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Chat Selection
    private val _activeChatId = MutableStateFlow<Long?>(null)
    val activeChatId = _activeChatId.asStateFlow()

    val activeChatMessages: StateFlow<List<ChatMessageEntity>> = _activeChatId
        .flatMapLatest { id ->
            if (id != null) {
                repository.getMessagesForChat(id)
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Chat UI variables
    private val _chattingStatus = MutableStateFlow<String?>(null) // "TYPING", "ERROR", null
    val chattingStatus = _chattingStatus.asStateFlow()

    private val _lastChatError = MutableStateFlow<String?>(null)
    val lastChatError = _lastChatError.asStateFlow()

    // Testing / Diagnostics variables
    private val _validatorStatus = MutableStateFlow<Map<Long, UnifiedApiClient.ValidationResult>>(emptyMap())
    val validatorStatus = _validatorStatus.asStateFlow()

    private val _isTestingKeyId = MutableStateFlow<Long?>(null)
    val isTestingKeyId = _isTestingKeyId.asStateFlow()

    // Settings Configuration
    private val _darkMode = MutableStateFlow<String>("system") // "light", "dark", "system"
    val darkMode = _darkMode.asStateFlow()

    private val _importExportMessage = MutableStateFlow<String?>(null)
    val importExportMessage = _importExportMessage.asStateFlow()

    fun selectTab(tab: String) {
        _currentTab.value = tab
    }

    // --- API KEY ACTIONS ---
    fun addApiKey(providerId: String, nickname: String, apiKeyStr: String) {
        viewModelScope.launch {
            val key = ApiKeyEntity(
                providerId = providerId,
                nickname = if (nickname.trim().isEmpty()) UnifiedApiClient.getProviderLabel(providerId) else nickname,
                apiKey = apiKeyStr,
                isActive = false
            )
            repository.insertApiKey(key)
        }
    }

    fun deleteApiKey(id: Long) {
        viewModelScope.launch {
            repository.deleteApiKey(id)
        }
    }

    fun makeKeyActive(id: Long, providerId: String) {
        viewModelScope.launch {
            repository.selectActiveApiKey(id, providerId)
        }
    }

    fun testApiKey(keyEntity: ApiKeyEntity) {
        viewModelScope.launch {
            _isTestingKeyId.value = keyEntity.id
            val result = UnifiedApiClient.validateApiKey(keyEntity.providerId, keyEntity.apiKey)
            
            // Save check status to local database
            val updated = keyEntity.copy(
                status = result.status,
                lastChecked = System.currentTimeMillis(),
                latencyMs = result.latencyMs,
                availableModels = result.availableModels.joinToString(",")
            )
            repository.updateApiKey(updated)

            val currentMap = _validatorStatus.value.toMutableMap()
            currentMap[keyEntity.id] = result
            _validatorStatus.value = currentMap
            _isTestingKeyId.value = null
        }
    }

    // --- CHAT ACTIONS ---
    fun selectChat(chatId: Long?) {
        _activeChatId.value = chatId
        if (chatId != null) {
            _currentTab.value = "chat"
        }
    }

    fun createChat(providerId: String, model: String) {
        viewModelScope.launch {
            val label = UnifiedApiClient.getProviderLabel(providerId)
            val chatId = repository.createNewChat(
                title = "New Chat ($label)",
                providerId = providerId,
                modelName = model
            )
            selectChat(chatId)
        }
    }

    fun sendChatMessage(content: String) {
        val chatId = _activeChatId.value ?: return
        if (content.trim().isEmpty()) return

        viewModelScope.launch {
            // Write User message
            repository.insertMessage(chatId, "user", content)
            _chattingStatus.value = "TYPING"
            _lastChatError.value = null

            // Find current chat settings
            val chatItem = repository.allChats.stateIn(this).value.find { it.id == chatId }
            if (chatItem == null) {
                _chattingStatus.value = null
                return@launch
            }

            // Find an active API key for this provider
            val activeKeys = repository.allApiKeys.stateIn(this).value
            val activeKeyForProvider = activeKeys.find { it.providerId == chatItem.providerId && it.isActive }
                ?: activeKeys.find { it.providerId == chatItem.providerId } // Fallback to any key of provider

            if (activeKeyForProvider == null) {
                // Return a friendly assistant system guide if no custom key provided
                delay(1200)
                repository.insertMessage(
                    chatId = chatId,
                    role = "assistant",
                    content = """**Welcome to BYOA Assistant!**
                    
I am reacting in offline guidance mode because no active API Key is entered or toggled for **${UnifiedApiClient.getProviderLabel(chatItem.providerId)}**.

To start chatting:
1. Tap **Settings** ⚙️ or go to the **Home Panel** 🏠.
2. Under **Credentials**, tap **Add Custom API Key**.
3. Select this provider, enter your owned secret key, and save!
4. Activate the newly created key in the dashboard.

*Diagnostic Checklist:*
- Provider: `${chatItem.providerId}`
- Selected Model: `${chatItem.modelName}`
- Storage Mode: `Protected Local SQLite`"""
                )
                _chattingStatus.value = null
                return@launch
            }

            // Fetch chat history
            val messages = repository.getMessagesForChat(chatId).stateIn(this).value

            try {
                val response = UnifiedApiClient.getChatCompletion(
                    providerId = chatItem.providerId,
                    apiKey = activeKeyForProvider.apiKey,
                    model = chatItem.modelName,
                    messages = messages
                )
                repository.insertMessage(chatId, "assistant", response)
            } catch (e: Exception) {
                _lastChatError.value = e.localizedMessage
                repository.insertMessage(
                    chatId = chatId,
                    role = "assistant",
                    content = "⚠️ **API Connection Error:** ${e.localizedMessage}\n\nMake sure your key has balance quota and contains no trailing characters."
                )
            } finally {
                _chattingStatus.value = null
            }
        }
    }

    fun deleteChatAndMessages(chatId: Long) {
        viewModelScope.launch {
            if (_activeChatId.value == chatId) {
                _activeChatId.value = null
            }
            repository.deleteChat(chatId)
        }
    }

    // --- PROMPT LIBRARY ACTIONS ---
    fun togglePromptFav(id: Long, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoritePrompt(id, isFav)
        }
    }

    fun makeCustomPrompt(title: String, desc: String, text: String, cat: String) {
        viewModelScope.launch {
            val pr = PromptTemplateEntity(
                title = title,
                description = desc,
                prompt = text,
                category = cat,
                isCustom = true
            )
            repository.insertPrompt(pr)
        }
    }

    fun deleteCustomPrompt(id: Long) {
        viewModelScope.launch {
            repository.deletePrompt(id)
        }
    }

    // --- MULTIMEDIA GENERATION ---
    private val _isGeneratingMedia = MutableStateFlow(false)
    val isGeneratingMedia = _isGeneratingMedia.asStateFlow()

    fun generateImage(prompt: String, ratio: String, style: String) {
        viewModelScope.launch {
            _isGeneratingMedia.value = true
            delay(2000)

            // Dynamic algorithmic mock generator (gorgeous canvas vectors or colorful high-vibrancy placeholders)
            val mockUrl = "https://images.unsplash.com/photo-1618005182384-a83a8bd57fbe?q=80&w=600&auto=format&fit=crop" // Abstract render
            val selectedUrl = when (style.lowercase()) {
                "cyberpunk" -> "https://images.unsplash.com/photo-1607604276583-eef5d076aa5f?q=80&w=600&auto=format&fit=crop"
                "anime" -> "https://images.unsplash.com/photo-1578632767115-351597cf2477?q=80&w=600&auto=format&fit=crop"
                "cinematic" -> "https://images.unsplash.com/photo-1536440136628-849c177e76a1?q=80&w=600&auto=format&fit=crop"
                "surrealist" -> "https://images.unsplash.com/photo-1518709268805-4e9042af9f23?q=80&w=600&auto=format&fit=crop"
                "3d render" -> "https://images.unsplash.com/photo-1634017839464-5c339ebe3cb4?q=80&w=600&auto=format&fit=crop"
                else -> mockUrl
            }

            repository.insertMedia(
                type = "IMAGE",
                prompt = prompt,
                url = selectedUrl,
                aspectRatio = ratio,
                style = style
            )
            _isGeneratingMedia.value = false
        }
    }

    fun generateVideo(prompt: String, motion: String, duration: Int) {
        viewModelScope.launch {
            _isGeneratingMedia.value = true
            delay(2500)

            val sourceVideoUrls = listOf(
                "https://assets.mixkit.co/videos/preview/mixkit-nebula-of-outer-space-background-42173-large.mp4",
                "https://assets.mixkit.co/videos/preview/mixkit-particles-glowing-in-the-plasma-current-43187-large.mp4"
            )
            val selectedVideoUrl = sourceVideoUrls.random()

            repository.insertMedia(
                type = "VIDEO",
                prompt = prompt,
                url = selectedVideoUrl,
                style = motion,
                durationSec = duration
            )
            _isGeneratingMedia.value = false
        }
    }

    fun generateAudio(prompt: String, voice: String, speed: Float) {
        viewModelScope.launch {
            _isGeneratingMedia.value = true
            delay(1500)

            // Simulated base64 or relative media playback
            val mockAudio = "https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"

            repository.insertMedia(
                type = "AUDIO",
                prompt = prompt,
                url = mockAudio,
                style = "Speed: ${speed}x",
                voiceId = voice
            )
            _isGeneratingMedia.value = false
        }
    }

    fun toggleMediaFavorite(id: Long, isFav: Boolean) {
        viewModelScope.launch {
            repository.toggleFavoriteMedia(id, isFav)
        }
    }

    fun deleteMediaItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMedia(id)
        }
    }

    // --- DATA MANAGEMENT ---
    fun selectTheme(mode: String) {
        _darkMode.value = mode
    }

    fun clearAppDataCache() {
        viewModelScope.launch {
            repository.clearAllKeys()
            repository.clearAllMedia()
            // Clears keys and media. Can rebuild prompts again
        }
    }

    fun exportLocalDataToJson(): String? {
        return try {
            val root = JSONObject()
            val keyList = apiKeys.value
            val keyArr = JSONArray()
            for (k in keyList) {
                val kObj = JSONObject()
                kObj.put("providerId", k.providerId)
                kObj.put("nickname", k.nickname)
                kObj.put("apiKey", k.apiKey)
                keyArr.put(kObj)
            }
            root.put("keys", keyArr)
            _importExportMessage.value = "Data Export Succeeded! Core configurations saved."
            root.toString()
        } catch (e: Exception) {
            _importExportMessage.value = "Export failed: ${e.message}"
            null
        }
    }

    fun importLocalDataFromJson(jsonStr: String): Boolean {
        return try {
            val root = JSONObject(jsonStr)
            if (root.has("keys")) {
                val keys = root.getJSONArray("keys")
                for (i in 0 until keys.length()) {
                    val k = keys.getJSONObject(i)
                    addApiKey(
                        providerId = k.getString("providerId"),
                        nickname = k.getString("nickname"),
                        apiKeyStr = k.getString("apiKey")
                    )
                }
                _importExportMessage.value = "Import Succeeded! API Keys parsed: ${keys.length()}"
                return true
            }
            false
        } catch (e: Exception) {
            _importExportMessage.value = "Import failed: Parsing error."
            false
        }
    }

    fun clearImportExportMessage() {
        _importExportMessage.value = null
    }
}
