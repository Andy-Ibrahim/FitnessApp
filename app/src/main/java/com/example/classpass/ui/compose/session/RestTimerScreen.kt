package com.example.classpass.ui.compose.session

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.TextSecondaryDark

@Composable
fun RestTimerScreen(
    timeRemaining: Int,
    isRunning: Boolean,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onSkip: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Rest Time",
            fontSize = 20.sp,
            fontWeight = FontWeight.SemiBold,
            color = TextSecondaryDark
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Big Countdown Timer
        Text(
            text = String.format("%02d:%02d", timeRemaining / 60, timeRemaining % 60),
            fontSize = 96.sp,
            fontWeight = FontWeight.Bold,
            color = if (timeRemaining <= 10) Color.Red else Color(0xFFFF9500) // Orange/Red
        )
        
        Spacer(modifier = Modifier.height(48.dp))
        
        // Control Buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pause/Play Button
            Button(
                onClick = { if (isRunning) onPause() else onStart() },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = CardDark
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(
                    if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Resume",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            // Skip Rest Button
            Button(
                onClick = onSkip,
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.SkipNext,
                        contentDescription = "Skip Rest",
                        tint = Color.Black,
                        modifier = Modifier.size(24.dp)
                    )
                    Text(
                        text = "Skip",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                }
            }
        }
    }
}

