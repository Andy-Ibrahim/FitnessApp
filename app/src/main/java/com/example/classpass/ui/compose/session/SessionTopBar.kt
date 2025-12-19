package com.example.classpass.ui.compose.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.TextSecondaryDark

@Composable
fun SessionTopBar(
    sessionName: String,
    progress: Float,
    workoutTimeSeconds: Int,
    isTimerRunning: Boolean,
    onNavigateBack: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onCompleteWorkout: () -> Unit
) {
    val minutes = workoutTimeSeconds / 60
    val seconds = workoutTimeSeconds % 60
    val timeString = String.format("%02d:%02d", minutes, seconds)
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundDark
    ) {
        Column {
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
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        sessionName,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "${(progress * 100).toInt()}% Complete",
                        fontSize = 13.sp,
                        color = TextSecondaryDark
                    )
                }
                
                // Global Timer Display
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        timeString,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                    IconButton(
                        onClick = { if (isTimerRunning) onPauseTimer() else onResumeTimer() },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            if (isTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isTimerRunning) "Pause Timer" else "Resume Timer",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }
            
            // Progress Bar
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp),
                color = PrimaryGreen,
                trackColor = Color.Gray.copy(alpha = 0.2f)
            )
            
            // Finish Workout Button (under the progress line, top-left)
            Button(
                onClick = onCompleteWorkout,
                modifier = Modifier
                    .padding(start = 16.dp, top = 12.dp, bottom = 8.dp)
                    .widthIn(max = 140.dp)
                    .height(36.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Finish Workout",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
            }
        }
    }
}

