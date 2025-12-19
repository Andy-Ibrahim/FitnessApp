package com.example.classpass.domain.mapper

import com.example.classpass.data.entity.ProgramScheduleEntity
import com.example.classpass.data.entity.WorkoutDayEntity
import com.example.classpass.data.entity.WorkoutTemplateEntity
import com.example.classpass.domain.model.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.encodeToString
import kotlinx.serialization.decodeFromString

/**
 * Mapper object for converting between database entities and DTOs.
 * 
 * This keeps the UI layer clean and independent of database structure.
 */
object WorkoutMapper {
    
    private val json = Json { ignoreUnknownKeys = true }
    
    // ========== ProgramSchedule -> WorkoutProgramDto ==========
    
    /**
     * Convert ProgramScheduleEntity + WorkoutTemplateEntity + List<WorkoutDayEntity> 
     * to WorkoutProgramDto (for UI)
     */
    fun toWorkoutProgramDto(
        schedule: ProgramScheduleEntity,
        template: WorkoutTemplateEntity,
        days: List<WorkoutDayEntity>
    ): WorkoutProgramDto {
        return WorkoutProgramDto(
            id = schedule.programId,
            title = schedule.programTitle,
            description = schedule.programDescription,
            icon = schedule.programIcon,
            totalWeeks = schedule.durationWeeks,
            daysPerWeek = template.daysPerWeek,
            status = mapStatusStringToDto(schedule.status),
            currentWeek = schedule.currentWeek,
            currentDay = schedule.currentDay,
            startDate = schedule.startDate,
            completionPercentage = schedule.completionPercentage,
            createdDate = schedule.createdDate
        )
    }
    
    // ========== WorkoutDayEntity -> WorkoutSessionDto ==========
    
    /**
     * Convert WorkoutDayEntity to WorkoutSessionDto
     * 
     * @param dayEntity The database entity
     * @param weekNumber The current week (for calculating completion)
     * @param completedDays Set of completed day keys (e.g., "1-1", "2-3")
     */
    fun toWorkoutSessionDto(
        dayEntity: WorkoutDayEntity,
        weekNumber: Int,
        completedDays: Set<String>
    ): WorkoutSessionDto {
        val dayKey = "$weekNumber-${dayEntity.dayNumber}"
        val exercises = parseExercisesJson(dayEntity.exercisesJson)
        
        return WorkoutSessionDto(
            id = "${dayEntity.templateId}-${dayEntity.dayNumber}",
            dayNumber = dayEntity.dayNumber,
            name = dayEntity.workoutType,
            exercises = exercises,
            estimatedDuration = dayEntity.estimatedDuration,
            isCompleted = completedDays.contains(dayKey),
            isRestDay = dayEntity.isRestDay,
            completedDate = null, // TODO: Track completion dates if needed
            notes = null
        )
    }
    
    /**
     * Convert list of WorkoutDayEntity to list of WorkoutSessionDto
     */
    fun toWorkoutSessionDtos(
        dayEntities: List<WorkoutDayEntity>,
        weekNumber: Int,
        completedDays: Set<String>
    ): List<WorkoutSessionDto> {
        return dayEntities.map { toWorkoutSessionDto(it, weekNumber, completedDays) }
    }
    
    // ========== JSON Parsing ==========
    
    /**
     * Parse exercises JSON string to list of ExerciseDto
     */
    fun parseExercisesJson(exercisesJson: String): List<ExerciseDto> {
        return try {
            json.decodeFromString<List<ExerciseDto>>(exercisesJson)
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * Convert list of ExerciseDto to JSON string
     */
    fun exercisesToJson(exercises: List<ExerciseDto>): String {
        return json.encodeToString(exercises)
    }
    
    /**
     * Parse completed days JSON to Set
     */
    fun parseCompletedDaysJson(completedDaysJson: String): Set<String> {
        return try {
            json.decodeFromString<List<String>>(completedDaysJson).toSet()
        } catch (e: Exception) {
            emptySet()
        }
    }
    
    /**
     * Convert Set of completed days to JSON string
     */
    fun completedDaysToJson(completedDays: Set<String>): String {
        return json.encodeToString(completedDays.toList())
    }
    
    // ========== Status Mapping ==========
    
    /**
     * Map status string to ProgramStatusDto enum
     */
    private fun mapStatusStringToDto(status: String): ProgramStatusDto {
        return when (status) {
            "NOT_STARTED" -> ProgramStatusDto.NOT_STARTED
            "ACTIVE" -> ProgramStatusDto.ACTIVE
            "IN_PROGRESS" -> ProgramStatusDto.IN_PROGRESS
            "PAUSED" -> ProgramStatusDto.PAUSED
            "COMPLETED" -> ProgramStatusDto.COMPLETED
            else -> ProgramStatusDto.NOT_STARTED
        }
    }
    
    /**
     * Map ProgramStatusDto enum to status string
     */
    fun mapStatusDtoToString(status: ProgramStatusDto): String {
        return status.name
    }
    
    // ========== DTO -> Entity (for creating new programs) ==========
    
    /**
     * Create ProgramScheduleEntity from WorkoutProgramDto
     */
    fun toProgramScheduleEntity(
        dto: WorkoutProgramDto,
        userId: Long,
        templateId: String
    ): ProgramScheduleEntity {
        return ProgramScheduleEntity(
            id = dto.id,
            userId = userId,
            programId = dto.id,
            templateId = templateId,
            programTitle = dto.title,
            programDescription = dto.description,
            programIcon = dto.icon,
            startDate = dto.startDate ?: System.currentTimeMillis(),
            durationWeeks = dto.totalWeeks,
            currentWeek = dto.currentWeek,
            currentDay = dto.currentDay,
            completedDaysJson = "[]",
            status = mapStatusDtoToString(dto.status),
            completionPercentage = dto.completionPercentage,
            createdDate = dto.createdDate,
            lastModifiedDate = System.currentTimeMillis()
        )
    }
    
    /**
     * Create WorkoutTemplateEntity from basic info
     */
    fun createWorkoutTemplateEntity(
        id: String,
        programId: String,
        name: String,
        daysPerWeek: Int,
        description: String
    ): WorkoutTemplateEntity {
        return WorkoutTemplateEntity(
            id = id,
            programId = programId,
            name = name,
            daysPerWeek = daysPerWeek,
            description = description,
            createdDate = System.currentTimeMillis()
        )
    }
    
    /**
     * Create WorkoutDayEntity from ExerciseDto list
     */
    fun createWorkoutDayEntity(
        id: String,
        templateId: String,
        dayNumber: Int,
        workoutType: String,
        exercises: List<ExerciseDto>,
        isRestDay: Boolean,
        estimatedDuration: Int
    ): WorkoutDayEntity {
        return WorkoutDayEntity(
            id = id,
            templateId = templateId,
            dayNumber = dayNumber,
            workoutType = workoutType,
            exercisesJson = exercisesToJson(exercises),
            isRestDay = isRestDay,
            estimatedDuration = estimatedDuration
        )
    }
}

