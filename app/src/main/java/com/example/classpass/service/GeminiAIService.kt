package com.example.classpass.service

import com.example.classpass.BuildConfig
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Simple Gemini AI service for conversational fitness coaching.
 * Handles all AI interactions for the fitness app.
 */
class GeminiAIService {
    
    // API key loaded from BuildConfig (stored in local.properties)
    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    private val model = GenerativeModel(
        modelName = "gemini-2.0-flash-exp",
        apiKey = apiKey
    )
    
    private val chat = model.startChat(
        history = listOf(
            content("user") { text(AIPrompts.SYSTEM_PROMPT) },
            content("model") { text(AIPrompts.GREETING) }
        )
    )
    
    /**
     * Send a message to the AI and get a response.
     */
    suspend fun sendMessage(userMessage: String): String {
        return try {
            val response = chat.sendMessage(userMessage)
            response.text ?: "Sorry, I couldn't generate a response. Please try again."
        } catch (e: Exception) {
            "Error: ${e.message ?: "Something went wrong. Please check your connection."}"
        }
    }
    
    /**
     * Send a message and stream the response (for typing effect).
     */
    fun sendMessageStreaming(userMessage: String): Flow<String> = flow {
        try {
            chat.sendMessageStream(userMessage).collect { chunk ->
                chunk.text?.let { emit(it) }
            }
        } catch (e: Exception) {
            emit("Error: ${e.message ?: "Something went wrong."}")
        }
    }
    
    /**
     * Generate a chat title from the first user message.
     * Returns a concise title (max 50 characters).
     */
    suspend fun generateChatTitle(firstMessage: String): String {
        return try {
            val prompt = AIPrompts.TITLE_GENERATION_PROMPT + firstMessage
            val response = model.generateContent(prompt)
            val title = response.text?.trim() ?: "New Chat"
            
            // Ensure title is not too long
            if (title.length > 50) {
                title.substring(0, 47) + "..."
            } else {
                title
            }
        } catch (e: Exception) {
            // Fallback to a simple title based on first words
            val words = firstMessage.trim().split(" ").take(4)
            words.joinToString(" ").take(50)
        }
    }
    
    /**
     * Clear chat history and start fresh.
     */
    fun clearHistory() {
        // Note: Gemini SDK doesn't have a built-in clear method
        // We'd need to create a new chat instance
        // For now, this is a placeholder
    }
}

