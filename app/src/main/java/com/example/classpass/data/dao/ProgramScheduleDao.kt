package com.example.classpass.data.dao

import androidx.room.*
import com.example.classpass.data.entity.ProgramScheduleEntity
import com.example.classpass.data.entity.WorkoutTemplateEntity
import com.example.classpass.data.entity.WorkoutDayEntity
import kotlinx.coroutines.flow.Flow

/**
 * Result class for joined query
 */
data class ProgramWithTemplateAndDays(
    @Embedded val schedule: ProgramScheduleEntity,
    @Relation(
        entity = WorkoutTemplateEntity::class,
        parentColumn = "templateId",
        entityColumn = "id"
    )
    val template: WorkoutTemplateEntity?,
    @Relation(
        entity = WorkoutDayEntity::class,
        parentColumn = "templateId",
        entityColumn = "templateId"
    )
    val days: List<WorkoutDayEntity>
)

/**
 * DAO for ProgramScheduleEntity.
 * Handles database operations for user's workout program schedules and progress.
 */
@Dao
interface ProgramScheduleDao {
    
    /**
     * Get all schedules for a user
     */
    @Query("SELECT * FROM program_schedules WHERE userId = :userId ORDER BY lastModifiedDate DESC")
    fun getSchedulesByUserId(userId: Long): Flow<List<ProgramScheduleEntity>>
    
    /**
     * Get all schedules with templates and days (optimized, no N+1)
     */
    @Transaction
    @Query("SELECT * FROM program_schedules WHERE userId = :userId ORDER BY lastModifiedDate DESC")
    fun getSchedulesWithDetailsForUser(userId: Long): Flow<List<ProgramWithTemplateAndDays>>
    
    /**
     * Get active schedule for a user
     */
    @Query("SELECT * FROM program_schedules WHERE userId = :userId AND status IN ('ACTIVE', 'IN_PROGRESS') ORDER BY lastModifiedDate DESC LIMIT 1")
    fun getActiveSchedule(userId: Long): Flow<ProgramScheduleEntity?>
    
    /**
     * Get a schedule by program ID (programId column)
     */
    @Query("SELECT * FROM program_schedules WHERE programId = :programId LIMIT 1")
    fun getScheduleByProgramId(programId: String): Flow<ProgramScheduleEntity?>
    
    /**
     * Get a schedule by its ID
     */
    @Query("SELECT * FROM program_schedules WHERE id = :scheduleId LIMIT 1")
    fun getScheduleById(scheduleId: String): Flow<ProgramScheduleEntity?>
    
    /**
     * Insert a schedule
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSchedule(schedule: ProgramScheduleEntity)
    
    /**
     * Update a schedule
     */
    @Update
    suspend fun updateSchedule(schedule: ProgramScheduleEntity)
    
    /**
     * Update progress (current week, day, completion percentage)
     */
    @Query("""
        UPDATE program_schedules 
        SET currentWeek = :week, 
            currentDay = :day, 
            completionPercentage = :percentage,
            lastModifiedDate = :timestamp
        WHERE id = :scheduleId
    """)
    suspend fun updateProgress(
        scheduleId: String, 
        week: Int, 
        day: Int, 
        percentage: Float,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Update completed days JSON
     */
    @Query("""
        UPDATE program_schedules 
        SET completedDaysJson = :completedDaysJson,
            completionPercentage = :percentage,
            lastModifiedDate = :timestamp
        WHERE id = :scheduleId
    """)
    suspend fun updateCompletedDays(
        scheduleId: String, 
        completedDaysJson: String,
        percentage: Float,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Update status
     */
    @Query("""
        UPDATE program_schedules 
        SET status = :status,
            lastModifiedDate = :timestamp
        WHERE id = :scheduleId
    """)
    suspend fun updateStatus(
        scheduleId: String, 
        status: String,
        timestamp: Long = System.currentTimeMillis()
    )
    
    /**
     * Delete a schedule
     */
    @Delete
    suspend fun deleteSchedule(schedule: ProgramScheduleEntity)
    
    /**
     * Delete a schedule by ID
     */
    @Query("DELETE FROM program_schedules WHERE id = :scheduleId")
    suspend fun deleteScheduleById(scheduleId: String)
    
    /**
     * Delete all completed schedules for a user
     */
    @Query("DELETE FROM program_schedules WHERE userId = :userId AND status = 'COMPLETED'")
    suspend fun deleteCompletedSchedules(userId: Long)
}

