package com.example.classpass.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.classpass.ClassPassApplication
import com.example.classpass.domain.model.WorkoutProgramDto
import com.example.classpass.domain.model.WorkoutSessionDto
import com.example.classpass.domain.model.ExerciseDto
import kotlinx.coroutines.launch

/**
 * ViewModel for Program Detail Screen
 */
class ProgramDetailViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as ClassPassApplication).workoutRepository
    private val userId = 1L // TODO: Get from UserRepository
    
    private val _program = MutableLiveData<WorkoutProgramDto?>()
    val program: LiveData<WorkoutProgramDto?> = _program
    
    private val _currentWeekWorkouts = MutableLiveData<List<WorkoutSessionDto>>(emptyList())
    val currentWeekWorkouts: LiveData<List<WorkoutSessionDto>> = _currentWeekWorkouts
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    /**
     * Load program by ID
     */
    fun loadProgram(programId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val programData = repository.getProgramById(programId)
                _program.value = programData
                
                if (programData != null) {
                    loadWeek(programId, programData.currentWeek)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load program: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load workouts for a specific week
     */
    fun loadWeek(programId: String, weekNumber: Int) {
        viewModelScope.launch {
            try {
                val workouts = repository.getWeekWorkouts(programId, weekNumber)
                _currentWeekWorkouts.value = workouts
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load week: ${e.message}"
            }
        }
    }
    
    /**
     * Complete a workout
     */
    fun completeWorkout(programId: String, weekNumber: Int, dayNumber: Int) {
        viewModelScope.launch {
            try {
                // Use 0 duration as this is called directly without a workout session
                repository.completeWorkout(programId, weekNumber, dayNumber, 0)
                _successMessage.value = "Workout completed! 🎉"
                // Reload program and week to show updated progress
                loadProgram(programId)
                loadWeek(programId, weekNumber)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to complete workout: ${e.message}"
            }
        }
    }
    
    /**
     * Delete the program
     */
    fun deleteProgram(programId: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteProgram(programId)
                // Navigate immediately before any state changes
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete program: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Clear error message
     */
    fun clearError() {
        _errorMessage.value = null
    }
    
    /**
     * Clear success message
     */
    fun clearSuccess() {
        _successMessage.value = null
    }
    
    /**
     * Get history ID for a specific workout session
     */
    fun getHistoryIdForSession(programId: String, sessionId: String, callback: (Long?) -> Unit) {
        viewModelScope.launch {
            try {
                val histories = repository.getHistoryForSession(programId, sessionId)
                // Get the most recent history entry for this session
                callback(histories.firstOrNull()?.id)
            } catch (e: Exception) {
                callback(null)
            }
        }
    }
    
    /**
     * Add an exercise to a workout day
     */
    fun addExercise(programId: String, weekNumber: Int, dayNumber: Int, exercise: ExerciseDto, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.addExerciseToDay(programId, dayNumber, exercise)
                _successMessage.value = "Exercise added! ✅"
                // Reload the current week to show updated exercises
                loadWeek(programId, weekNumber)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add exercise: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update an exercise in a workout day
     */
    fun updateExercise(
        programId: String,
        weekNumber: Int,
        dayNumber: Int,
        exerciseIndex: Int,
        updatedExercise: ExerciseDto,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.updateExerciseInDay(programId, dayNumber, exerciseIndex, updatedExercise)
                _successMessage.value = "Exercise updated! ✅"
                // Reload the current week to show updated exercises
                loadWeek(programId, weekNumber)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update exercise: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Delete an exercise from a workout day
     */
    fun deleteExercise(programId: String, weekNumber: Int, dayNumber: Int, exerciseIndex: Int, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteExerciseFromDay(programId, dayNumber, exerciseIndex)
                _successMessage.value = "Exercise deleted! 🗑️"
                // Reload the current week to show updated exercises
                loadWeek(programId, weekNumber)
                onSuccess()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete exercise: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Save rest day log (notes, feeling, activities)
     */
    fun saveRestDayLog(
        programId: String,
        weekNumber: Int,
        dayNumber: Int,
        note: String,
        feeling: String,
        activities: List<String>
    ) {
        viewModelScope.launch {
            try {
                repository.saveRestDayLog(
                    userId = userId,
                    programId = programId,
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    feeling = feeling,
                    activities = activities,
                    note = note
                )
                _successMessage.value = "Rest day logged! 💪"
            } catch (e: Exception) {
                _errorMessage.value = "Failed to save rest day log: ${e.message}"
            }
        }
    }
    
    /**
     * Load rest day log for a specific day
     */
    fun loadRestDayLog(
        programId: String,
        weekNumber: Int,
        dayNumber: Int,
        onLoaded: (feeling: String, activities: List<String>, note: String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.getRestDayLog(programId, weekNumber, dayNumber).collect { log ->
                    if (log != null) {
                        // Simple parsing of JSON array: ["activity1", "activity2"]
                        val activities = try {
                            log.activitiesJson
                                .removeSurrounding("[", "]")
                                .split("\",\"")
                                .map { it.trim().removeSurrounding("\"") }
                                .filter { it.isNotEmpty() }
                        } catch (e: Exception) {
                            emptyList()
                        }
                        onLoaded(log.feeling, activities, log.note)
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load rest day log: ${e.message}"
            }
        }
    }
}

