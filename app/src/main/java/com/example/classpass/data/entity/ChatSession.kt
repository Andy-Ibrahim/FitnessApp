package com.example.classpass.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Represents a chat conversation session.
 * Each session contains multiple messages and has a unique identifier.
 */
@Entity(
    tableName = "chat_sessions",
    indices = [
        Index(value = ["lastUpdated"]),
        Index(value = ["isStarred", "lastUpdated"]) // For efficient starred + date sorting
    ]
)
data class ChatSession(
    @PrimaryKey
    val sessionId: String = UUID.randomUUID().toString(),
    val title: String = "New Chat",
    val createdAt: Long = System.currentTimeMillis(),
    val lastUpdated: Long = System.currentTimeMillis(),
    val messageCount: Int = 0,
    val isActive: Boolean = false, // Only one active session at a time
    val isStarred: Boolean = false // Starred chats appear at the top (pinned)
)

