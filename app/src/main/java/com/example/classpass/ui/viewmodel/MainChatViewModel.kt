package com.example.classpass.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.switchMap
import androidx.lifecycle.viewModelScope
import com.example.classpass.ClassPassApplication
import com.example.classpass.data.entity.ChatMessage
import com.example.classpass.data.repository.ChatMessageRepository
import com.example.classpass.data.repository.ChatSessionRepository
import com.example.classpass.service.GeminiAIService
import kotlinx.coroutines.launch

/**
 * ViewModel for MainChatScreen.
 * Manages chat messages and AI interactions with session support.
 */
class MainChatViewModel(application: Application) : AndroidViewModel(application) {
    
    private val chatRepository: ChatMessageRepository
    private val sessionRepository: ChatSessionRepository
    private val aiService = GeminiAIService()
    
    init {
        val app = application as ClassPassApplication
        chatRepository = app.chatRepository
        sessionRepository = app.chatSessionRepository
    }
    
    // Current active session ID
    private val _currentSessionId = MutableLiveData<String?>()
    val currentSessionId: LiveData<String?> = _currentSessionId
    
    // Messages for current session (switches when session changes)
    val currentMessages: LiveData<List<ChatMessage>> = _currentSessionId.switchMap { sessionId ->
        if (sessionId != null) {
            chatRepository.getMessagesForSession(sessionId)
        } else {
            MutableLiveData(emptyList())
        }
    }
    
    // All messages (for backward compatibility)
    val allMessages: LiveData<List<ChatMessage>> = chatRepository.getAllMessages()
    
    // Loading state
    private val _isProcessing = MutableLiveData(false)
    val isProcessing: LiveData<Boolean> = _isProcessing
    
    // Error state
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * Initialize or create a session.
     * Called when MainChatScreen is created.
     */
    fun initializeSession() {
        viewModelScope.launch {
            try {
                // Check if there's an active session
                val activeSession = sessionRepository.getActiveSessionSync()
                
                if (activeSession != null) {
                    // Use existing active session
                    _currentSessionId.value = activeSession.sessionId
                } else {
                    // No active session, create a new one
                    val newSession = sessionRepository.createNewSession()
                    _currentSessionId.value = newSession.sessionId
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize chat session"
            }
        }
    }
    
    /**
     * Send a user message and get AI response.
     */
    fun sendMessage(userMessage: String) {
        val sessionId = _currentSessionId.value
        if (userMessage.isBlank() || _isProcessing.value == true || sessionId == null) return
        
        viewModelScope.launch {
            try {
                _isProcessing.value = true
                _errorMessage.value = null
                
                // Save user message to database
                val userChatMessage = ChatMessage(
                    sessionId = sessionId,
                    role = "user",
                    content = userMessage.trim()
                )
                chatRepository.insertMessage(userChatMessage)
                
                // Update session timestamp and message count
                sessionRepository.updateSessionAfterMessage(sessionId)
                
                // Check if this is the first message (generate title)
                val messageCount = chatRepository.getMessageCountForSession(sessionId)
                if (messageCount == 1) {
                    generateChatTitle(sessionId, userMessage)
                }
                
                // Get AI response
                val aiResponse = aiService.sendMessage(userMessage)
                
                // Save AI response to database
                val aiChatMessage = ChatMessage(
                    sessionId = sessionId,
                    role = "assistant",
                    content = aiResponse
                )
                chatRepository.insertMessage(aiChatMessage)
                
                // Update session again after AI response
                sessionRepository.updateSessionAfterMessage(sessionId)
                
            } catch (e: Exception) {
                _errorMessage.value = e.message ?: "Failed to send message"
            } finally {
                _isProcessing.value = false
            }
        }
    }
    
    /**
     * Generate chat title from first user message.
     */
    private suspend fun generateChatTitle(sessionId: String, firstMessage: String) {
        try {
            val title = aiService.generateChatTitle(firstMessage)
            sessionRepository.updateSessionTitle(sessionId, title)
        } catch (e: Exception) {
            // Keep default "New Chat" title if generation fails
        }
    }
    
    /**
     * Switch to a different session.
     */
    fun switchSession(sessionId: String) {
        _currentSessionId.value = sessionId
    }
    
    /**
     * Clear all messages in current session.
     */
    fun clearCurrentSession() {
        val sessionId = _currentSessionId.value ?: return
        viewModelScope.launch {
            try {
                chatRepository.deleteMessagesForSession(sessionId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to clear chat"
            }
        }
    }
    
    /**
     * Clear error message.
     */
    fun clearError() {
        _errorMessage.value = null
    }
}
