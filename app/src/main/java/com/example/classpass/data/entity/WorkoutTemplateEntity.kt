package com.example.classpass.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a weekly workout template.
 * This is the pattern that repeats across all weeks.
 * 
 * Example: "4-Day Upper/Lower Split"
 * - Stores the template name and metadata
 * - Associated with 7 WorkoutDayEntity objects (one for each day of the week)
 */
@Entity(
    tableName = "workout_templates",
    indices = [Index(value = ["programId"])]
)
data class WorkoutTemplateEntity(
    @PrimaryKey
    val id: String,
    
    val programId: String, // Reference to the program this template belongs to
    
    val name: String, // e.g., "4-Day Upper/Lower Split"
    
    val daysPerWeek: Int, // 3, 4, 5, 6, or 7
    
    val description: String,
    
    val createdDate: Long = System.currentTimeMillis()
)

