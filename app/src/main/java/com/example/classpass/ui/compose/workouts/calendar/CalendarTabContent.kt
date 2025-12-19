package com.example.classpass.ui.compose.workouts.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.WorkoutSessionDto
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.CardDark
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun CalendarTabContent(
    calendarWorkouts: Map<LocalDate, List<WorkoutSessionDto>>,
    todaysWorkout: com.example.classpass.domain.model.ScheduledWorkoutDto?,
    upcomingWorkouts: List<com.example.classpass.domain.model.ScheduledWorkoutDto>,
    onNavigateToProgram: (String) -> Unit,
    onStartWorkout: (String, Int, Int) -> Unit = { _, _, _ -> },
    allPrograms: List<com.example.classpass.domain.model.WorkoutProgramDto> = emptyList(),
    viewModel: com.example.classpass.ui.viewmodel.WorkoutsViewModelNew? = null
) {
    // Sheet state for workout preview
    var showWorkoutPreview by remember { mutableStateOf(false) }
    var selectedWorkout by remember { mutableStateOf<WorkoutSessionDto?>(null) }
    var selectedProgramId by remember { mutableStateOf<String?>(null) }
    var selectedWeekNumber by remember { mutableStateOf(1) }
    var selectedDayNumber by remember { mutableStateOf(1) }
    
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Today's Workout Section
        if (todaysWorkout != null) {
            item {
                Text(
                    text = "TODAY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }

            item {
                ScheduledWorkoutCard(
                    programName = todaysWorkout.programName,
                    workoutName = todaysWorkout.workout.name,
                    duration = "${todaysWorkout.workout.estimatedDuration} min",
                    exerciseCount = todaysWorkout.workout.exercises.size,
                    isCompleted = todaysWorkout.isCompleted,
                    onClick = {
                        selectedWorkout = todaysWorkout.workout
                        selectedProgramId = todaysWorkout.programId
                        selectedWeekNumber = todaysWorkout.weekNumber
                        selectedDayNumber = todaysWorkout.dayNumber
                        showWorkoutPreview = true
                    }
                )
            }
        } else {
            item {
                Text(
                    text = "TODAY",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray
                )
            }
            item {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = CardDark,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            "No workout scheduled for today",
                            fontSize = 16.sp,
                            color = Color.Gray,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            "Enjoy your rest day! 😊",
                            fontSize = 14.sp,
                            color = Color.Gray.copy(alpha = 0.7f),
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }

        // Show upcoming workouts
        if (upcomingWorkouts.isNotEmpty()) {
            item {
                Text(
                    text = "UPCOMING",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Gray,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(upcomingWorkouts) { scheduledWorkout ->
                val formatter = DateTimeFormatter.ofPattern("EEE MMM d", Locale.getDefault())
                
                UpcomingWorkoutItem(
                    date = scheduledWorkout.scheduledDate.format(formatter),
                    workoutName = scheduledWorkout.workout.name,
                    programName = scheduledWorkout.programName,
                    onClick = {
                        // Upcoming workouts are read-only - no click action
                    }
                )
            }
        } else {
            item {
                Text(
                    text = "No upcoming workouts scheduled",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp)
                )
            }
        }
    }
    
    // Workout Preview Sheet
    if (showWorkoutPreview && selectedWorkout != null && selectedProgramId != null) {
        // Capture values to prevent null pointer when button is clicked
        val workout = selectedWorkout!!
        val programId = selectedProgramId!!
        val week = selectedWeekNumber
        val day = selectedDayNumber
        
        com.example.classpass.ui.compose.sheets.WorkoutPreviewSheet(
            workout = workout,
            onDismiss = {
                showWorkoutPreview = false
                selectedWorkout = null
                selectedProgramId = null
            },
            onStartWorkout = {
                showWorkoutPreview = false
                onStartWorkout(programId, week, day)
            },
            onEditExercise = { exercise, index ->
                // Read-only from calendar
            },
            onAddExercise = {
                // Read-only from calendar
            },
            onDeleteExercise = { index ->
                // Read-only from calendar
            }
        )
    }
}

