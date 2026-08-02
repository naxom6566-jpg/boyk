package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ApiKeyEntity
import com.example.data.model.ChatItemEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.GeneratedMediaEntity
import com.example.data.model.PromptTemplateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ApiKeyDao {
    @Query("SELECT * FROM api_keys ORDER BY providerId ASC")
    fun getAllApiKeys(): Flow<List<ApiKeyEntity>>

    @Query("SELECT * FROM api_keys WHERE id = :id LIMIT 1")
    suspend fun getApiKeyById(id: Long): ApiKeyEntity?

    @Query("SELECT * FROM api_keys WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveApiKey(): ApiKeyEntity?

    @Query("SELECT * FROM api_keys WHERE providerId = :providerId")
    suspend fun getApiKeysByProvider(providerId: String): List<ApiKeyEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApiKey(key: ApiKeyEntity): Long

    @Update
    suspend fun updateApiKey(key: ApiKeyEntity)

    @Query("UPDATE api_keys SET isActive = 0 WHERE providerId = :providerId")
    suspend fun deactivateAllForProvider(providerId: String)

    @Query("DELETE FROM api_keys WHERE id = :id")
    suspend fun deleteApiKeyById(id: Long)

    @Query("DELETE FROM api_keys")
    suspend fun deleteAllKeys()
}

@Dao
interface ChatDao {
    @Query("SELECT * FROM chat_items ORDER BY createdAt DESC")
    fun getAllChats(): Flow<List<ChatItemEntity>>

    @Query("SELECT * FROM chat_items WHERE id = :chatId LIMIT 1")
    suspend fun getChatById(chatId: Long): ChatItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChat(chat: ChatItemEntity): Long

    @Query("DELETE FROM chat_items WHERE id = :chatId")
    suspend fun deleteChatById(chatId: Long)

    @Query("SELECT * FROM chat_messages WHERE chatId = :chatId ORDER BY timestamp ASC")
    fun getMessagesForChat(chatId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Query("DELETE FROM chat_messages WHERE chatId = :chatId")
    suspend fun deleteMessagesForChat(chatId: Long)
}

@Dao
interface MediaDao {
    @Query("SELECT * FROM generated_media ORDER BY createdAt DESC")
    fun getAllMedia(): Flow<List<GeneratedMediaEntity>>

    @Query("SELECT * FROM generated_media WHERE type = :type ORDER BY createdAt DESC")
    fun getMediaByType(type: String): Flow<List<GeneratedMediaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMedia(media: GeneratedMediaEntity): Long

    @Query("UPDATE generated_media SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)

    @Query("DELETE FROM generated_media WHERE id = :id")
    suspend fun deleteMediaById(id: Long)

    @Query("DELETE FROM generated_media")
    suspend fun clearAllMedia()
}

@Dao
interface PromptDao {
    @Query("SELECT * FROM prompt_templates ORDER BY category ASC, title ASC")
    fun getAllPrompts(): Flow<List<PromptTemplateEntity>>

    @Query("SELECT * FROM prompt_templates WHERE isCustom = 1 ORDER BY title ASC")
    fun getCustomPrompts(): Flow<List<PromptTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPrompt(prompt: PromptTemplateEntity): Long

    @Query("UPDATE prompt_templates SET isFavorite = :isFav WHERE id = :id")
    suspend fun toggleFavorite(id: Long, isFav: Boolean)

    @Query("DELETE FROM prompt_templates WHERE id = :id")
    suspend fun deletePromptById(id: Long)

    @Query("SELECT COUNT(*) FROM prompt_templates")
    suspend fun getPromptsCount(): Int
}
