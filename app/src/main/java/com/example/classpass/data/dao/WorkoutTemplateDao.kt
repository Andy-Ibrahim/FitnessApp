package com.example.classpass.data.dao

import androidx.room.*
import com.example.classpass.data.entity.WorkoutTemplateEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO for WorkoutTemplateEntity.
 * Handles database operations for workout templates.
 */
@Dao
interface WorkoutTemplateDao {
    
    /**
     * Get a template by program ID
     */
    @Query("SELECT * FROM workout_templates WHERE programId = :programId LIMIT 1")
    fun getTemplateByProgramId(programId: String): Flow<WorkoutTemplateEntity?>
    
    /**
     * Get a template by its ID
     */
    @Query("SELECT * FROM workout_templates WHERE id = :templateId LIMIT 1")
    fun getTemplateById(templateId: String): Flow<WorkoutTemplateEntity?>
    
    /**
     * Get all templates
     */
    @Query("SELECT * FROM workout_templates ORDER BY createdDate DESC")
    fun getAllTemplates(): Flow<List<WorkoutTemplateEntity>>
    
    /**
     * Insert a template
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: WorkoutTemplateEntity)
    
    /**
     * Update a template
     */
    @Update
    suspend fun updateTemplate(template: WorkoutTemplateEntity)
    
    /**
     * Delete a template
     */
    @Delete
    suspend fun deleteTemplate(template: WorkoutTemplateEntity)
    
    /**
     * Delete a template by ID
     */
    @Query("DELETE FROM workout_templates WHERE id = :templateId")
    suspend fun deleteTemplateById(templateId: String)
}

