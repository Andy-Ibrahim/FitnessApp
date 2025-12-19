package com.example.classpass.service

import com.example.classpass.domain.model.ExerciseDto
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.util.UUID

/**
 * AI Service for generating workout programs using weekly templates.
 * 
 * This service:
 * - Generates a SINGLE weekly template (7 days)
 * - That template repeats for all weeks in the program
 * - Reduces AI generation from 84 workouts to 7 workouts
 * - 90% storage reduction
 */
class WorkoutAIService(private val apiKey: String) {
    
    private val model = GenerativeModel(
        modelName = "gemini-1.5-flash",
        apiKey = apiKey
    )
    
    private val json = Json { 
        ignoreUnknownKeys = true
        isLenient = true
    }
    
    /**
     * Generate a weekly workout template based on user profile
     * 
     * @param userProfile User's fitness profile
     * @param programGoal Program goal (e.g., "Build Strength", "Lose Weight")
     * @param durationWeeks Total program duration (e.g., 12)
     * @param daysPerWeek Training days per week (3-7)
     * @return Weekly template (7 days, some may be rest days)
     */
    suspend fun generateWeeklyTemplate(
        userProfile: UserProfile,
        programGoal: String,
        durationWeeks: Int,
        daysPerWeek: Int
    ): WeeklyTemplateResponse {
        
        val prompt = buildWeeklyTemplatePrompt(userProfile, programGoal, durationWeeks, daysPerWeek)
        
        val response = model.generateContent(
            content {
                text(prompt)
            }
        )
        
        val responseText = response.text ?: throw Exception("No response from AI")
        
        // Parse JSON response
        return parseWeeklyTemplateResponse(responseText)
    }
    
    /**
     * Build the AI prompt for weekly template generation
     */
    private fun buildWeeklyTemplatePrompt(
        userProfile: UserProfile,
        programGoal: String,
        durationWeeks: Int,
        daysPerWeek: Int
    ): String {
        return """
You are an expert fitness coach. Generate a weekly workout template for a $durationWeeks-week program.

**IMPORTANT**: Generate ONLY ONE WEEK (7 days). This template will repeat for all $durationWeeks weeks.

User Profile:
- Name: ${userProfile.name}
- Age: ${userProfile.age ?: "Not specified"}
- Gender: ${userProfile.gender ?: "Not specified"}
- Fitness Level: ${userProfile.fitnessLevel ?: "Beginner"}
- Primary Goal: $programGoal
- Training Days: $daysPerWeek days per week
- Injuries/Limitations: ${userProfile.injuries ?: "None"}

Requirements:
1. Generate EXACTLY 7 days (Monday-Sunday)
2. $daysPerWeek days should have workouts
3. ${7 - daysPerWeek} days should be REST days
4. Each workout should have 4-8 exercises
5. Each exercise should have sets, reps, and rest time
6. Workouts should be balanced (push/pull/legs or upper/lower split)
7. Consider user's fitness level and injuries

Return ONLY valid JSON in this exact format:
{
  "programTitle": "12-Week Strength Program",
  "programDescription": "Build strength with progressive overload",
  "programIcon": "💪",
  "weeklySchedule": [
    {
      "dayNumber": 1,
      "dayName": "Monday",
      "workoutType": "Upper Body Push",
      "isRestDay": false,
      "exercises": [
        {
          "name": "Bench Press",
          "sets": 4,
          "reps": 8,
          "weight": null,
          "restSeconds": 120,
          "notes": "Focus on form"
        }
      ]
    },
    {
      "dayNumber": 2,
      "dayName": "Tuesday",
      "workoutType": "Rest",
      "isRestDay": true,
      "exercises": []
    }
  ]
}

Generate the weekly template now:
        """.trimIndent()
    }
    
    /**
     * Parse AI response into WeeklyTemplateResponse
     */
    private fun parseWeeklyTemplateResponse(responseText: String): WeeklyTemplateResponse {
        // Extract JSON from markdown code blocks if present
        val jsonText = responseText
            .substringAfter("```json", "")
            .substringAfter("```", "")
            .substringBefore("```", responseText)
            .trim()
        
        return json.decodeFromString<WeeklyTemplateResponse>(jsonText)
    }
    
    // ========== Data Classes ==========
    
    /**
     * User profile for AI generation
     */
    @Serializable
    data class UserProfile(
        val name: String,
        val age: Int? = null,
        val gender: String? = null,
        val fitnessLevel: String? = null,
        val injuries: String? = null
    )
    
    /**
     * AI response for weekly template
     */
    @Serializable
    data class WeeklyTemplateResponse(
        val programTitle: String,
        val programDescription: String,
        val programIcon: String,
        val weeklySchedule: List<DaySchedule>
    )
    
    /**
     * Single day in the weekly schedule
     */
    @Serializable
    data class DaySchedule(
        val dayNumber: Int,
        val dayName: String,
        val workoutType: String,
        val isRestDay: Boolean,
        val exercises: List<ExerciseResponse>
    )
    
    /**
     * Exercise from AI response
     */
    @Serializable
    data class ExerciseResponse(
        val name: String,
        val sets: Int,
        val reps: Int,
        val weight: Float? = null,
        val restSeconds: Int,
        val notes: String? = null
    )
    
    /**
     * Convert AI response to domain DTOs
     */
    fun convertToWeeklyWorkouts(response: WeeklyTemplateResponse): List<Pair<String, List<ExerciseDto>>> {
        return response.weeklySchedule.map { day ->
            val workoutType = day.workoutType
            val exercises = day.exercises.map { exercise ->
                ExerciseDto(
                    id = UUID.randomUUID().toString(),
                    name = exercise.name,
                    sets = exercise.sets,
                    reps = exercise.reps,
                    weight = exercise.weight,
                    restSeconds = exercise.restSeconds,
                    notes = exercise.notes,
                    isCompleted = false,
                    videoUrl = null
                )
            }
            Pair(workoutType, exercises)
        }
    }
}

