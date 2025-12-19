package com.example.classpass.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.classpass.data.entity.ChatSession

/**
 * Data Access Object for ChatSession operations.
 * Manages CRUD operations for chat conversation sessions.
 */
@Dao
interface ChatSessionDao {
    
    /**
     * Get all sessions ordered by starred first, then by last updated (most recent first).
     */
    @Query("SELECT * FROM chat_sessions ORDER BY isStarred DESC, lastUpdated DESC")
    fun getAllSessions(): LiveData<List<ChatSession>>
    
    /**
     * Get recent sessions (for navigation drawer).
     * Starred chats appear first, then sorted by date.
     */
    @Query("SELECT * FROM chat_sessions ORDER BY isStarred DESC, lastUpdated DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 10): LiveData<List<ChatSession>>
    
    /**
     * Get all starred sessions.
     */
    @Query("SELECT * FROM chat_sessions WHERE isStarred = 1 ORDER BY lastUpdated DESC")
    fun getStarredSessions(): LiveData<List<ChatSession>>
    
    /**
     * Get the currently active session.
     */
    @Query("SELECT * FROM chat_sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveSession(): LiveData<ChatSession?>
    
    /**
     * Get the currently active session synchronously.
     */
    @Query("SELECT * FROM chat_sessions WHERE isActive = 1 LIMIT 1")
    suspend fun getActiveSessionSync(): ChatSession?
    
    /**
     * Get session by ID.
     */
    @Query("SELECT * FROM chat_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): ChatSession?
    
    /**
     * Get session by ID as LiveData.
     */
    @Query("SELECT * FROM chat_sessions WHERE sessionId = :sessionId")
    fun getSessionByIdLive(sessionId: String): LiveData<ChatSession?>
    
    /**
     * Insert a new session.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: ChatSession): Long
    
    /**
     * Update an existing session.
     */
    @Update
    suspend fun updateSession(session: ChatSession)
    
    /**
     * Update session title.
     */
    @Query("UPDATE chat_sessions SET title = :title WHERE sessionId = :sessionId")
    suspend fun updateSessionTitle(sessionId: String, title: String)
    
    /**
     * Toggle starred status for a session.
     */
    @Query("UPDATE chat_sessions SET isStarred = :isStarred WHERE sessionId = :sessionId")
    suspend fun updateStarredStatus(sessionId: String, isStarred: Boolean)
    
    /**
     * Update last updated timestamp.
     */
    @Query("UPDATE chat_sessions SET lastUpdated = :timestamp WHERE sessionId = :sessionId")
    suspend fun updateLastUpdated(sessionId: String, timestamp: Long)
    
    /**
     * Increment message count for a session.
     */
    @Query("UPDATE chat_sessions SET messageCount = messageCount + 1 WHERE sessionId = :sessionId")
    suspend fun incrementMessageCount(sessionId: String)
    
    /**
     * Deactivate all sessions (used before setting a new active session).
     */
    @Query("UPDATE chat_sessions SET isActive = 0")
    suspend fun deactivateAllSessions()
    
    /**
     * Set a session as active.
     */
    @Query("UPDATE chat_sessions SET isActive = 1 WHERE sessionId = :sessionId")
    suspend fun setActiveSession(sessionId: String)
    
    /**
     * Delete a session (cascades to messages via foreign key).
     */
    @Delete
    suspend fun deleteSession(session: ChatSession)
    
    /**
     * Delete all sessions.
     */
    @Query("DELETE FROM chat_sessions")
    suspend fun deleteAllSessions()
    
    /**
     * Get session count.
     */
    @Query("SELECT COUNT(*) FROM chat_sessions")
    suspend fun getSessionCount(): Int
}

