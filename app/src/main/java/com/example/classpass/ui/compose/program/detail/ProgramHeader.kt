package com.example.classpass.ui.compose.program.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.WorkoutProgramDto
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.TextSecondaryDark

@Composable
fun ProgramHeader(program: WorkoutProgramDto) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardDark
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        program.description,
                        fontSize = 14.sp,
                        color = TextSecondaryDark,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        InfoChip(
                            icon = Icons.Default.CalendarToday,
                            text = "${program.totalWeeks} weeks"
                        )
                        InfoChip(
                            icon = Icons.Default.FitnessCenter,
                            text = "${program.daysPerWeek} days/week"
                        )
                    }
                }
                
                // Progress Circle
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(80.dp)
                ) {
                    CircularProgressIndicator(
                        progress = program.completionPercentage,
                        modifier = Modifier.fillMaxSize(),
                        color = PrimaryGreen,
                        strokeWidth = 6.dp,
                        trackColor = Color.Gray.copy(alpha = 0.2f)
                    )
                    Text(
                        "${(program.completionPercentage * 100).toInt()}%",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = PrimaryGreen,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text,
            fontSize = 13.sp,
            color = Color.White
        )
    }
}

