package com.example.classpass.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.classpass.ClassPassApplication
import com.example.classpass.data.entity.ChatSession
import com.example.classpass.data.repository.ChatMessageRepository
import com.example.classpass.data.repository.ChatSessionRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for managing chat history and sessions.
 * Handles creating, switching, renaming, and deleting chat sessions.
 */
class ChatHistoryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val sessionRepository: ChatSessionRepository
    private val messageRepository: ChatMessageRepository
    
    init {
        val app = application as ClassPassApplication
        sessionRepository = app.chatSessionRepository
        messageRepository = app.chatRepository
    }
    
    // Cache for message previews
    private val _messagePreviews = MutableLiveData<Map<String, String>>(emptyMap())
    val messagePreviews: LiveData<Map<String, String>> = _messagePreviews
    
    /**
     * Observe all sessions ordered by last updated.
     */
    val allSessions: LiveData<List<ChatSession>> = sessionRepository.allSessions
    
    /**
     * Observe recent sessions (for navigation drawer).
     */
    val recentSessions: LiveData<List<ChatSession>> = sessionRepository.getRecentSessions(10)
    
    /**
     * Observe the currently active session.
     */
    val activeSession: LiveData<ChatSession?> = sessionRepository.activeSession
    
    /**
     * Create a new chat session and set it as active.
     */
    fun createNewChat() {
        viewModelScope.launch {
            sessionRepository.createNewSession()
        }
    }
    
    /**
     * Switch to an existing chat session.
     */
    fun switchToChat(sessionId: String) {
        viewModelScope.launch {
            sessionRepository.setActiveSession(sessionId)
        }
    }
    
    /**
     * Rename a chat session.
     */
    fun renameChat(sessionId: String, newTitle: String) {
        viewModelScope.launch {
            sessionRepository.updateSessionTitle(sessionId, newTitle)
        }
    }
    
    /**
     * Toggle starred status for a chat session.
     */
    fun toggleStarChat(sessionId: String, isStarred: Boolean) {
        viewModelScope.launch {
            sessionRepository.toggleStarred(sessionId, isStarred)
        }
    }
    
    /**
     * Observe starred sessions.
     */
    val starredSessions: LiveData<List<ChatSession>> = sessionRepository.starredSessions
    
    /**
     * Delete a chat session (cascades to messages).
     * Auto-creates a new session if this was the last one.
     */
    fun deleteChat(session: ChatSession) {
        viewModelScope.launch {
            sessionRepository.deleteSession(session)
            
            // Check if there are any sessions left
            if (!sessionRepository.hasSessions()) {
                // No sessions left, create a new one automatically
                sessionRepository.createNewSession()
            }
        }
    }
    
    /**
     * Check if there are any sessions.
     */
    suspend fun hasSessions(): Boolean {
        return sessionRepository.hasSessions()
    }
    
    /**
     * Load message previews for a list of sessions.
     */
    fun loadMessagePreviews(sessions: List<ChatSession>) {
        viewModelScope.launch {
            val previews = mutableMapOf<String, String>()
            sessions.forEach { session ->
                val lastMessage = messageRepository.getLastMessageForSession(session.sessionId)
                lastMessage?.let {
                    // Truncate to 50 characters for preview
                    val preview = if (it.content.length > 50) {
                        "${it.content.take(50)}..."
                    } else {
                        it.content
                    }
                    previews[session.sessionId] = preview
                }
            }
            _messagePreviews.value = previews
        }
    }
    
    /**
     * Get message preview for a specific session.
     */
    fun getMessagePreview(sessionId: String): String? {
        return _messagePreviews.value?.get(sessionId)
    }
}

