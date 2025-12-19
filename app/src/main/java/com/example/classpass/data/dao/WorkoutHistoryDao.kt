package com.example.classpass.data.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.example.classpass.data.entity.WorkoutHistoryEntity

@Dao
interface WorkoutHistoryDao {
    
    /**
     * Get workout history for a specific program
     */
    @Query("SELECT * FROM workout_history WHERE programId = :programId ORDER BY completedDate DESC")
    fun getHistoryForProgram(programId: String): LiveData<List<WorkoutHistoryEntity>>
    
    /**
     * Get recent workout history (across all programs)
     */
    @Query("SELECT * FROM workout_history ORDER BY completedDate DESC LIMIT :limit")
    fun getRecentHistory(limit: Int): LiveData<List<WorkoutHistoryEntity>>
    
    /**
     * Insert completed workout to history
     */
    @Insert
    suspend fun insertHistory(history: WorkoutHistoryEntity)
    
    /**
     * Delete history entry
     */
    @Delete
    suspend fun deleteHistory(history: WorkoutHistoryEntity)
    
    /**
     * Get count of completed sessions for a program
     */
    @Query("SELECT COUNT(*) FROM workout_history WHERE programId = :programId")
    fun getCompletedSessionCount(programId: String): LiveData<Int>
    
    /**
     * Get total workouts completed (all programs)
     */
    @Query("SELECT COUNT(*) FROM workout_history")
    fun getTotalWorkoutsCompleted(): LiveData<Int>
    
    /**
     * Get specific workout history by ID
     */
    @Query("SELECT * FROM workout_history WHERE id = :historyId")
    suspend fun getHistoryById(historyId: Long): WorkoutHistoryEntity?
    
    /**
     * Get history for a specific workout session (program + week + day)
     */
    @Query("SELECT * FROM workout_history WHERE programId = :programId AND sessionId = :sessionId ORDER BY completedDate DESC")
    suspend fun getHistoryForSession(programId: String, sessionId: String): List<WorkoutHistoryEntity>
}

