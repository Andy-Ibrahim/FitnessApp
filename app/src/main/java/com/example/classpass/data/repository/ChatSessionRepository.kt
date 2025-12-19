package com.example.classpass.data.repository

import androidx.lifecycle.LiveData
import com.example.classpass.data.dao.ChatMessageDao
import com.example.classpass.data.dao.ChatSessionDao
import com.example.classpass.data.entity.ChatMessage
import com.example.classpass.data.entity.ChatSession

/**
 * Repository for managing chat sessions.
 * Handles business logic for creating, updating, and deleting chat sessions.
 */
class ChatSessionRepository(
    private val sessionDao: ChatSessionDao,
    private val messageDao: ChatMessageDao
) {
    
    /**
     * Observe all sessions ordered by last updated.
     */
    val allSessions: LiveData<List<ChatSession>> = sessionDao.getAllSessions()
    
    /**
     * Observe recent sessions (for navigation drawer).
     */
    fun getRecentSessions(limit: Int = 10): LiveData<List<ChatSession>> {
        return sessionDao.getRecentSessions(limit)
    }
    
    /**
     * Observe the currently active session.
     */
    val activeSession: LiveData<ChatSession?> = sessionDao.getActiveSession()
    
    /**
     * Get active session synchronously.
     */
    suspend fun getActiveSessionSync(): ChatSession? {
        return sessionDao.getActiveSessionSync()
    }
    
    /**
     * Get session by ID.
     */
    suspend fun getSessionById(sessionId: String): ChatSession? {
        return sessionDao.getSessionById(sessionId)
    }
    
    /**
     * Create a new chat session and set it as active.
     */
    suspend fun createNewSession(): ChatSession {
        val newSession = ChatSession()
        sessionDao.insertSession(newSession)
        setActiveSession(newSession.sessionId)
        return newSession
    }
    
    /**
     * Set a session as the active session (deactivates all others).
     */
    suspend fun setActiveSession(sessionId: String) {
        sessionDao.deactivateAllSessions()
        sessionDao.setActiveSession(sessionId)
    }
    
    /**
     * Update session title.
     */
    suspend fun updateSessionTitle(sessionId: String, title: String) {
        sessionDao.updateSessionTitle(sessionId, title)
    }
    
    /**
     * Toggle starred status for a session.
     */
    suspend fun toggleStarred(sessionId: String, isStarred: Boolean) {
        sessionDao.updateStarredStatus(sessionId, isStarred)
    }
    
    /**
     * Observe starred sessions.
     */
    val starredSessions: LiveData<List<ChatSession>> = sessionDao.getStarredSessions()
    
    /**
     * Update session after a new message is added.
     * Updates last updated timestamp and increments message count.
     */
    suspend fun updateSessionAfterMessage(sessionId: String) {
        sessionDao.updateLastUpdated(sessionId, System.currentTimeMillis())
        sessionDao.incrementMessageCount(sessionId)
    }
    
    /**
     * Delete a session (cascades to messages via foreign key).
     */
    suspend fun deleteSession(session: ChatSession) {
        sessionDao.deleteSession(session)
    }
    
    /**
     * Get session with its last message (for preview in chat list).
     */
    suspend fun getSessionWithPreview(sessionId: String): Pair<ChatSession?, ChatMessage?> {
        val session = sessionDao.getSessionById(sessionId)
        val lastMessage = messageDao.getLastMessageForSession(sessionId)
        return Pair(session, lastMessage)
    }
    
    /**
     * Get session count.
     */
    suspend fun getSessionCount(): Int {
        return sessionDao.getSessionCount()
    }
    
    /**
     * Check if there are any sessions.
     */
    suspend fun hasSessions(): Boolean {
        return getSessionCount() > 0
    }
}

