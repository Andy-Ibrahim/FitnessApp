package com.example.classpass.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity for storing rest day logs (feelings, activities, notes)
 */
@Entity(tableName = "rest_day_logs")
data class RestDayLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    val userId: Long,
    val programId: String,
    val weekNumber: Int,
    val dayNumber: Int,
    
    // User inputs
    val feeling: String = "",  // "Energized", "Good", "Normal", "Tired", "Sore"
    val activitiesJson: String = "[]",  // JSON array of selected activities
    val note: String = "",
    
    // Metadata
    val logDate: Long = System.currentTimeMillis()
)

