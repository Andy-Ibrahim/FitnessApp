package com.example.classpass.ui.compose.program

import android.app.Application
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classpass.ClassPassApplication
import com.example.classpass.domain.model.ExerciseDto
import com.example.classpass.domain.model.WorkoutProgramDto
import com.example.classpass.domain.model.WorkoutSessionDto
import com.example.classpass.ui.compose.program.detail.ProgramHeader
import com.example.classpass.ui.compose.program.detail.WeekSelector
import com.example.classpass.ui.compose.program.detail.WorkoutSessionCard
import com.example.classpass.ui.compose.sheets.AddEditExerciseSheet
import com.example.classpass.ui.compose.sheets.DeleteProgramSheet
import com.example.classpass.ui.compose.sheets.ProgramOptionsSheet
import com.example.classpass.ui.compose.sheets.RestDaySheet
import com.example.classpass.ui.compose.sheets.WorkoutPreviewSheet
import com.example.classpass.ui.theme.*
import com.example.classpass.ui.viewmodel.ProgramDetailViewModel

/**
 * Program Detail Screen - Shows weekly workout schedule and progress
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramDetailScreen(
    programId: String,
    onNavigateBack: () -> Unit,
    onNavigateToWorkoutSession: (String, Int, Int) -> Unit = { _, _, _ -> },
    onNavigateToEditProgram: (String) -> Unit = {},
    onNavigateToSummary: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: ProgramDetailViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as ClassPassApplication
        )
    )
    
    // Load program data
    LaunchedEffect(programId) {
        viewModel.loadProgram(programId)
    }
    
    val program by viewModel.program.observeAsState()
    val currentWeekWorkouts by viewModel.currentWeekWorkouts.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()
    
    var showOptionsSheet by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }
    
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }
    
    // If deleting, show loading and don't render anything
    if (isDeleting) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = PrimaryGreen)
        }
        return
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (isLoading && program == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (program != null) {
            ProgramDetailContent(
                program = program!!,
                currentWeekWorkouts = currentWeekWorkouts,
                onNavigateBack = onNavigateBack,
                onWeekChange = { week -> viewModel.loadWeek(programId, week) },
                onStartWorkout = { weekNum, dayNum ->
                    onNavigateToWorkoutSession(programId, weekNum, dayNum)
                },
                onMoreOptionsClick = { showOptionsSheet = true },
                onNavigateToSummary = onNavigateToSummary,
                viewModel = viewModel
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Program not found", color = Color.White, fontSize = 18.sp)
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = onNavigateBack) {
                        Text("Go Back")
                    }
                }
            }
        }
    }
    
    // Program Options Sheet
    if (showOptionsSheet && program != null) {
        ProgramOptionsSheet(
            program = program!!,
            onDismiss = { showOptionsSheet = false },
            onEditClick = {
                showOptionsSheet = false
                onNavigateToEditProgram(programId)
            },
            onDeleteClick = {
                showOptionsSheet = false
                showDeleteDialog = true
            }
        )
    }
    
    // Delete Confirmation Sheet
    if (showDeleteDialog && program != null && !isDeleting) {
        val programTitle = program!!.title // Capture before deletion
        DeleteProgramSheet(
            programTitle = programTitle,
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                // Close sheet and set deleting flag
                showDeleteDialog = false
                isDeleting = true
                // Delete and navigate
                viewModel.deleteProgram(programId) {
                    onNavigateBack()
                }
            }
        )
    }
}

@Composable
private fun ProgramDetailContent(
    program: WorkoutProgramDto,
    currentWeekWorkouts: List<WorkoutSessionDto>,
    onNavigateBack: () -> Unit,
    onWeekChange: (Int) -> Unit,
    onStartWorkout: (Int, Int) -> Unit,
    onMoreOptionsClick: () -> Unit = {},
    onNavigateToSummary: (Long) -> Unit = {},
    viewModel: ProgramDetailViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    var selectedWeek by remember { mutableStateOf(program.currentWeek) }
    var showWorkoutPreview by remember { mutableStateOf(false) }
    var showRestDaySheet by remember { mutableStateOf(false) }
    var selectedWorkout by remember { mutableStateOf<WorkoutSessionDto?>(null) }
    var editingDayNumber by remember { mutableStateOf<Int?>(null) }
    var showAddEditExerciseSheet by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<Pair<ExerciseDto, Int>?>(null) }
    
    // Update selectedWorkout when currentWeekWorkouts changes
    LaunchedEffect(currentWeekWorkouts, selectedWorkout?.dayNumber) {
        selectedWorkout?.let { current ->
            val updated = currentWeekWorkouts.find { it.dayNumber == current.dayNumber }
            if (updated != null && updated != current) {
                selectedWorkout = updated
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Bar
        ProgramDetailTopBar(
            program = program,
            onNavigateBack = onNavigateBack,
            onMoreClick = onMoreOptionsClick
        )
        
        // Program Header
        ProgramHeader(program = program)
        
        // Week Selector
        WeekSelector(
            currentWeek = selectedWeek,
            totalWeeks = program.totalWeeks,
            onWeekChange = { week ->
                selectedWeek = week
                onWeekChange(week)
            }
        )
        
        // Weekly Workouts
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(currentWeekWorkouts) { workout ->
                WorkoutSessionCard(
                    workout = workout,
                    weekNumber = selectedWeek,
                    programId = program.id,
                    onCardClick = {
                        selectedWorkout = workout
                        showWorkoutPreview = true
                    },
                    onRestDayClick = {
                        selectedWorkout = workout
                        showRestDaySheet = true
                    },
                    onStartClick = { onStartWorkout(selectedWeek, workout.dayNumber) },
                    onViewSummary = onNavigateToSummary,
                    viewModel = viewModel
                )
            }
        }
    }
    
    // Workout Preview Sheet
    selectedWorkout?.let { workout ->
        if (showWorkoutPreview) {
            WorkoutPreviewSheet(
                workout = workout,
                onDismiss = { 
                    showWorkoutPreview = false
                    selectedWorkout = null
                },
                onStartWorkout = { 
                    onStartWorkout(selectedWeek, workout.dayNumber)
                },
                onEditExercise = { exercise, index ->
                    editingDayNumber = workout.dayNumber
                    editingExercise = Pair(exercise, index)
                    showAddEditExerciseSheet = true
                },
                onAddExercise = {
                    editingDayNumber = workout.dayNumber
                    editingExercise = null
                    showAddEditExerciseSheet = true
                },
                onDeleteExercise = { index ->
                    viewModel.deleteExercise(
                        programId = program.id,
                        weekNumber = selectedWeek,
                        dayNumber = workout.dayNumber,
                        exerciseIndex = index,
                        onSuccess = {
                            // Week is already reloaded by ViewModel
                        }
                    )
                }
            )
        }
    }
    
    // Add/Edit Exercise Sheet
    if (showAddEditExerciseSheet && editingDayNumber != null) {
        AddEditExerciseSheet(
            exercise = editingExercise?.first,
            onDismiss = {
                showAddEditExerciseSheet = false
                editingExercise = null
                editingDayNumber = null
            },
            onSave = { exerciseDto ->
                if (editingExercise != null) {
                    // Edit existing exercise
                    viewModel.updateExercise(
                        programId = program.id,
                        weekNumber = selectedWeek,
                        dayNumber = editingDayNumber!!,
                        exerciseIndex = editingExercise!!.second,
                        updatedExercise = exerciseDto,
                        onSuccess = {
                            showAddEditExerciseSheet = false
                            editingExercise = null
                            editingDayNumber = null
                            // Week is already reloaded by ViewModel
                        }
                    )
                } else {
                    // Add new exercise
                    viewModel.addExercise(
                        programId = program.id,
                        weekNumber = selectedWeek,
                        dayNumber = editingDayNumber!!,
                        exercise = exerciseDto,
                        onSuccess = {
                            showAddEditExerciseSheet = false
                            editingDayNumber = null
                            // Week is already reloaded by ViewModel
                        }
                    )
                }
            }
        )
    }
    
    // Rest Day Sheet
    selectedWorkout?.let { workout ->
        if (showRestDaySheet && workout.isRestDay) {
            var existingNote by remember { mutableStateOf("") }
            var existingFeeling by remember { mutableStateOf("") }
            var existingActivities by remember { mutableStateOf<List<String>>(emptyList()) }
            
            // Load existing rest day log
            LaunchedEffect(workout.dayNumber) {
                viewModel.loadRestDayLog(
                    programId = program.id,
                    weekNumber = selectedWeek,
                    dayNumber = workout.dayNumber
                ) { feeling, activities, note ->
                    existingFeeling = feeling
                    existingActivities = activities
                    existingNote = note
                }
            }
            
            RestDaySheet(
                dayName = workout.name,
                existingNote = existingNote,
                existingFeeling = existingFeeling,
                existingActivities = existingActivities,
                onDismiss = {
                    showRestDaySheet = false
                    selectedWorkout = null
                },
                onSave = { note, feeling, activities ->
                    viewModel.saveRestDayLog(
                        programId = program.id,
                        weekNumber = selectedWeek,
                        dayNumber = workout.dayNumber,
                        note = note,
                        feeling = feeling,
                        activities = activities
                    )
                }
            )
        }
    }
}

@Composable
private fun ProgramDetailTopBar(
    program: WorkoutProgramDto,
    onNavigateBack: () -> Unit,
    onMoreClick: () -> Unit = {}
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
            Text(
                program.title,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp)
            )
            IconButton(onClick = onMoreClick) {
                Icon(
                    Icons.Default.MoreVert,
                    contentDescription = "More options",
                    tint = Color.White
                )
            }
        }
    }
}

