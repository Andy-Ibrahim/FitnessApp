package com.example.classpass.domain.model

import java.time.LocalDate

/**
 * DTO representing a workout scheduled on a specific date
 * Combines program info, workout details, and scheduled date
 */
data class ScheduledWorkoutDto(
    val programId: String,
    val programName: String,
    val programIcon: String,
    val weekNumber: Int,
    val dayNumber: Int,
    val scheduledDate: LocalDate,
    val workout: WorkoutSessionDto,
    val isCompleted: Boolean
)

