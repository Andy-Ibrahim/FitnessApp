package com.example.classpass.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a chat message in a conversation session.
 * Messages are grouped by sessionId and linked to ChatSession via foreign key.
 */
@Entity(
    tableName = "chat_messages",
    indices = [
        Index(value = ["timestamp"]),
        Index(value = ["sessionId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = ChatSession::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE // Delete messages when session is deleted
        )
    ]
)
data class ChatMessage(
    @PrimaryKey(autoGenerate = true)
    val messageId: Long = 0,
    val sessionId: String, // Links to ChatSession
    val role: String, // "user" or "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)
