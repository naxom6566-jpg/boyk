package com.example.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.ApiKeyDao
import com.example.data.dao.ChatDao
import com.example.data.dao.MediaDao
import com.example.data.dao.PromptDao
import com.example.data.model.ApiKeyEntity
import com.example.data.model.ChatItemEntity
import com.example.data.model.ChatMessageEntity
import com.example.data.model.GeneratedMediaEntity
import com.example.data.model.PromptTemplateEntity

@Database(
    entities = [
        ApiKeyEntity::class,
        ChatItemEntity::class,
        ChatMessageEntity::class,
        GeneratedMediaEntity::class,
        PromptTemplateEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apiKeyDao(): ApiKeyDao
    abstract fun chatDao(): ChatDao
    abstract fun mediaDao(): MediaDao
    abstract fun promptDao(): PromptDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "byoa_assistant_database"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
