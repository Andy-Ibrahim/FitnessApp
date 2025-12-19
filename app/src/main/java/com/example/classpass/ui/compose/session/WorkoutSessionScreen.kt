package com.example.classpass.ui.compose.session

import android.app.Application
import androidx.compose.foundation.layout.*
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
import com.example.classpass.domain.model.WorkoutSessionDto
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.TextSecondaryDark
import com.example.classpass.ui.viewmodel.WorkoutSessionViewModel

/**
 * Workout Session Screen - Exercise execution and tracking
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSessionScreen(
    programId: String,
    weekNumber: Int,
    dayNumber: Int,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (Long) -> Unit = {}
) {
    val context = LocalContext.current
    val viewModel: WorkoutSessionViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    
    // Load session data
    LaunchedEffect(programId, weekNumber, dayNumber) {
        viewModel.loadSession(programId, weekNumber, dayNumber)
    }
    
    val session by viewModel.session.observeAsState()
    val currentExerciseIndex by viewModel.currentExerciseIndex.observeAsState(0)
    val completedExercises by viewModel.completedExercises.observeAsState(emptySet())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val isWorkoutComplete by viewModel.isWorkoutComplete.observeAsState(false)
    val workoutTimeSeconds by viewModel.workoutTimeSeconds.observeAsState(0)
    val isTimerRunning by viewModel.isTimerRunning.observeAsState(false)
    
    val snackbarHostState = remember { SnackbarHostState() }
    var showCompleteDialog by remember { mutableStateOf(false) }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { paddingValues ->
        if (isLoading && session == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryGreen)
            }
        } else if (session != null) {
            WorkoutSessionContent(
                session = session!!,
                currentExerciseIndex = currentExerciseIndex,
                completedExercises = completedExercises,
                isWorkoutComplete = isWorkoutComplete,
                workoutTimeSeconds = workoutTimeSeconds,
                isTimerRunning = isTimerRunning,
                onNavigateBack = onNavigateBack,
                onExerciseComplete = { exerciseId -> viewModel.toggleExerciseComplete(exerciseId) },
                onNextExercise = { 
                    // Check if this is the last exercise
                    val isLastExercise = currentExerciseIndex >= session!!.exercises.size - 1
                    if (isLastExercise && isWorkoutComplete) {
                        // Auto-show completion dialog after last exercise rest
                        showCompleteDialog = true
                    } else {
                        viewModel.nextExercise()
                    }
                },
                onPreviousExercise = { viewModel.previousExercise() },
                onCompleteWorkout = { showCompleteDialog = true },
                onPauseTimer = { viewModel.pauseTimer() },
                onResumeTimer = { viewModel.resumeTimer() }
            )
        }
    }
    
    // Complete Workout Dialog
    if (showCompleteDialog) {
        AlertDialog(
            onDismissRequest = { showCompleteDialog = false },
            title = { Text("Complete Workout?", color = Color.White) },
            text = {
                Text(
                    "Great job! Mark this workout as complete?",
                    color = TextSecondaryDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.completeWorkout(programId, weekNumber, dayNumber) { historyId ->
                            showCompleteDialog = false
                            onNavigateToSummary(historyId)
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen)
                ) {
                    Text("Complete Workout")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCompleteDialog = false }) {
                    Text("Cancel", color = Color.White)
                }
            },
            containerColor = CardDark
        )
    }
}

@Composable
private fun WorkoutSessionContent(
    session: WorkoutSessionDto,
    currentExerciseIndex: Int,
    completedExercises: Set<String>,
    isWorkoutComplete: Boolean,
    workoutTimeSeconds: Int,
    isTimerRunning: Boolean,
    onNavigateBack: () -> Unit,
    onExerciseComplete: (String) -> Unit,
    onNextExercise: () -> Unit,
    onPreviousExercise: () -> Unit,
    onCompleteWorkout: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit
) {
    val currentExercise = session.exercises.getOrNull(currentExerciseIndex)
    val completionProgress = completedExercises.size.toFloat() / session.exercises.size.toFloat()
    val isLastExercise = currentExerciseIndex >= session.exercises.size - 1
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top Bar with Timer and Progress
        SessionTopBar(
            sessionName = session.name,
            progress = completionProgress,
            workoutTimeSeconds = workoutTimeSeconds,
            isTimerRunning = isTimerRunning,
            onNavigateBack = onNavigateBack,
            onPauseTimer = onPauseTimer,
            onResumeTimer = onResumeTimer,
            onCompleteWorkout = onCompleteWorkout
        )
        
        if (currentExercise != null) {
            // Main Exercise Content
            Box(modifier = Modifier.weight(1f)) {
                SingleExerciseView(
                    exercise = currentExercise,
                    isCompleted = completedExercises.contains(currentExercise.id),
                    isLastExercise = isLastExercise,
                    isWorkoutComplete = isWorkoutComplete,
                    onComplete = { onExerciseComplete(currentExercise.id) },
                    onRestComplete = onNextExercise
                )
            }
            
            // Bottom Navigation Bar
            Surface(
                modifier = Modifier.fillMaxWidth(),
                color = CardDark,
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Previous Button
                    TextButton(
                        onClick = onPreviousExercise,
                        enabled = currentExerciseIndex > 0
                    ) {
                        Text(
                            "← Prev",
                            color = if (currentExerciseIndex > 0) PrimaryGreen else Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    // Centered Exercise Counter
                    Text(
                        "Exercise ${currentExerciseIndex + 1} of ${session.exercises.size}",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                    
                    // Next Button
                    TextButton(
                        onClick = onNextExercise,
                        enabled = currentExerciseIndex < session.exercises.size - 1 && completedExercises.contains(currentExercise.id)
                    ) {
                        Text(
                            "Next →",
                            color = if (currentExerciseIndex < session.exercises.size - 1 && completedExercises.contains(currentExercise.id)) 
                                PrimaryGreen else Color.Gray,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

