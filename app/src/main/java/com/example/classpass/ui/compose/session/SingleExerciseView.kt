package com.example.classpass.ui.compose.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.ExerciseDto
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.TextSecondaryDark

@Composable
fun SingleExerciseView(
    exercise: ExerciseDto,
    isCompleted: Boolean,
    isLastExercise: Boolean,
    isWorkoutComplete: Boolean,
    onComplete: () -> Unit,
    onRestComplete: () -> Unit
) {
    var showRestScreen by remember { mutableStateOf(false) }
    var restTimeRemaining by remember { mutableStateOf(exercise.restSeconds) }
    var isRestTimerRunning by remember { mutableStateOf(false) }
    
    // Auto-start rest timer when exercise is marked complete
    LaunchedEffect(isCompleted) {
        if (isCompleted && !showRestScreen) {
            showRestScreen = true
            isRestTimerRunning = true
            restTimeRemaining = exercise.restSeconds
        }
    }
    
    // Rest timer countdown
    LaunchedEffect(isRestTimerRunning) {
        if (isRestTimerRunning && restTimeRemaining > 0) {
            while (restTimeRemaining > 0 && isRestTimerRunning) {
                kotlinx.coroutines.delay(1000)
                restTimeRemaining--
            }
            if (restTimeRemaining <= 0) {
                // Timer finished
                isRestTimerRunning = false
                showRestScreen = false
                // Trigger rest complete (will auto-show completion dialog if last exercise)
                onRestComplete()
            }
        }
    }
    
    if (showRestScreen) {
        // Full-screen Rest Timer
        RestTimerScreen(
            timeRemaining = restTimeRemaining,
            isRunning = isRestTimerRunning,
            onStart = { isRestTimerRunning = true },
            onPause = { isRestTimerRunning = false },
            onSkip = {
                isRestTimerRunning = false
                showRestScreen = false
                onRestComplete() // Auto-advance to next exercise
            }
        )
    } else {
        // Exercise Details View
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Exercise Name
            Text(
                text = exercise.name,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Exercise Details Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                ExerciseDetailCard(
                    icon = Icons.Default.FitnessCenter,
                    label = "Sets × Reps",
                    value = "${exercise.sets} × ${exercise.reps}",
                    modifier = Modifier.weight(1f)
                )
                if (exercise.weight != null) {
                    ExerciseDetailCard(
                        icon = Icons.Default.MonitorWeight,
                        label = "Weight",
                        value = "${exercise.weight}kg",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            ExerciseDetailCard(
                icon = Icons.Default.Timer,
                label = "Rest Time",
                value = "${exercise.restSeconds}s",
                modifier = Modifier.fillMaxWidth()
            )
            
            // Notes (if any)
            if (!exercise.notes.isNullOrBlank()) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    color = CardDark
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Notes",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondaryDark
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = exercise.notes,
                            fontSize = 15.sp,
                            color = Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Complete Exercise Button (only show if not completed)
            if (!isCompleted) {
                Button(
                    onClick = onComplete,
                    modifier = Modifier
                        .widthIn(max = 300.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Complete Exercise",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun ExerciseDetailCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = CardDark
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon,
                contentDescription = null,
                tint = PrimaryGreen,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 12.sp,
                color = TextSecondaryDark
            )
        }
    }
}

