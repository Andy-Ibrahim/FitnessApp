package com.example.classpass.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.classpass.ClassPassApplication
import com.example.classpass.domain.model.*
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch

/**
 * NEW ViewModel for WorkoutsScreen using DTOs and template system.
 * 
 * This replaces the old WorkoutsViewModel and uses:
 * - DTOs instead of entities
 * - Template system instead of 84 individual workouts
 * - Clean architecture with Repository pattern
 */
class WorkoutsViewModelNew(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as ClassPassApplication).workoutRepository
    private val userId = 1L // TODO: Get from UserRepository
    
    // ========== Observed Data ==========
    
    /**
     * All workout programs for the user
     */
    private val _allPrograms = MutableLiveData<List<WorkoutProgramDto>>(emptyList())
    val allPrograms: LiveData<List<WorkoutProgramDto>> = _allPrograms
    
    /**
     * Currently active program
     */
    private val _activeProgram = MutableLiveData<WorkoutProgramDto?>()
    val activeProgram: LiveData<WorkoutProgramDto?> = _activeProgram
    
    /**
     * Current week's workouts
     */
    private val _currentWeekWorkouts = MutableLiveData<List<WorkoutSessionDto>>(emptyList())
    val currentWeekWorkouts: LiveData<List<WorkoutSessionDto>> = _currentWeekWorkouts
    
    /**
     * Today's workout (if any) - with full scheduling info
     */
    private val _todaysWorkout = MutableLiveData<ScheduledWorkoutDto?>()
    val todaysWorkout: LiveData<ScheduledWorkoutDto?> = _todaysWorkout
    
    /**
     * Upcoming workouts (next 5) - with full scheduling info
     */
    private val _upcomingWorkouts = MutableLiveData<List<ScheduledWorkoutDto>>(emptyList())
    val upcomingWorkouts: LiveData<List<ScheduledWorkoutDto>> = _upcomingWorkouts
    
    /**
     * Calendar workouts (map of dates to workouts)
     */
    private val _calendarWorkouts = MutableLiveData<Map<java.time.LocalDate, List<WorkoutSessionDto>>>(emptyMap())
    val calendarWorkouts: LiveData<Map<java.time.LocalDate, List<WorkoutSessionDto>>> = _calendarWorkouts
    
    /**
     * Program count
     */
    private val _programCount = MutableLiveData(0)
    val programCount: LiveData<Int> = _programCount
    
    // ========== UI State ==========
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    // ========== Initialization ==========
    
    init {
        loadPrograms()
        loadActiveProgram()
    }
    
    // ========== Load Data ==========
    
    /**
     * Load all programs for the user
     */
    fun loadPrograms() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val programs = repository.getUserProgramsDetailed(userId)
                _allPrograms.value = programs
                _programCount.value = programs.size
                
                // Load calendar data for all programs
                loadTodaysWorkout()
                loadUpcomingWorkouts()
                loadCalendarWorkouts()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load programs: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load active program and current week workouts
     */
    fun loadActiveProgram() {
        viewModelScope.launch {
            try {
                val program = repository.getActiveProgram(userId)
                _activeProgram.value = program
                
                if (program != null) {
                    loadCurrentWeekWorkouts(program.id, program.currentWeek)
                    // Calendar data is loaded in loadPrograms()
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load active program: ${e.message}"
            }
        }
    }
    
    /**
     * Load workouts for a specific week
     */
    private suspend fun loadCurrentWeekWorkouts(programId: String, weekNumber: Int) {
        try {
            val workouts = repository.getWeekWorkouts(programId, weekNumber)
            _currentWeekWorkouts.value = workouts
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load week workouts: ${e.message}"
        }
    }
    
    /**
     * Load today's workout using scheduled dates
     */
    private suspend fun loadTodaysWorkout() {
        try {
            val todayScheduled = repository.getTodaysScheduledWorkout(userId)
            _todaysWorkout.value = todayScheduled // Keep full ScheduledWorkoutDto!
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load today's workout: ${e.message}"
        }
    }
    
    /**
     * Load upcoming workouts using scheduled dates
     */
    private suspend fun loadUpcomingWorkouts() {
        try {
            val upcomingScheduled = repository.getUpcomingScheduledWorkouts(userId, limit = 5)
            _upcomingWorkouts.value = upcomingScheduled // Keep full ScheduledWorkoutDto list!
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load upcoming workouts: ${e.message}"
        }
    }
    
    /**
     * Load calendar workouts for date range (current month ± 1 month)
     * Note: Currently unused as calendar view was removed, but kept for potential future use
     */
    private suspend fun loadCalendarWorkouts() {
        try {
            val today = java.time.LocalDate.now()
            val startDate = today.minusMonths(1).withDayOfMonth(1)
            val endDate = today.plusMonths(1).withDayOfMonth(today.plusMonths(1).lengthOfMonth())
            
            val scheduledWorkouts = repository.getScheduledWorkoutsForDateRange(userId, startDate, endDate)
            
            // Convert ScheduledWorkoutDto map to WorkoutSessionDto map for UI compatibility
            val calendarMap = scheduledWorkouts.mapValues { entry ->
                entry.value.map { it.workout }
            }
            
            _calendarWorkouts.value = calendarMap
        } catch (e: Exception) {
            _errorMessage.value = "Failed to load calendar: ${e.message}"
        }
    }
    
    // ========== Program Operations ==========
    
    /**
     * Create a new workout program
     * 
     * This is called after AI generates the weekly template
     */
    fun createProgram(
        title: String,
        description: String,
        icon: String,
        durationWeeks: Int,
        daysPerWeek: Int,
        weeklyWorkouts: List<Pair<String, List<ExerciseDto>>>
    ) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val programId = repository.createProgram(
                    userId = userId,
                    title = title,
                    description = description,
                    icon = icon,
                    durationWeeks = durationWeeks,
                    daysPerWeek = daysPerWeek,
                    weeklyWorkouts = weeklyWorkouts
                )
                
                _successMessage.value = "Program created successfully!"
                loadPrograms()
                loadActiveProgram()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create program: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Complete a workout
     */
    fun completeWorkout(programId: String, weekNumber: Int, dayNumber: Int) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                // Use 0 duration as this is called directly without a workout session
                repository.completeWorkout(programId, weekNumber, dayNumber, 0)
                _successMessage.value = "Workout completed! 💪"
                
                // Reload data
                loadActiveProgram()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to complete workout: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Navigate to a specific week
     */
    fun navigateToWeek(programId: String, weekNumber: Int) {
        viewModelScope.launch {
            try {
                loadCurrentWeekWorkouts(programId, weekNumber)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load week: ${e.message}"
            }
        }
    }
    
    /**
     * Delete a program (accepts DTO)
     */
    fun deleteProgram(program: WorkoutProgramDto) {
        deleteProgram(program.id)
    }
    
    /**
     * Delete a program by ID
     */
    fun deleteProgram(programId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.deleteProgram(programId)
                _successMessage.value = "Program deleted"
                
                loadPrograms()
                loadActiveProgram()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete program: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Rename a program
     */
    fun renameProgram(program: WorkoutProgramDto, newTitle: String) {
        renameProgram(program.id, newTitle)
    }
    
    /**
     * Rename a program by ID
     */
    fun renameProgram(programId: String, newTitle: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                repository.renameProgram(programId, newTitle)
                _successMessage.value = "Program renamed"
                
                loadPrograms()
                loadActiveProgram()
            } catch (e: Exception) {
                _errorMessage.value = "Failed to rename program: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Load mock programs for testing
     */
    fun loadMockPrograms() {
        viewModelScope.launch {
            try {
                // Create a mock 12-week strength program
                val mockExercises = listOf(
                    Pair("Upper Body Push", listOf(
                        ExerciseDto(
                            id = "ex1",
                            name = "Bench Press",
                            sets = 4,
                            reps = 8,
                            weight = 60f,
                            restSeconds = 120,
                            notes = "Focus on form"
                        ),
                        ExerciseDto(
                            id = "ex2",
                            name = "Overhead Press",
                            sets = 3,
                            reps = 10,
                            weight = 40f,
                            restSeconds = 90
                        )
                    )),
                    Pair("Rest", emptyList()),
                    Pair("Lower Body", listOf(
                        ExerciseDto(
                            id = "ex3",
                            name = "Squats",
                            sets = 4,
                            reps = 8,
                            weight = 80f,
                            restSeconds = 120
                        ),
                        ExerciseDto(
                            id = "ex4",
                            name = "Deadlifts",
                            sets = 3,
                            reps = 6,
                            weight = 100f,
                            restSeconds = 180
                        )
                    )),
                    Pair("Rest", emptyList()),
                    Pair("Upper Body Pull", listOf(
                        ExerciseDto(
                            id = "ex5",
                            name = "Pull-ups",
                            sets = 4,
                            reps = 10,
                            restSeconds = 90
                        ),
                        ExerciseDto(
                            id = "ex6",
                            name = "Rows",
                            sets = 4,
                            reps = 10,
                            weight = 50f,
                            restSeconds = 90
                        )
                    )),
                    Pair("Rest", emptyList()),
                    Pair("Rest", emptyList())
                )
                
                createProgram(
                    title = "12-Week Strength Program",
                    description = "Build strength with progressive overload",
                    icon = "💪",
                    durationWeeks = 12,
                    daysPerWeek = 3,
                    weeklyWorkouts = mockExercises
                )
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create mock program: ${e.message}"
            }
        }
    }
    
    // ========== Utility ==========
    
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
     * Get workout for a specific day
     */
    fun getWorkoutForDay(dayNumber: Int): WorkoutSessionDto? {
        return _currentWeekWorkouts.value?.find { it.dayNumber == dayNumber }
    }
    
    /**
     * Check if a specific day is completed
     */
    fun isDayCompleted(dayNumber: Int): Boolean {
        return getWorkoutForDay(dayNumber)?.isCompleted ?: false
    }
}

