package com.example.classpass.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.classpass.ClassPassApplication
import com.example.classpass.domain.model.ExerciseDto
import com.example.classpass.domain.model.WorkoutSessionDto
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel for Workout Session Screen (exercise execution)
 */
class WorkoutSessionViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as ClassPassApplication).workoutRepository
    
    private val _session = MutableLiveData<WorkoutSessionDto?>()
    val session: LiveData<WorkoutSessionDto?> = _session
    
    private val _currentExerciseIndex = MutableLiveData(0)
    val currentExerciseIndex: LiveData<Int> = _currentExerciseIndex
    
    private val _completedExercises = MutableLiveData<Set<String>>(emptySet())
    val completedExercises: LiveData<Set<String>> = _completedExercises
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _isWorkoutComplete = MutableLiveData(false)
    val isWorkoutComplete: LiveData<Boolean> = _isWorkoutComplete
    
    // Global workout timer
    private val _workoutTimeSeconds = MutableLiveData(0)
    val workoutTimeSeconds: LiveData<Int> = _workoutTimeSeconds
    
    private val _isTimerRunning = MutableLiveData(false)
    val isTimerRunning: LiveData<Boolean> = _isTimerRunning
    
    private var timerJob: Job? = null
    
    /**
     * Load workout session
     */
    fun loadSession(programId: String, weekNumber: Int, dayNumber: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val sessionData = repository.getWorkoutSession(programId, weekNumber, dayNumber)
                _session.value = sessionData
                _currentExerciseIndex.value = 0
                
                // Start the global timer automatically
                startTimer()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load workout: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Start the global workout timer
     */
    fun startTimer() {
        if (_isTimerRunning.value == true) return
        
        _isTimerRunning.value = true
        timerJob = viewModelScope.launch {
            while (_isTimerRunning.value == true) {
                delay(1000)
                _workoutTimeSeconds.value = (_workoutTimeSeconds.value ?: 0) + 1
            }
        }
    }
    
    /**
     * Pause the global workout timer
     */
    fun pauseTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
    }
    
    /**
     * Resume the global workout timer
     */
    fun resumeTimer() {
        startTimer()
    }
    
    /**
     * Stop and reset the timer
     */
    fun stopTimer() {
        _isTimerRunning.value = false
        timerJob?.cancel()
        _workoutTimeSeconds.value = 0
    }
    
    /**
     * Mark exercise as complete
     */
    fun toggleExerciseComplete(exerciseId: String) {
        val current = _completedExercises.value?.toMutableSet() ?: mutableSetOf()
        if (current.contains(exerciseId)) {
            current.remove(exerciseId)
        } else {
            current.add(exerciseId)
        }
        _completedExercises.value = current
        
        // Check if all exercises are complete
        val session = _session.value
        if (session != null && !session.isRestDay) {
            val allComplete = session.exercises.all { current.contains(it.id) }
            _isWorkoutComplete.value = allComplete
        }
    }
    
    /**
     * Move to next exercise
     */
    fun nextExercise() {
        val session = _session.value ?: return
        val currentIndex = _currentExerciseIndex.value ?: 0
        if (currentIndex < session.exercises.size - 1) {
            _currentExerciseIndex.value = currentIndex + 1
        }
    }
    
    /**
     * Move to previous exercise
     */
    fun previousExercise() {
        val currentIndex = _currentExerciseIndex.value ?: 0
        if (currentIndex > 0) {
            _currentExerciseIndex.value = currentIndex - 1
        }
    }
    
    /**
     * Complete the entire workout and return history ID
     */
    fun completeWorkout(programId: String, weekNumber: Int, dayNumber: Int, onSuccess: (Long) -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Get workout duration from timer
                val durationSeconds = _workoutTimeSeconds.value ?: 0
                
                // Complete workout and get history ID
                val historyId = repository.completeWorkout(programId, weekNumber, dayNumber, durationSeconds)
                onSuccess(historyId)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to complete workout: ${e.message}"
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
     * Get current exercise
     */
    fun getCurrentExercise(): ExerciseDto? {
        val session = _session.value ?: return null
        val index = _currentExerciseIndex.value ?: 0
        return session.exercises.getOrNull(index)
    }
    
    /**
     * Check if exercise is completed
     */
    fun isExerciseCompleted(exerciseId: String): Boolean {
        return _completedExercises.value?.contains(exerciseId) ?: false
    }
}

