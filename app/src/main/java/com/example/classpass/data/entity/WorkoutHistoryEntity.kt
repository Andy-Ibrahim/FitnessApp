package com.example.classpass.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Records completed workout sessions for history/analytics
 */
@Entity(
    tableName = "workout_history",
    indices = [
        Index(value = ["programId"]),
        Index(value = ["completedDate"])
    ]
)
data class WorkoutHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val programId: String, // Reference to WorkoutProgramEntity
    val sessionId: String, // Reference to specific session
    val sessionName: String, // "Push Day", etc.
    val completedDate: Long,
    val duration: Int, // Actual duration in minutes
    val exercisesJson: String, // JSON serialized List<Exercise> with actual performance
    val notes: String? = null
)

