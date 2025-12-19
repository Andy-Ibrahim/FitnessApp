package com.example.classpass.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tracks a user's progress through a workout program.
 * 
 * This entity:
 * - References a WorkoutTemplateEntity (the weekly pattern)
 * - Tracks which week and day the user is on
 * - Records which days have been completed
 * - Calculates completion percentage
 * 
 * Example: User is on Week 3, Day 2 of a 12-week program
 */
@Entity(
    tableName = "program_schedules",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["userId"]),
        Index(value = ["programId"]),
        Index(value = ["templateId"]),
        Index(value = ["status"])
    ]
)
data class ProgramScheduleEntity(
    @PrimaryKey
    val id: String,
    
    val userId: Long, // Reference to User
    
    val programId: String, // Unique identifier for this program instance
    
    val templateId: String, // Reference to WorkoutTemplateEntity
    
    val programTitle: String, // "12-Week Strength Program"
    
    val programDescription: String,
    
    val programIcon: String = "💪",
    
    val startDate: Long? = null, // Timestamp when user started (null until started)
    
    val durationWeeks: Int, // 12, 16, etc.
    
    val currentWeek: Int = 1, // Which week user is on (1-12)
    
    val currentDay: Int = 1, // Which day of the week (1-7)
    
    val completedDaysJson: String = "[]", // JSON array: ["1-1", "1-2", "1-4", "2-1", ...]
    
    val status: String = "NOT_STARTED", // NOT_STARTED, ACTIVE, IN_PROGRESS, PAUSED, COMPLETED
    
    val completionPercentage: Float = 0f, // 0.0 - 1.0
    
    val createdDate: Long = System.currentTimeMillis(),
    
    val lastModifiedDate: Long = System.currentTimeMillis()
)

