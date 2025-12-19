package com.example.classpass.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a single day in the weekly template (1-7).
 * 
 * Example: Day 1 = "Upper Body Push"
 * - Contains the workout type and exercises for that day
 * - 7 of these make up a complete weekly template
 * - This template repeats for all weeks in the program
 */
@Entity(
    tableName = "workout_days",
    foreignKeys = [
        ForeignKey(
            entity = WorkoutTemplateEntity::class,
            parentColumns = ["id"],
            childColumns = ["templateId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["templateId"]),
        Index(value = ["dayNumber"])
    ]
)
data class WorkoutDayEntity(
    @PrimaryKey
    val id: String,
    
    val templateId: String, // Reference to WorkoutTemplateEntity
    
    val dayNumber: Int, // 1-7 (Monday-Sunday)
    
    val workoutType: String, // "Upper Body Push", "Lower Body", "Rest"
    
    val exercisesJson: String, // JSON array of exercises
    
    val isRestDay: Boolean = false,
    
    val estimatedDuration: Int // Minutes
)

