package com.example.classpass.ui.compose

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classpass.ui.theme.*
import com.example.classpass.ui.viewmodel.WorkoutSummaryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * Workout Summary Screen - Shows individual workout completion stats
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutSummaryScreen(
    historyId: Long,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val viewModel: WorkoutSummaryViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as Application
        )
    )
    
    // Load workout history
    LaunchedEffect(historyId) {
        viewModel.loadWorkoutHistory(historyId)
    }
    
    val workoutHistory by viewModel.workoutHistory.observeAsState()
    val isLoading by viewModel.isLoading.observeAsState(false)
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Workout Complete!", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = BackgroundDark
                )
            )
        },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center),
                        color = PrimaryGreen
                    )
                }
                
                workoutHistory != null -> {
                    WorkoutSummaryContent(
                        workoutHistory = workoutHistory!!,
                        onDone = onNavigateBack
                    )
                }
                
                else -> {
                    Text(
                        "Workout not found",
                        color = Color.White,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
            }
        }
    }
}

@Composable
private fun WorkoutSummaryContent(
    workoutHistory: com.example.classpass.data.repository.WorkoutHistoryDto,
    onDone: () -> Unit
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
        contentPadding = PaddingValues(vertical = 24.dp)
    ) {
        // Celebration message
        item {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "🎉",
                    fontSize = 64.sp,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Great Work!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    workoutHistory.sessionName,
                    fontSize = 18.sp,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // Stats summary
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Duration stat
                StatCard(
                    icon = Icons.Default.Timer,
                    label = "Duration",
                    value = "${workoutHistory.durationMinutes} min",
                    modifier = Modifier.weight(1f)
                )
                
                // Exercises stat
                StatCard(
                    icon = Icons.Default.FitnessCenter,
                    label = "Exercises",
                    value = "${workoutHistory.exercises.size}",
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        // Date completed
        item {
            StatCard(
                icon = Icons.Default.CalendarToday,
                label = "Completed",
                value = formatDate(workoutHistory.completedDate),
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Exercises completed
        item {
            Text(
                "Exercises Completed",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(top = 8.dp)
            )
        }
        
        items(workoutHistory.exercises) { exercise ->
            ExerciseSummaryCard(exercise = exercise)
        }
        
        // Done button
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Button(
                onClick = onDone,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Done",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = CardDark,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                icon,
                contentDescription = label,
                tint = PrimaryGreen,
                modifier = Modifier.size(32.dp)
            )
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                label,
                fontSize = 13.sp,
                color = Color.Gray
            )
        }
    }
}

@Composable
private fun ExerciseSummaryCard(
    exercise: com.example.classpass.domain.model.ExerciseDto
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardDark,
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    exercise.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "${exercise.sets} × ${exercise.reps}${if (exercise.weight != null) " @ ${exercise.weight}kg" else ""}",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }
            Icon(
                Icons.Default.CheckCircle,
                contentDescription = "Completed",
                tint = PrimaryGreen,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

private fun formatDate(timestamp: Long): String {
    val sdf = SimpleDateFormat("MMM dd, yyyy 'at' h:mm a", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

