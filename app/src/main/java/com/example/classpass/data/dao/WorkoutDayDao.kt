package com.example.classpass.data.dao

import androidx.room.*
import com.example.classpass.data.entity.WorkoutDayEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for WorkoutDayEntity.
 * Handles database operations for workout days within a template.
 */
@Dao
interface WorkoutDayDao {
    
    /**
     * Get all days for a specific template, ordered by day number
     */
    @Query("SELECT * FROM workout_days WHERE templateId = :templateId ORDER BY dayNumber ASC")
    fun getDaysByTemplateId(templateId: String): Flow<List<WorkoutDayEntity>>
    
    /**
     * Get a specific day from a template
     */
    @Query("SELECT * FROM workout_days WHERE templateId = :templateId AND dayNumber = :dayNumber LIMIT 1")
    fun getDay(templateId: String, dayNumber: Int): Flow<WorkoutDayEntity?>
    
    /**
     * Get a day by its ID
     */
    @Query("SELECT * FROM workout_days WHERE id = :dayId LIMIT 1")
    fun getDayById(dayId: String): Flow<WorkoutDayEntity?>
    
    /**
     * Insert a single day
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDay(day: WorkoutDayEntity)
    
    /**
     * Insert multiple days (for creating a full week template)
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDays(days: List<WorkoutDayEntity>)
    
    /**
     * Update a day
     */
    @Update
    suspend fun updateDay(day: WorkoutDayEntity)
    
    /**
     * Delete a day
     */
    @Delete
    suspend fun deleteDay(day: WorkoutDayEntity)
    
    /**
     * Delete all days for a template
     */
    @Query("DELETE FROM workout_days WHERE templateId = :templateId")
    suspend fun deleteDaysByTemplateId(templateId: String)
}

