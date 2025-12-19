package com.example.classpass.data.dao

import androidx.room.*
import com.example.classpass.data.entity.RestDayLogEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for rest day logs
 */
@Dao
interface RestDayLogDao {
    
    /**
     * Insert a new rest day log
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: RestDayLogEntity): Long
    
    /**
     * Get rest day log for a specific day
     */
    @Query("SELECT * FROM rest_day_logs WHERE programId = :programId AND weekNumber = :weekNumber AND dayNumber = :dayNumber ORDER BY logDate DESC LIMIT 1")
    fun getLogForDay(programId: String, weekNumber: Int, dayNumber: Int): Flow<RestDayLogEntity?>
    
    /**
     * Get all rest day logs for a program
     */
    @Query("SELECT * FROM rest_day_logs WHERE programId = :programId ORDER BY logDate DESC")
    fun getLogsForProgram(programId: String): Flow<List<RestDayLogEntity>>
    
    /**
     * Delete rest day log
     */
    @Delete
    suspend fun deleteLog(log: RestDayLogEntity)
    
    /**
     * Delete all logs for a program (cascade delete)
     */
    @Query("DELETE FROM rest_day_logs WHERE programId = :programId")
    suspend fun deleteLogsForProgram(programId: String)
}

