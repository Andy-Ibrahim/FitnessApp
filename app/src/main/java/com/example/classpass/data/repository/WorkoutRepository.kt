package com.example.classpass.data.repository

import com.example.classpass.data.dao.ProgramScheduleDao
import com.example.classpass.data.dao.WorkoutDayDao
import com.example.classpass.data.dao.WorkoutHistoryDao
import com.example.classpass.data.dao.WorkoutTemplateDao
import com.example.classpass.data.entity.ProgramScheduleEntity
import com.example.classpass.data.entity.WorkoutDayEntity
import com.example.classpass.data.entity.WorkoutTemplateEntity
import com.example.classpass.domain.mapper.WorkoutMapper
import com.example.classpass.domain.model.ExerciseDto
import com.example.classpass.domain.model.WorkoutProgramDto
import com.example.classpass.domain.model.WorkoutSessionDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import java.util.UUID

/**
 * Repository for workout programs using the new template system.
 * 
 * This repository:
 * - Abstracts database operations
 * - Converts entities to DTOs (clean models for UI)
 * - Handles business logic (completion tracking, progress calculation)
 * - Provides a clean API for ViewModels
 */
class WorkoutRepository(
    private val templateDao: WorkoutTemplateDao,
    private val dayDao: WorkoutDayDao,
    private val scheduleDao: ProgramScheduleDao,
    private val historyDao: WorkoutHistoryDao,
    private val restDayLogDao: com.example.classpass.data.dao.RestDayLogDao
) {
    
    // ========== Get Programs ==========
    
    /**
     * Get all programs for a user as DTOs
     * 
     * Note: This returns ProgramScheduleEntity for now.
     * To convert to WorkoutProgramDto, you need to fetch template and days for each schedule.
     * For performance, use this method for listing and getActiveProgram() for details.
     */
    fun getUserPrograms(userId: Long): Flow<List<ProgramScheduleEntity>> {
        return scheduleDao.getSchedulesByUserId(userId)
    }
    
    /**
     * Get all programs for a user as full DTOs (with template data)
     * OPTIMIZED: Uses single JOIN query instead of N+1 queries
     */
    suspend fun getUserProgramsDetailed(userId: Long): List<WorkoutProgramDto> {
        val programsWithDetails = scheduleDao.getSchedulesWithDetailsForUser(userId).firstOrNull() ?: return emptyList()
        
        return programsWithDetails.mapNotNull { programData ->
            if (programData.template != null && programData.days.isNotEmpty()) {
                WorkoutMapper.toWorkoutProgramDto(programData.schedule, programData.template, programData.days)
            } else {
                null
            }
        }
    }
    
    /**
     * Get active program for a user
     */
    suspend fun getActiveProgram(userId: Long): WorkoutProgramDto? {
        val schedule = scheduleDao.getActiveSchedule(userId).firstOrNull() ?: return null
        val template = templateDao.getTemplateById(schedule.templateId).firstOrNull() ?: return null
        val days = dayDao.getDaysByTemplateId(template.id).firstOrNull() ?: emptyList()
        
        return WorkoutMapper.toWorkoutProgramDto(schedule, template, days)
    }
    
    /**
     * Get a specific program by ID
     */
    suspend fun getProgramById(programId: String): WorkoutProgramDto? {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull() ?: return null
        val template = templateDao.getTemplateById(schedule.templateId).firstOrNull() ?: return null
        val days = dayDao.getDaysByTemplateId(template.id).firstOrNull() ?: emptyList()
        
        return WorkoutMapper.toWorkoutProgramDto(schedule, template, days)
    }
    
    // ========== Get Workout Sessions ==========
    
    /**
     * Get all workout sessions for a specific week
     */
    suspend fun getWeekWorkouts(programId: String, weekNumber: Int): List<WorkoutSessionDto> {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull() ?: return emptyList()
        val days = dayDao.getDaysByTemplateId(schedule.templateId).firstOrNull() ?: return emptyList()
        val completedDays = WorkoutMapper.parseCompletedDaysJson(schedule.completedDaysJson)
        
        return WorkoutMapper.toWorkoutSessionDtos(days, weekNumber, completedDays)
    }
    
    /**
     * Get a specific workout session
     */
    suspend fun getWorkoutSession(
        programId: String,
        weekNumber: Int,
        dayNumber: Int
    ): WorkoutSessionDto? {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull() ?: return null
        val days = dayDao.getDaysByTemplateId(schedule.templateId).firstOrNull() ?: return null
        val day = days.find { it.dayNumber == dayNumber } ?: return null
        val completedDays = WorkoutMapper.parseCompletedDaysJson(schedule.completedDaysJson)
        
        return WorkoutMapper.toWorkoutSessionDto(day, weekNumber, completedDays)
    }
    
    // ========== Create Program ==========
    
    /**
     * Create a new workout program with template and schedule
     * 
     * @param userId User ID
     * @param title Program title (e.g., "12-Week Strength Program")
     * @param description Program description
     * @param icon Program icon
     * @param durationWeeks Total weeks (e.g., 12)
     * @param daysPerWeek Training days per week (e.g., 4)
     * @param weeklyWorkouts List of 7 workout sessions (one for each day)
     * @return Created program ID
     */
    suspend fun createProgram(
        userId: Long,
        title: String,
        description: String,
        icon: String,
        durationWeeks: Int,
        daysPerWeek: Int,
        weeklyWorkouts: List<Pair<String, List<ExerciseDto>>> // (workoutType, exercises)
    ): String {
        val programId = UUID.randomUUID().toString()
        val templateId = UUID.randomUUID().toString()
        
        // 1. Create template
        val template = WorkoutMapper.createWorkoutTemplateEntity(
            id = templateId,
            programId = programId,
            name = "$daysPerWeek-Day Split",
            daysPerWeek = daysPerWeek,
            description = description
        )
        templateDao.insertTemplate(template)
        
        // 2. Create 7 days (some may be rest days)
        val dayEntities = weeklyWorkouts.mapIndexed { index, (workoutType, exercises) ->
            val dayNumber = index + 1
            val isRestDay = workoutType.equals("Rest", ignoreCase = true) || exercises.isEmpty()
            val estimatedDuration = if (isRestDay) 0 else exercises.size * 10 // ~10 min per exercise
            
            WorkoutMapper.createWorkoutDayEntity(
                id = UUID.randomUUID().toString(),
                templateId = templateId,
                dayNumber = dayNumber,
                workoutType = workoutType,
                exercises = exercises,
                isRestDay = isRestDay,
                estimatedDuration = estimatedDuration
            )
        }
        dayDao.insertDays(dayEntities)
        
        // 3. Create schedule (NOT_STARTED by default)
        val schedule = ProgramScheduleEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            programId = programId,
            templateId = templateId,
            programTitle = title,
            programDescription = description,
            programIcon = icon,
            startDate = System.currentTimeMillis(), // Set start date immediately
            durationWeeks = durationWeeks,
            currentWeek = 1,
            currentDay = 1,
            completedDaysJson = "[]",
            status = "ACTIVE", // Programs are active by default
            completionPercentage = 0f,
            createdDate = System.currentTimeMillis(),
            lastModifiedDate = System.currentTimeMillis()
        )
        scheduleDao.insertSchedule(schedule)
        
        return programId
    }
    
    /**
     * Update an existing program
     */
    suspend fun updateProgram(
        programId: String,
        title: String,
        description: String,
        durationWeeks: Int,
        daysPerWeek: Int,
        weeklyWorkouts: List<Pair<String, List<ExerciseDto>>> // (workoutType, exercises)
    ) {
        // 1. Get existing schedule
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull()
            ?: throw IllegalArgumentException("Program not found: $programId")
        
        val templateId = schedule.templateId
        
        // 2. Update template
        val template = templateDao.getTemplateById(templateId).firstOrNull()
            ?: throw IllegalArgumentException("Template not found: $templateId")
        
        val updatedTemplate = template.copy(
            name = "$daysPerWeek-Day Split",
            daysPerWeek = daysPerWeek,
            description = description
        )
        templateDao.updateTemplate(updatedTemplate)
        
        // 3. Delete old days and insert new ones
        dayDao.deleteDaysByTemplateId(templateId)
        
        val dayEntities = weeklyWorkouts.mapIndexed { index, (workoutType, exercises) ->
            val dayNumber = index + 1
            val isRestDay = workoutType.equals("Rest", ignoreCase = true) || exercises.isEmpty()
            val estimatedDuration = if (isRestDay) 0 else exercises.size * 10 // ~10 min per exercise
            
            WorkoutMapper.createWorkoutDayEntity(
                id = UUID.randomUUID().toString(),
                templateId = templateId,
                dayNumber = dayNumber,
                workoutType = workoutType,
                exercises = exercises,
                isRestDay = isRestDay,
                estimatedDuration = estimatedDuration
            )
        }
        dayDao.insertDays(dayEntities)
        
        // 4. Update schedule
        val updatedSchedule = schedule.copy(
            programTitle = title,
            programDescription = description,
            durationWeeks = durationWeeks,
            lastModifiedDate = System.currentTimeMillis()
        )
        scheduleDao.updateSchedule(updatedSchedule)
    }
    
    /**
     * Update exercises for a specific workout day in the template
     */
    suspend fun updateWorkoutDayExercises(
        programId: String,
        dayNumber: Int,
        exercises: List<ExerciseDto>
    ) {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull()
            ?: throw IllegalArgumentException("Program not found: $programId")
        
        val templateId = schedule.templateId
        val dayId = "${templateId}_day_$dayNumber"
        
        val day = dayDao.getDayById(dayId).firstOrNull()
            ?: throw IllegalArgumentException("Day not found: $dayId")
        
        val updatedDay = day.copy(
            exercisesJson = WorkoutMapper.exercisesToJson(exercises),
            estimatedDuration = exercises.sumOf { (it.sets * it.reps * 3) + it.restSeconds } / 60
        )
        
        dayDao.updateDay(updatedDay)
    }
    
    /**
     * Add an exercise to a specific workout day
     */
    suspend fun addExerciseToDay(
        programId: String,
        dayNumber: Int,
        exercise: ExerciseDto
    ) {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull()
            ?: throw IllegalArgumentException("Program not found: $programId")
        
        val templateId = schedule.templateId
        
        val day = dayDao.getDay(templateId, dayNumber).firstOrNull()
            ?: throw IllegalArgumentException("Day not found: templateId=$templateId, dayNumber=$dayNumber")
        
        val currentExercises = WorkoutMapper.parseExercisesJson(day.exercisesJson).toMutableList()
        currentExercises.add(exercise)
        
        val updatedDay = day.copy(
            exercisesJson = WorkoutMapper.exercisesToJson(currentExercises),
            estimatedDuration = currentExercises.sumOf { (it.sets * it.reps * 3) + it.restSeconds } / 60
        )
        
        dayDao.updateDay(updatedDay)
    }
    
    /**
     * Update a specific exercise in a workout day
     */
    suspend fun updateExerciseInDay(
        programId: String,
        dayNumber: Int,
        exerciseIndex: Int,
        updatedExercise: ExerciseDto
    ) {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull()
            ?: throw IllegalArgumentException("Program not found: $programId")
        
        val templateId = schedule.templateId
        
        val day = dayDao.getDay(templateId, dayNumber).firstOrNull()
            ?: throw IllegalArgumentException("Day not found: templateId=$templateId, dayNumber=$dayNumber")
        
        val exercises = WorkoutMapper.parseExercisesJson(day.exercisesJson).toMutableList()
        if (exerciseIndex !in exercises.indices) {
            throw IndexOutOfBoundsException("Invalid exercise index: $exerciseIndex")
        }
        
        exercises[exerciseIndex] = updatedExercise
        
        val updatedDay = day.copy(
            exercisesJson = WorkoutMapper.exercisesToJson(exercises),
            estimatedDuration = exercises.sumOf { (it.sets * it.reps * 3) + it.restSeconds } / 60
        )
        
        dayDao.updateDay(updatedDay)
    }
    
    /**
     * Delete an exercise from a workout day
     */
    suspend fun deleteExerciseFromDay(
        programId: String,
        dayNumber: Int,
        exerciseIndex: Int
    ) {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull()
            ?: throw IllegalArgumentException("Program not found: $programId")
        
        val templateId = schedule.templateId
        
        val day = dayDao.getDay(templateId, dayNumber).firstOrNull()
            ?: throw IllegalArgumentException("Day not found: templateId=$templateId, dayNumber=$dayNumber")
        
        val exercises = WorkoutMapper.parseExercisesJson(day.exercisesJson).toMutableList()
        if (exerciseIndex !in exercises.indices) {
            throw IndexOutOfBoundsException("Invalid exercise index: $exerciseIndex")
        }
        
        exercises.removeAt(exerciseIndex)
        
        val updatedDay = day.copy(
            exercisesJson = WorkoutMapper.exercisesToJson(exercises),
            estimatedDuration = exercises.sumOf { (it.sets * it.reps * 3) + it.restSeconds } / 60
        )
        
        dayDao.updateDay(updatedDay)
    }
    
    // ========== Update Progress ==========
    
    /**
     * Mark a workout as completed and save to history
     * @return The history ID of the completed workout
     */
    suspend fun completeWorkout(programId: String, weekNumber: Int, dayNumber: Int, durationSeconds: Int): Long {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull() ?: return 0L
        
        // Save workout history first
        val historyId = saveWorkoutHistory(programId, weekNumber, dayNumber, durationSeconds)
        
        // Add to completed days
        val completedDays = WorkoutMapper.parseCompletedDaysJson(schedule.completedDaysJson).toMutableSet()
        val dayKey = "$weekNumber-$dayNumber"
        completedDays.add(dayKey)
        
        // Calculate completion percentage
        val daysInWeek = dayDao.getDaysByTemplateId(schedule.templateId).firstOrNull()?.size ?: 7
        val totalDays = schedule.durationWeeks * daysInWeek.coerceAtMost(7)
        val completionPercentage = if (totalDays > 0) completedDays.size.toFloat() / totalDays else 0f
        
        // Update schedule
        scheduleDao.updateCompletedDays(
            scheduleId = schedule.id,
            completedDaysJson = WorkoutMapper.completedDaysToJson(completedDays),
            percentage = completionPercentage
        )
        
        // Move to next day if needed
        val template = templateDao.getTemplateById(schedule.templateId).firstOrNull()
        if (template != null && dayNumber == template.daysPerWeek && weekNumber < schedule.durationWeeks) {
            // Move to next week
            scheduleDao.updateProgress(
                scheduleId = schedule.id,
                week = weekNumber + 1,
                day = 1,
                percentage = completionPercentage
            )
        } else if (dayNumber < (template?.daysPerWeek ?: 7)) {
            // Move to next day
            scheduleDao.updateProgress(
                scheduleId = schedule.id,
                week = weekNumber,
                day = dayNumber + 1,
                percentage = completionPercentage
            )
        }
        
        return historyId
    }
    
    /**
     * Update current week and day
     */
    suspend fun updateProgress(programId: String, weekNumber: Int, dayNumber: Int) {
        val schedule = scheduleDao.getScheduleByProgramId(programId).firstOrNull() ?: return
        scheduleDao.updateProgress(
            scheduleId = schedule.id,
            week = weekNumber,
            day = dayNumber,
            percentage = schedule.completionPercentage
        )
    }
    
    // ========== Delete Program ==========
    
    /**
     * Delete a program (cascades to template and days)
     */
    suspend fun deleteProgram(programId: String) {
        scheduleDao.getScheduleByProgramId(programId).firstOrNull()?.let { schedule ->
            scheduleDao.deleteSchedule(schedule)
        }
        // Also delete rest day logs
        deleteRestDayLogsForProgram(programId)
    }
    
    /**
     * Rename a program
     */
    suspend fun renameProgram(programId: String, newTitle: String) {
        scheduleDao.getScheduleByProgramId(programId).firstOrNull()?.let { schedule ->
            val updated = schedule.copy(
                programTitle = newTitle,
                lastModifiedDate = System.currentTimeMillis()
            )
            scheduleDao.updateSchedule(updated)
        }
    }
    
    
    // ========== Workout History ==========
    
    /**
     * Save workout history after completion
     */
    suspend fun saveWorkoutHistory(
        programId: String,
        weekNumber: Int,
        dayNumber: Int,
        durationSeconds: Int
    ): Long {
        // Get workout session details
        val session = getWorkoutSession(programId, weekNumber, dayNumber) ?: return 0L
        val sessionId = "$weekNumber-$dayNumber"
        
        // Create history entity
        val history = com.example.classpass.data.entity.WorkoutHistoryEntity(
            programId = programId,
            sessionId = sessionId,
            sessionName = session.name,
            completedDate = System.currentTimeMillis(),
            duration = (durationSeconds / 60), // Convert to minutes
            exercisesJson = WorkoutMapper.exercisesToJson(session.exercises),
            notes = null
        )
        
        historyDao.insertHistory(history)
        
        // Return the ID of the inserted history (Room auto-generates it)
        // We'll need to query it back
        val histories = historyDao.getHistoryForSession(programId, sessionId)
        return histories.firstOrNull()?.id ?: 0L
    }
    
    /**
     * Get workout history by ID
     */
    suspend fun getWorkoutHistoryById(historyId: Long): WorkoutHistoryDto? {
        val entity = historyDao.getHistoryById(historyId) ?: return null
        return WorkoutHistoryDto(
            id = entity.id,
            programId = entity.programId,
            sessionId = entity.sessionId,
            sessionName = entity.sessionName,
            completedDate = entity.completedDate,
            durationMinutes = entity.duration,
            exercises = WorkoutMapper.parseExercisesJson(entity.exercisesJson),
            notes = entity.notes
        )
    }
    
    /**
     * Get all workout history for a program
     */
    suspend fun getWorkoutHistoryForProgram(programId: String): List<WorkoutHistoryDto> {
        // Using Flow.firstOrNull() to get LiveData value
        val entities = historyDao.getHistoryForProgram(programId)
        // For now, we'll create a suspend version
        return emptyList() // TODO: Convert LiveData properly
    }
    
    /**
     * Get history for a specific workout session
     */
    suspend fun getHistoryForSession(programId: String, sessionId: String): List<WorkoutHistoryDto> {
        val entities = historyDao.getHistoryForSession(programId, sessionId)
        return entities.map { entity ->
            WorkoutHistoryDto(
                id = entity.id,
                programId = entity.programId,
                sessionId = entity.sessionId,
                sessionName = entity.sessionName,
                completedDate = entity.completedDate,
                durationMinutes = entity.duration,
                exercises = WorkoutMapper.parseExercisesJson(entity.exercisesJson),
                notes = entity.notes
            )
        }
    }
    
    // ========== Rest Day Logs ==========
    
    /**
     * Save a rest day log
     */
    suspend fun saveRestDayLog(
        userId: Long,
        programId: String,
        weekNumber: Int,
        dayNumber: Int,
        feeling: String,
        activities: List<String>,
        note: String
    ) {
        // Simple JSON array format: ["activity1", "activity2"]
        val activitiesJson = activities.joinToString(prefix = "[\"", separator = "\",\"", postfix = "\"]")
            .replace("[]", "[\"\"]") // Handle empty list
        
        val log = com.example.classpass.data.entity.RestDayLogEntity(
            userId = userId,
            programId = programId,
            weekNumber = weekNumber,
            dayNumber = dayNumber,
            feeling = feeling,
            activitiesJson = activitiesJson,
            note = note
        )
        
        restDayLogDao.insertLog(log)
    }
    
    /**
     * Get rest day log for a specific day
     */
    fun getRestDayLog(
        programId: String,
        weekNumber: Int,
        dayNumber: Int
    ): Flow<com.example.classpass.data.entity.RestDayLogEntity?> {
        return restDayLogDao.getLogForDay(programId, weekNumber, dayNumber)
    }
    
    /**
     * Delete rest day logs when program is deleted
     */
    suspend fun deleteRestDayLogsForProgram(programId: String) {
        restDayLogDao.deleteLogsForProgram(programId)
    }
    
    // ========== Calendar & Scheduling ==========
    
    /**
     * Get scheduled workouts for a date range
     * Calculates actual dates from program start date + week/day
     */
    suspend fun getScheduledWorkoutsForDateRange(
        userId: Long,
        startDate: java.time.LocalDate,
        endDate: java.time.LocalDate
    ): Map<java.time.LocalDate, List<com.example.classpass.domain.model.ScheduledWorkoutDto>> {
        val programs = getUserProgramsDetailed(userId)
        val scheduledWorkouts = mutableMapOf<java.time.LocalDate, MutableList<com.example.classpass.domain.model.ScheduledWorkoutDto>>()
        
        programs.forEach { program ->
            val programStartDate = program.startDate?.let { 
                java.time.Instant.ofEpochMilli(it)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDate()
            } ?: return@forEach // Skip if no start date
            
            // Get schedule to access completed days
            val schedule = scheduleDao.getScheduleByProgramId(program.id).firstOrNull() ?: return@forEach
            val completedDays = WorkoutMapper.parseCompletedDaysJson(schedule.completedDaysJson)
            
            // Get all workouts for this program
            for (week in 1..program.totalWeeks) {
                val weekWorkouts = getWeekWorkouts(program.id, week)
                
                weekWorkouts.forEach { workout ->
                    // Calculate scheduled date: startDate + ((week - 1) * 7) + (day - 1)
                    val daysToAdd = ((week - 1) * 7) + (workout.dayNumber - 1)
                    val scheduledDate = programStartDate.plusDays(daysToAdd.toLong())
                    
                    // Only include if within date range
                    if (!scheduledDate.isBefore(startDate) && !scheduledDate.isAfter(endDate)) {
                        val dayKey = "$week-${workout.dayNumber}"
                        val isCompleted = completedDays.contains(dayKey)
                        
                        val scheduledWorkout = com.example.classpass.domain.model.ScheduledWorkoutDto(
                            programId = program.id,
                            programName = program.title,
                            programIcon = program.icon,
                            weekNumber = week,
                            dayNumber = workout.dayNumber,
                            scheduledDate = scheduledDate,
                            workout = workout,
                            isCompleted = isCompleted
                        )
                        
                        scheduledWorkouts.getOrPut(scheduledDate) { mutableListOf() }.add(scheduledWorkout)
                    }
                }
            }
        }
        
        return scheduledWorkouts
    }
    
    /**
     * Get today's scheduled workout
     */
    suspend fun getTodaysScheduledWorkout(userId: Long): com.example.classpass.domain.model.ScheduledWorkoutDto? {
        val today = java.time.LocalDate.now()
        val todaysWorkouts = getScheduledWorkoutsForDateRange(userId, today, today)
        return todaysWorkouts[today]?.firstOrNull()
    }
    
    /**
     * Get upcoming scheduled workouts (next N days)
     */
    suspend fun getUpcomingScheduledWorkouts(userId: Long, limit: Int = 5): List<com.example.classpass.domain.model.ScheduledWorkoutDto> {
        val today = java.time.LocalDate.now()
        val endDate = today.plusDays(30) // Look ahead 30 days
        
        val scheduledWorkouts = getScheduledWorkoutsForDateRange(userId, today.plusDays(1), endDate)
        
        return scheduledWorkouts.entries
            .sortedBy { it.key }
            .flatMap { it.value }
            .take(limit)
    }
}

/**
 * DTO for workout history
 */
data class WorkoutHistoryDto(
    val id: Long,
    val programId: String,
    val sessionId: String,
    val sessionName: String,
    val completedDate: Long,
    val durationMinutes: Int,
    val exercises: List<ExerciseDto>,
    val notes: String?
)
