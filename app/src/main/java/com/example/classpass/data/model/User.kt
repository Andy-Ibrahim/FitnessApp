package com.example.classpass.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey(autoGenerate = true)
    val userId: Long = 0,
    val name: String? = null,
    val email: String,
    val phoneNumber: String? = null,
    val dateOfBirth: String? = null,
    val height: Int? = null, // in cm
    val weight: Int? = null, // in kg
    val fitnessGoal: String? = null,
    val preferredActivities: String? = null, // Comma-separated
    val profilePictureUrl: String = "",
    val memberSince: Long = System.currentTimeMillis(),
    
    // Enhanced Profile Fields (AI-collected)
    val age: Int? = null,
    val gender: String? = null, // Male, Female, Other, Prefer not to say
    val fitnessLevel: String? = null, // Beginner, Intermediate, Advanced
    val primaryGoal: String? = null, // Primary fitness goal
    val bodyType: String? = null, // Ectomorph, Mesomorph, Endomorph
    val specificGoals: String? = null, // Detailed goals beyond basic fitness goal
    val motivation: String? = null, // What drives them
    val timeAvailability: String? = null, // e.g., "3-4 days per week, mornings"
    val workoutDuration: String? = null, // e.g., "30-45 minutes per session"
    val injuries: String? = null, // Current or past injuries
    val medicalConditions: String? = null, // Any conditions to be aware of
    val dietaryRestrictions: String? = null, // Food allergies, preferences
    val activityLevel: String? = null, // Sedentary, Lightly Active, Moderately Active, Very Active
    val sleepHours: String? = null, // Average hours of sleep
    val stressLevel: String? = null, // Low, Medium, High
    val previousExperience: String? = null, // Past gym/sports experience
    val favoriteWorkouts: String? = null, // Types they enjoy most
    val dislikedWorkouts: String? = null, // Types they want to avoid
    val targetWeight: Float? = null, // Goal weight in kg
    val targetDate: String? = null, // Goal date if they have one
    val profileCompleteness: Int = 0, // Percentage (0-100) of profile completion
    
    // Physical Metrics & Measurements
    val bodyFatPercentage: Float? = null, // Current body fat %
    val targetBodyFatPercentage: Float? = null, // Goal body fat %
    val muscleMass: Float? = null, // Current muscle mass (kg)
    val bmi: Float? = null, // Body Mass Index (calculated)
    val waistCircumference: Float? = null, // cm
    val chestCircumference: Float? = null, // cm
    val armCircumference: Float? = null // cm
)

