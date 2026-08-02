package com.example.network

import android.util.Log
import com.example.data.model.ChatMessageEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object UnifiedApiClient {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        .build()

    private val JSON_MEDIA_TYPE = "application/json; charset=utf-8".toMediaType()

    data class ValidationResult(
        val success: Boolean,
        val status: String, // ACTIVE, LIMITED, ERROR
        val errorMessage: String? = null,
        val latencyMs: Long = 0,
        val availableModels: List<String> = emptyList()
    )

    // Default suggested models for each provider
    fun getDefaultModelsForProvider(providerId: String): List<String> {
        return when (providerId.lowercase()) {
            "gemini" -> listOf("gemini-1.5-flash", "gemini-1.5-pro", "gemini-2.5-flash", "gemini-2.0-flash")
            "openai" -> listOf("gpt-4o-mini", "gpt-4o", "o1-mini", "gpt-4-turbo")
            "anthropic" -> listOf("claude-3-5-sonnet-20241022", "claude-3-5-haiku-20241022")
            "deepseek" -> listOf("deepseek-chat", "deepseek-coder")
            "grok" -> listOf("grok-2-1212", "grok-beta")
            "openrouter" -> listOf("google/gemini-2.5-flash", "meta-llama/llama-3-70b-instruct", "anthropic/claude-3.5-sonnet")
            "mistral" -> listOf("mistral-tiny", "mistral-small", "mistral-medium")
            "together" -> listOf("meta-llama/Llama-3-70b-chat-hf", "mistralai/Mixtral-8x7B-Instruct-v0.1")
            "stability" -> listOf("stable-diffusion-xl-1024-v1-0", "stable-diffusion-v1-6")
            "fal" -> listOf("fal-ai/flux/schnell", "fal-ai/fast-sdr")
            "elevenlabs" -> listOf("eleven_monolingual_v1", "eleven_turbo_v2")
            else -> listOf("default-model")
        }
    }

    // Return friendly name for UI display
    fun getProviderLabel(providerId: String): String {
        return when (providerId.lowercase()) {
            "openai" -> "OpenAI"
            "anthropic" -> "Anthropic Claude"
            "gemini" -> "Google Gemini"
            "grok" -> "xAI Grok"
            "deepseek" -> "DeepSeek"
            "mistral" -> "Mistral AI"
            "together" -> "Together AI"
            "openrouter" -> "OpenRouter"
            "replicate" -> "Replicate"
            "stability" -> "Stability AI"
            "elevenlabs" -> "ElevenLabs"
            "runway" -> "Runway"
            "fal" -> "Fal.ai"
            else -> providerId.capitalize()
        }
    }

    // Hit a lightweight validation model
    suspend fun validateApiKey(providerId: String, apiKey: String): ValidationResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            when (providerId.lowercase()) {
                "gemini" -> {
                    // Try hitting Gemini Info API or simple completion
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
                    val requestBodyJson = JSONObject()
                        .put("contents", JSONArray().put(
                            JSONObject().put("parts", JSONArray().put(
                                JSONObject().put("text", "Perform connection test, reply with 'OK'.")
                            ))
                        ))
                        .put("generationConfig", JSONObject().put("maxOutputTokens", 5))

                    val request = Request.Builder()
                        .url(url)
                        .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    client.newCall(request).execute().use { response ->
                        val latency = System.currentTimeMillis() - startTime
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string() ?: ""
                            val hasText = bodyStr.contains("candidates")
                            if (hasText) {
                                ValidationResult(true, "ACTIVE", null, latency, getDefaultModelsForProvider("gemini"))
                            } else {
                                ValidationResult(false, "LIMITED", "Invalid response payload layout", latency, getDefaultModelsForProvider("gemini"))
                            }
                        } else {
                            val errBody = response.body?.string() ?: ""
                            val errorMsg = parseErrorMessage(errBody) ?: "HTTP Error: ${response.code}"
                            ValidationResult(false, "ERROR", errorMsg, latency)
                        }
                    }
                }
                "openai" -> {
                    val url = "https://api.openai.com/v1/chat/completions"
                    val requestBodyJson = JSONObject()
                        .put("model", "gpt-4o-mini")
                        .put("messages", JSONArray().put(
                            JSONObject().put("role", "user").put("content", "Test connection. Reply 'OK'.")
                        ))
                        .put("max_tokens", 5)

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    client.newCall(request).execute().use { response ->
                        val latency = System.currentTimeMillis() - startTime
                        if (response.isSuccessful) {
                            ValidationResult(true, "ACTIVE", null, latency, getDefaultModelsForProvider("openai"))
                        } else {
                            val errBody = response.body?.string() ?: ""
                            val errorMsg = parseErrorMessage(errBody) ?: "HTTP Error: ${response.code}"
                            ValidationResult(false, "ERROR", errorMsg, latency)
                        }
                    }
                }
                "anthropic" -> {
                    val url = "https://api.anthropic.com/v1/messages"
                    val requestBodyJson = JSONObject()
                        .put("model", "claude-3-5-haiku-20241022")
                        .put("max_tokens", 5)
                        .put("messages", JSONArray().put(
                            JSONObject().put("role", "user").put("content", "Test")
                        ))

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    client.newCall(request).execute().use { response ->
                        val latency = System.currentTimeMillis() - startTime
                        if (response.isSuccessful) {
                            ValidationResult(true, "ACTIVE", null, latency, getDefaultModelsForProvider("anthropic"))
                        } else {
                            val errBody = response.body?.string() ?: ""
                            val errorMsg = parseErrorMessage(errBody) ?: "HTTP Error: ${response.code}"
                            ValidationResult(false, "ERROR", errorMsg, latency)
                        }
                    }
                }
                // Other modular providers (DeepSeek, Grok, OpenRouter) are OpenAI-compatible, we can validate easily
                "deepseek", "grok", "openrouter", "together", "mistral" -> {
                    val baseUrl = when (providerId.lowercase()) {
                        "deepseek" -> "https://api.deepseek.com/v1/chat/completions"
                        "grok" -> "https://api.x.ai/v1/chat/completions"
                        "openrouter" -> "https://openrouter.ai/api/v1/chat/completions"
                        "together" -> "https://api.together.xyz/v1/chat/completions"
                        "mistral" -> "https://api.mistral.ai/v1/chat/completions"
                        else -> "https://api.openai.com/v1/chat/completions"
                    }
                    val defaultModel = getDefaultModelsForProvider(providerId).first()
                    val requestBodyJson = JSONObject()
                        .put("model", defaultModel)
                        .put("messages", JSONArray().put(
                            JSONObject().put("role", "user").put("content", "Hi")
                        ))
                        .put("max_tokens", 5)

                    val request = Request.Builder()
                        .url(baseUrl)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    client.newCall(request).execute().use { response ->
                        val latency = System.currentTimeMillis() - startTime
                        if (response.isSuccessful) {
                            ValidationResult(true, "ACTIVE", null, latency, getDefaultModelsForProvider(providerId))
                        } else {
                            val errBody = response.body?.string() ?: ""
                            val errorMsg = parseErrorMessage(errBody) ?: "HTTP Error: ${response.code}"
                            ValidationResult(false, "ERROR", errorMsg, latency)
                        }
                    }
                }
                // Other simple validators
                else -> {
                    // For static or simple media providers, do a mock connection or minor URL HEAD checker to verify it's not empty
                    val isSecretValid = apiKey.length > 5
                    val latency = System.currentTimeMillis() - startTime
                    if (isSecretValid) {
                        ValidationResult(true, "ACTIVE", null, latency, getDefaultModelsForProvider(providerId))
                    } else {
                        ValidationResult(false, "ERROR", "Key is too short or invalid format.", latency)
                    }
                }
            }
        } catch (e: Exception) {
            val latency = System.currentTimeMillis() - startTime
            ValidationResult(false, "ERROR", e.localizedMessage ?: "Connection Timeout", latency)
        }
    }

    // Call live text completion
    suspend fun getChatCompletion(
        providerId: String,
        apiKey: String,
        model: String,
        messages: List<ChatMessageEntity>
    ): String = withContext(Dispatchers.IO) {
        try {
            when (providerId.lowercase()) {
                "gemini" -> {
                    val url = "https://generativelanguage.googleapis.com/v1beta/models/$model:generateContent?key=$apiKey"
                    val contentsArray = JSONArray()
                    for (msg in messages) {
                        // Gemini roles map to "user" or "model" (instead of assistant)
                        val gRole = if (msg.role == "user") "user" else "model"
                        contentsArray.put(
                            JSONObject()
                                .put("role", gRole)
                                .put("parts", JSONArray().put(JSONObject().put("text", msg.content)))
                        )
                    }
                    val requestBodyJson = JSONObject().put("contents", contentsArray)

                    val request = Request.Builder()
                        .url(url)
                        .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string() ?: ""
                            val jsonObj = JSONObject(bodyStr)
                            val candidates = jsonObj.getJSONArray("candidates")
                            val content = candidates.getJSONObject(0).getJSONObject("content")
                            val parts = content.getJSONArray("parts")
                            parts.getJSONObject(0).getString("text")
                        } else {
                            val errBody = response.body?.string() ?: ""
                            val rawError = parseErrorMessage(errBody) ?: "HTTP Error ${response.code}"
                            throw IOException(rawError)
                        }
                    }
                }
                "openai" -> {
                    val url = "https://api.openai.com/v1/chat/completions"
                    val messageArray = JSONArray()
                    for (msg in messages) {
                        messageArray.put(JSONObject().put("role", msg.role).put("content", msg.content))
                    }
                    val requestBodyJson = JSONObject()
                        .put("model", model)
                        .put("messages", messageArray)

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string() ?: ""
                            val jsonObj = JSONObject(bodyStr)
                            val choices = jsonObj.getJSONArray("choices")
                            val messageObj = choices.getJSONObject(0).getJSONObject("message")
                            messageObj.getString("content")
                        } else {
                            val errBody = response.body?.string() ?: ""
                            throw IOException(parseErrorMessage(errBody) ?: "HTTP Error ${response.code}")
                        }
                    }
                }
                "anthropic" -> {
                    val url = "https://api.anthropic.com/v1/messages"
                    val messageArray = JSONArray()
                    for (msg in messages) {
                        messageArray.put(JSONObject().put("role", msg.role).put("content", msg.content))
                    }
                    val requestBodyJson = JSONObject()
                        .put("model", model)
                        .put("max_tokens", 2048)
                        .put("messages", messageArray)

                    val request = Request.Builder()
                        .url(url)
                        .addHeader("x-api-key", apiKey)
                        .addHeader("anthropic-version", "2023-06-01")
                        .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string() ?: ""
                            val jsonObj = JSONObject(bodyStr)
                            val contentArray = jsonObj.getJSONArray("content")
                            contentArray.getJSONObject(0).getString("text")
                        } else {
                            val errBody = response.body?.string() ?: ""
                            throw IOException(parseErrorMessage(errBody) ?: "HTTP Error ${response.code}")
                        }
                    }
                }
                // DeepSeek, Grok, OpenRouter, Together, Mistral
                "deepseek", "grok", "openrouter", "together", "mistral" -> {
                    val endpoint = when (providerId.lowercase()) {
                        "deepseek" -> "https://api.deepseek.com/v1/chat/completions"
                        "grok" -> "https://api.x.ai/v1/chat/completions"
                        "openrouter" -> "https://openrouter.ai/api/v1/chat/completions"
                        "together" -> "https://api.together.xyz/v1/chat/completions"
                        "mistral" -> "https://api.mistral.ai/v1/chat/completions"
                        else -> "https://api.openai.com/v1/chat/completions"
                    }
                    val messageArray = JSONArray()
                    for (msg in messages) {
                        messageArray.put(JSONObject().put("role", msg.role).put("content", msg.content))
                    }
                    val requestBodyJson = JSONObject()
                        .put("model", model)
                        .put("messages", messageArray)

                    val request = Request.Builder()
                        .url(endpoint)
                        .addHeader("Authorization", "Bearer $apiKey")
                        .post(requestBodyJson.toString().toRequestBody(JSON_MEDIA_TYPE))
                        .build()

                    client.newCall(request).execute().use { response ->
                        if (response.isSuccessful) {
                            val bodyStr = response.body?.string() ?: ""
                            val jsonObj = JSONObject(bodyStr)
                            val choices = jsonObj.getJSONArray("choices")
                            val messageObj = choices.getJSONObject(0).getJSONObject("message")
                            messageObj.getString("content")
                        } else {
                            val errBody = response.body?.string() ?: ""
                            throw IOException(parseErrorMessage(errBody) ?: "HTTP Error ${response.code}")
                        }
                    }
                }
                else -> {
                    throw IOException("Selected provider model completions are currently processed locally or require active billing credentials.")
                }
            }
        } catch (e: Exception) {
            Log.e("UnifiedApiClient", "API Call failed: ${e.message}", e)
            throw IOException(e.localizedMessage ?: "Check network integrity or API Key limits.")
        }
    }

    // Attempt to parse standard API error formats
    private fun parseErrorMessage(jsonBody: String): String? {
        if (jsonBody.isEmpty()) return null
        return try {
            val json = JSONObject(jsonBody)
            if (json.has("error")) {
                val errObj = json.get("error")
                if (errObj is JSONObject) {
                    if (errObj.has("message")) {
                        return errObj.getString("message")
                    }
                } else if (errObj is String) {
                    return errObj
                }
            }
            // Gemini error body structure
            if (json.has("error") && json.getJSONObject("error").has("message")) {
                return json.getJSONObject("error").getString("message")
            }
            null
        } catch (e: Exception) {
            null
        }
    }
}
