package com.example.classpass.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.classpass.ClassPassApplication
import com.example.classpass.domain.model.ExerciseDto
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.util.UUID

/**
 * ViewModel for Create Your Own Program flow.
 * Manages program builder state and saves to database.
 */
class CreateProgramViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository = (application as ClassPassApplication).workoutRepository
    private val userId = 1L // TODO: Get from UserRepository
    
    // Edit mode state
    private val _editProgramId = MutableLiveData<String?>(null)
    val editProgramId: LiveData<String?> = _editProgramId
    
    private val _isEditMode = MutableLiveData(false)
    val isEditMode: LiveData<Boolean> = _isEditMode
    
    // ========== Program Setup (Step 1) ==========
    
    private val _programName = MutableLiveData("My Workout Program")
    val programName: LiveData<String> = _programName
    
    private val _durationWeeks = MutableLiveData(12)
    val durationWeeks: LiveData<Int> = _durationWeeks
    
    private val _trainingDaysPerWeek = MutableLiveData(4)
    val trainingDaysPerWeek: LiveData<Int> = _trainingDaysPerWeek
    
    private val _selectedDays = MutableLiveData<Set<DayOfWeek>>(
        setOf(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY)
    )
    val selectedDays: LiveData<Set<DayOfWeek>> = _selectedDays
    
    // ========== Weekly Template (Step 2) ==========
    
    private val _weeklyTemplate = MutableLiveData<Map<Int, DayWorkout>>(emptyMap())
    val weeklyTemplate: LiveData<Map<Int, DayWorkout>> = _weeklyTemplate
    
    // ========== Current Step ==========
    
    private val _currentStep = MutableLiveData(BuilderStep.SETUP)
    val currentStep: LiveData<BuilderStep> = _currentStep
    
    // ========== UI State ==========
    
    private val _isLoading = MutableLiveData(false)
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage
    
    private val _successMessage = MutableLiveData<String?>()
    val successMessage: LiveData<String?> = _successMessage
    
    // ========== Initialization ==========
    
    init {
        initializeWeeklyTemplate()
    }
    
    /**
     * Initialize weekly template with 7 days (all rest days by default)
     */
    private fun initializeWeeklyTemplate() {
        val template = mutableMapOf<Int, DayWorkout>()
        val selectedDaysList = _selectedDays.value?.toList() ?: emptyList()
        
        var workoutCounter = 1 // Sequential workout numbering
        
        for (dayNumber in 1..7) {
            val dayOfWeek = DayOfWeek.of(dayNumber)
            val isTrainingDay = selectedDaysList.contains(dayOfWeek)
            
            template[dayNumber] = DayWorkout(
                dayNumber = dayNumber,
                dayName = dayOfWeek.name.lowercase().replaceFirstChar { it.uppercase() },
                workoutName = if (isTrainingDay) {
                    "Workout Day ${workoutCounter++}" // Use sequential counter
                } else {
                    "Rest"
                },
                exercises = emptyList(),
                isRestDay = !isTrainingDay
            )
        }
        
        _weeklyTemplate.value = template
    }
    
    // ========== Step 1: Program Setup ==========
    
    fun updateProgramName(name: String) {
        _programName.value = name
    }
    
    fun updateDurationWeeks(weeks: Int) {
        _durationWeeks.value = weeks
    }
    
    fun updateTrainingDaysPerWeek(days: Int) {
        _trainingDaysPerWeek.value = days
        
        // If user has more days selected than the new limit, trim to match
        val currentSelected = _selectedDays.value?.toMutableSet() ?: mutableSetOf()
        if (currentSelected.size > days) {
            // Remove excess days (keep first N selected)
            val sortedSelected = currentSelected.sortedBy { it.value }
            val daysToKeep = sortedSelected.take(days).toSet()
            _selectedDays.value = daysToKeep
            
            // Update template
            initializeWeeklyTemplate()
        }
    }
    
    fun toggleDay(day: DayOfWeek) {
        val current = _selectedDays.value?.toMutableSet() ?: mutableSetOf()
        val maxDays = _trainingDaysPerWeek.value ?: 4
        
        if (current.contains(day)) {
            // Always allow deselecting
            current.remove(day)
        } else {
            // Only allow selecting if under the limit
            if (current.size < maxDays) {
                current.add(day)
            } else {
                // At limit - show error or do nothing
                _errorMessage.value = "Maximum $maxDays training days selected. Increase the limit to add more."
                return
            }
        }
        
        _selectedDays.value = current
        
        // Update template
        initializeWeeklyTemplate()
    }
    
    fun proceedToWeeklyTemplate() {
        if (_programName.value.isNullOrBlank()) {
            _errorMessage.value = "Please enter a program name"
            return
        }
        if ((_selectedDays.value?.size ?: 0) < 1) {
            _errorMessage.value = "Please select at least one training day"
            return
        }
        _currentStep.value = BuilderStep.WEEKLY_TEMPLATE
    }
    
    // ========== Step 2: Weekly Template ==========
    
    fun updateDayWorkout(dayNumber: Int, workout: DayWorkout) {
        val current = _weeklyTemplate.value?.toMutableMap() ?: mutableMapOf()
        current[dayNumber] = workout
        
        // Recalculate training days count based on actual workout days
        val actualTrainingDays = current.values.count { !it.isRestDay }
        if (actualTrainingDays != _trainingDaysPerWeek.value) {
            _trainingDaysPerWeek.value = actualTrainingDays
            
            // Update selected days to match
            val selectedDaysList = current.filter { !it.value.isRestDay }
                .map { DayOfWeek.of(it.key) }
                .toSet()
            _selectedDays.value = selectedDaysList
        }
        
        // Renumber all workout days sequentially
        var workoutCounter = 1
        val renumberedTemplate = mutableMapOf<Int, DayWorkout>()
        
        for (day in 1..7) {
            val dayWorkout = current[day]
            if (dayWorkout != null) {
                renumberedTemplate[day] = if (dayWorkout.isRestDay) {
                    dayWorkout.copy(workoutName = "Rest")
                } else {
                    dayWorkout.copy(workoutName = "Workout Day ${workoutCounter++}")
                }
            }
        }
        
        _weeklyTemplate.value = renumberedTemplate
    }
    
    fun toggleRestDay(dayNumber: Int) {
        val current = _weeklyTemplate.value?.toMutableMap() ?: mutableMapOf()
        val day = current[dayNumber] ?: return
        
        current[dayNumber] = day.copy(
            isRestDay = !day.isRestDay,
            workoutName = if (!day.isRestDay) "Rest" else "Workout Day $dayNumber",
            exercises = if (!day.isRestDay) emptyList() else day.exercises
        )
        _weeklyTemplate.value = current
    }
    
    fun addExerciseToDay(dayNumber: Int, exercise: ExerciseBuilder) {
        val current = _weeklyTemplate.value?.toMutableMap() ?: mutableMapOf()
        val day = current[dayNumber] ?: return
        
        val updatedExercises = day.exercises.toMutableList()
        updatedExercises.add(exercise)
        
        current[dayNumber] = day.copy(exercises = updatedExercises)
        _weeklyTemplate.value = current
    }
    
    fun removeExerciseFromDay(dayNumber: Int, exerciseId: String) {
        val current = _weeklyTemplate.value?.toMutableMap() ?: mutableMapOf()
        val day = current[dayNumber] ?: return
        
        val updatedExercises = day.exercises.filter { it.id != exerciseId }
        
        current[dayNumber] = day.copy(exercises = updatedExercises)
        _weeklyTemplate.value = current
    }
    
    fun updateExerciseInDay(dayNumber: Int, exercise: ExerciseBuilder) {
        val current = _weeklyTemplate.value?.toMutableMap() ?: mutableMapOf()
        val day = current[dayNumber] ?: return
        
        val updatedExercises = day.exercises.map { 
            if (it.id == exercise.id) exercise else it 
        }
        
        current[dayNumber] = day.copy(exercises = updatedExercises)
        _weeklyTemplate.value = current
    }
    
    // ========== Step 3: Create/Update Program ==========
    
    /**
     * Load existing program data for editing
     */
    fun loadProgramForEdit(programId: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _isEditMode.value = true
                _editProgramId.value = programId
                
                val program = repository.getProgramById(programId)
                if (program == null) {
                    _errorMessage.value = "Program not found"
                    return@launch
                }
                
                // Load program details
                _programName.value = program.title
                _durationWeeks.value = program.totalWeeks
                _trainingDaysPerWeek.value = program.daysPerWeek
                
                // Load weekly template from first week
                val weekWorkouts = repository.getWeekWorkouts(programId, 1)
                val templateMap = mutableMapOf<Int, DayWorkout>()
                
                weekWorkouts.forEachIndexed { index, workout ->
                    val dayNumber = index + 1
                    templateMap[dayNumber] = DayWorkout(
                        dayNumber = dayNumber,
                        dayName = DayOfWeek.of(dayNumber).name.lowercase().replaceFirstChar { it.uppercase() },
                        workoutName = workout.name,
                        exercises = workout.exercises.map { ex ->
                            ExerciseBuilder(
                                id = ex.id,
                                name = ex.name,
                                sets = ex.sets,
                                reps = ex.reps,
                                weight = ex.weight,
                                restSeconds = ex.restSeconds,
                                notes = ex.notes
                            )
                        },
                        isRestDay = workout.isRestDay
                    )
                }
                
                _weeklyTemplate.value = templateMap
                
                // Update selected days based on workout days
                val selectedDaysList = mutableSetOf<DayOfWeek>()
                templateMap.values.forEachIndexed { index, day ->
                    if (!day.isRestDay) {
                        selectedDaysList.add(DayOfWeek.of(index + 1))
                    }
                }
                _selectedDays.value = selectedDaysList
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to load program: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Create or update program based on edit mode
     */
    fun createProgram(onSuccess: () -> Unit) {
        if (_isEditMode.value == true && _editProgramId.value != null) {
            updateProgram(onSuccess)
        } else {
            createNewProgram(onSuccess)
        }
    }
    
    /**
     * Create new program
     */
    private fun createNewProgram(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                // Validate
                if (_programName.value.isNullOrBlank()) {
                    _errorMessage.value = "Program name is required"
                    return@launch
                }
                
                val template = _weeklyTemplate.value ?: emptyMap()
                if (template.isEmpty()) {
                    _errorMessage.value = "Weekly template is empty"
                    return@launch
                }
                
                // Convert to repository format
                val weeklyWorkouts = (1..7).map { dayNumber ->
                    val day = template[dayNumber] ?: DayWorkout(
                        dayNumber = dayNumber,
                        dayName = DayOfWeek.of(dayNumber).name,
                        workoutName = "Rest",
                        exercises = emptyList(),
                        isRestDay = true
                    )
                    
                    val exercises = day.exercises.map { ex ->
                        ExerciseDto(
                            id = ex.id,
                            name = ex.name,
                            sets = ex.sets,
                            reps = ex.reps,
                            weight = ex.weight,
                            restSeconds = ex.restSeconds,
                            notes = ex.notes
                        )
                    }
                    
                    Pair(day.workoutName, exercises)
                }
                
                // Create program
                val programId = repository.createProgram(
                    userId = userId,
                    title = _programName.value ?: "My Program",
                    description = "Custom ${_durationWeeks.value}-week program with ${_trainingDaysPerWeek.value} training days per week",
                    icon = "💪",
                    durationWeeks = _durationWeeks.value ?: 12,
                    daysPerWeek = _trainingDaysPerWeek.value ?: 4,
                    weeklyWorkouts = weeklyWorkouts
                )
                
                _successMessage.value = "Program created successfully! 🎉"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to create program: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    /**
     * Update existing program
     */
    private fun updateProgram(onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null
                
                val programId = _editProgramId.value
                if (programId == null) {
                    _errorMessage.value = "Program ID not found"
                    return@launch
                }
                
                // Validate
                if (_programName.value.isNullOrBlank()) {
                    _errorMessage.value = "Program name is required"
                    return@launch
                }
                
                val template = _weeklyTemplate.value ?: emptyMap()
                if (template.isEmpty()) {
                    _errorMessage.value = "Weekly template is empty"
                    return@launch
                }
                
                // Convert to repository format
                val weeklyWorkouts = (1..7).map { dayNumber ->
                    val day = template[dayNumber] ?: DayWorkout(
                        dayNumber = dayNumber,
                        dayName = DayOfWeek.of(dayNumber).name,
                        workoutName = "Rest",
                        exercises = emptyList(),
                        isRestDay = true
                    )
                    
                    val exercises = day.exercises.map { ex ->
                        ExerciseDto(
                            id = ex.id,
                            name = ex.name,
                            sets = ex.sets,
                            reps = ex.reps,
                            weight = ex.weight,
                            restSeconds = ex.restSeconds,
                            notes = ex.notes
                        )
                    }
                    
                    Pair(day.workoutName, exercises)
                }
                
                // Update program
                repository.updateProgram(
                    programId = programId,
                    title = _programName.value ?: "My Program",
                    description = "Custom ${_durationWeeks.value}-week program with ${_trainingDaysPerWeek.value} training days per week",
                    durationWeeks = _durationWeeks.value ?: 12,
                    daysPerWeek = _trainingDaysPerWeek.value ?: 4,
                    weeklyWorkouts = weeklyWorkouts
                )
                
                _successMessage.value = "Program updated successfully! 🎉"
                
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update program: ${e.message}"
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }
    
    // ========== Navigation ==========
    
    fun goBack() {
        when (_currentStep.value) {
            BuilderStep.WEEKLY_TEMPLATE -> _currentStep.value = BuilderStep.SETUP
            BuilderStep.PREVIEW -> _currentStep.value = BuilderStep.WEEKLY_TEMPLATE
            else -> {}
        }
    }
    
    fun clearError() {
        _errorMessage.value = null
    }
    
    fun clearSuccess() {
        _successMessage.value = null
    }
}

// ========== Data Classes ==========

/**
 * Builder steps
 */
enum class BuilderStep {
    SETUP,           // Step 1: Program details
    WEEKLY_TEMPLATE, // Step 2: Design week
    PREVIEW          // Step 3: Review (optional)
}

/**
 * Day workout in the weekly template
 */
data class DayWorkout(
    val dayNumber: Int,
    val dayName: String,
    val workoutName: String,
    val exercises: List<ExerciseBuilder>,
    val isRestDay: Boolean = false
) {
    val estimatedDuration: Int
        get() = if (isRestDay) 0 else exercises.size * 10 // ~10 min per exercise
}

/**
 * Exercise builder
 */
data class ExerciseBuilder(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val sets: Int = 3,
    val reps: Int = 10,
    val weight: Float? = null,
    val restSeconds: Int = 90,
    val notes: String? = null
)

