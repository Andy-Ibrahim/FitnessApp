package com.example.classpass.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.classpass.ClassPassApplication
import com.example.classpass.data.repository.WorkoutHistoryDto
import kotlinx.coroutines.launch

/**
 * ViewModel for Workout Summary Screen
 */
class WorkoutSummaryViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as ClassPassApplication).workoutRepository
    
    private val _workoutHistory = MutableLiveData<WorkoutHistoryDto?>()
    val workoutHistory: LiveData<WorkoutHistoryDto?> = _workoutHistory
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    /**
     * Load workout history by ID
     */
    fun loadWorkoutHistory(historyId: Long) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val history = repository.getWorkoutHistoryById(historyId)
                _workoutHistory.value = history
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load workout history: ${e.message}"
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
}

