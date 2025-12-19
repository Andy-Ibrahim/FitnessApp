package com.example.classpass.domain.model

import kotlinx.serialization.Serializable

/**
 * DTOs (Data Transfer Objects) for Workout domain.
 * These are clean, UI-friendly models that don't know about database structure.
 */

/**
 * Represents a workout program (e.g., "12-Week Strength Program")
 */
@Serializable
data class WorkoutProgramDto(
    val id: String,
    val title: String,
    val description: String,
    val icon: String = "💪",
    val totalWeeks: Int,
    val daysPerWeek: Int,
    val status: ProgramStatusDto,
    val currentWeek: Int = 1,
    val currentDay: Int = 1,
    val startDate: Long? = null,
    val completionPercentage: Float = 0f,
    val createdDate: Long = System.currentTimeMillis()
)

/**
 * Represents a single workout session (e.g., "Upper Body Push")
 */
@Serializable
data class WorkoutSessionDto(
    val id: String,
    val dayNumber: Int, // 1-7 (Monday-Sunday)
    val name: String, // "Upper Body Push", "Lower Body", "Rest"
    val exercises: List<ExerciseDto>,
    val estimatedDuration: Int, // Minutes
    val isCompleted: Boolean = false,
    val isRestDay: Boolean = false,
    val completedDate: Long? = null,
    val notes: String? = null
)

/**
 * Represents an individual exercise
 */
@Serializable
data class ExerciseDto(
    val id: String,
    val name: String, // "Bench Press", "Squats"
    val sets: Int,
    val reps: Int,
    val weight: Float? = null, // kg or lbs
    val restSeconds: Int = 90,
    val notes: String? = null,
    val isCompleted: Boolean = false,
    val videoUrl: String? = null
)

/**
 * Program status
 */
@Serializable
enum class ProgramStatusDto {
    NOT_STARTED,
    ACTIVE,
    IN_PROGRESS,
    PAUSED,
    COMPLETED
}

/**
 * Workout history entry
 */
@Serializable
data class WorkoutHistoryDto(
    val id: Long = 0,
    val programId: String,
    val sessionId: String,
    val sessionName: String,
    val completedDate: Long,
    val duration: Int, // Minutes
    val exercises: List<ExerciseDto>,
    val notes: String? = null
)

/**
 * Progress statistics
 */
@Serializable
data class ProgressStatsDto(
    val totalWorkouts: Int,
    val totalDuration: Int, // Minutes
    val currentStreak: Int, // Days
    val completionRate: Float, // 0.0 - 1.0
    val averageDuration: Int // Minutes
)

