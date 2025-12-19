package com.example.classpass.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.classpass.data.entity.ChatMessage

/**
 * Data Access Object for ChatMessage operations.
 * Manages CRUD operations for chat messages within sessions.
 */
@Dao
interface ChatMessageDao {
    
    /**
     * Get all messages for a specific session, ordered by timestamp.
     */
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp ASC")
    fun getMessagesForSession(sessionId: String): LiveData<List<ChatMessage>>
    
    /**
     * Get all messages (for backward compatibility or migration).
     */
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): LiveData<List<ChatMessage>>
    
    /**
     * Get the last message for a session (for preview in chat list).
     */
    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessageForSession(sessionId: String): ChatMessage?
    
    /**
     * Get message count for a session.
     */
    @Query("SELECT COUNT(*) FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun getMessageCountForSession(sessionId: String): Int
    
    /**
     * Insert a new message.
     */
    @Insert
    suspend fun insert(message: ChatMessage): Long
    
    /**
     * Delete a specific message.
     */
    @Delete
    suspend fun delete(message: ChatMessage)
    
    /**
     * Delete all messages for a specific session.
     */
    @Query("DELETE FROM chat_messages WHERE sessionId = :sessionId")
    suspend fun deleteMessagesForSession(sessionId: String)
    
    /**
     * Delete all messages.
     */
    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
