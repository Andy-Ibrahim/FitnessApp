package com.example.classpass.ui.compose.program.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.WorkoutSessionDto
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.TextSecondaryDark
import com.example.classpass.ui.viewmodel.ProgramDetailViewModel

@Composable
fun WorkoutSessionCard(
    workout: WorkoutSessionDto,
    weekNumber: Int,
    programId: String,
    onCardClick: () -> Unit = {},
    onRestDayClick: () -> Unit = {},
    onStartClick: () -> Unit = {},
    onViewSummary: (Long) -> Unit = {},
    viewModel: ProgramDetailViewModel
) {
    val sessionId = "$weekNumber-${workout.dayNumber}"
    var historyId by remember { mutableStateOf<Long?>(null) }
    
    // Load history ID for completed workouts
    LaunchedEffect(workout.isCompleted, sessionId) {
        if (workout.isCompleted) {
            viewModel.getHistoryIdForSession(programId, sessionId) { id ->
                historyId = id
            }
        }
    }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = !workout.isCompleted) { 
                if (workout.isRestDay) {
                    onRestDayClick()
                } else {
                    onCardClick()
                }
            },
        shape = RoundedCornerShape(12.dp),
        color = CardDark
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Day indicator
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = if (workout.isCompleted) PrimaryGreen.copy(alpha = 0.2f)
                       else if (workout.isRestDay) Color.Gray.copy(alpha = 0.2f)
                       else BackgroundDark
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (workout.isCompleted) {
                        Icon(
                            Icons.Default.CheckCircle,
                            contentDescription = "Completed",
                            tint = PrimaryGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            getDayAbbreviation(workout.dayNumber),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (workout.isRestDay) Color.Gray else Color.White
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Workout info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    workout.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                if (!workout.isRestDay) {
                    Text(
                        "${workout.exercises.size} exercises • ${workout.exercises.size} min",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                } else {
                    Text(
                        "Rest & Recovery",
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
            
            // Action button
            if (!workout.isRestDay) {
                if (workout.isCompleted && historyId != null) {
                    // Show Summary button for completed workouts
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF4A4A4A),
                        modifier = Modifier.clickable { onViewSummary(historyId!!) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Assessment,
                                contentDescription = "View Summary",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Summary",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White
                            )
                        }
                    }
                } else if (!workout.isCompleted) {
                    // Show Add button if no exercises, Start button if has exercises
                    if (workout.exercises.isEmpty()) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryGreen,
                            modifier = Modifier.clickable(onClick = onCardClick)
                        ) {
                            Text(
                                "Add",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    } else {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = PrimaryGreen,
                            modifier = Modifier.clickable(onClick = onStartClick)
                        ) {
                            Text(
                                "Start",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun getDayAbbreviation(dayNumber: Int): String {
    return when (dayNumber) {
        1 -> "Mon"
        2 -> "Tue"
        3 -> "Wed"
        4 -> "Thu"
        5 -> "Fri"
        6 -> "Sat"
        7 -> "Sun"
        else -> ""
    }
}

