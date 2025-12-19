package com.example.classpass.data.repository

import androidx.lifecycle.LiveData
import com.example.classpass.data.dao.ChatMessageDao
import com.example.classpass.data.entity.ChatMessage

/**
 * Repository for managing chat messages.
 * Handles business logic for creating, reading, and deleting messages.
 */
class ChatMessageRepository(private val chatDao: ChatMessageDao) {
    
    /**
     * Get all messages for a specific session.
     */
    fun getMessagesForSession(sessionId: String): LiveData<List<ChatMessage>> {
        return chatDao.getMessagesForSession(sessionId)
    }
    
    /**
     * Get all messages (for backward compatibility or migration).
     */
    fun getAllMessages(): LiveData<List<ChatMessage>> {
        return chatDao.getAllMessages()
    }
    
    /**
     * Get the last message for a session.
     */
    suspend fun getLastMessageForSession(sessionId: String): ChatMessage? {
        return chatDao.getLastMessageForSession(sessionId)
    }
    
    /**
     * Get message count for a session.
     */
    suspend fun getMessageCountForSession(sessionId: String): Int {
        return chatDao.getMessageCountForSession(sessionId)
    }
    
    /**
     * Insert a new message.
     */
    suspend fun insertMessage(message: ChatMessage): Long {
        return chatDao.insert(message)
    }
    
    /**
     * Delete a specific message.
     */
    suspend fun deleteMessage(message: ChatMessage) {
        chatDao.delete(message)
    }
    
    /**
     * Delete all messages for a session.
     */
    suspend fun deleteMessagesForSession(sessionId: String) {
        chatDao.deleteMessagesForSession(sessionId)
    }
    
    /**
     * Delete all messages.
     */
    suspend fun deleteAll() {
        chatDao.deleteAll()
    }
}
