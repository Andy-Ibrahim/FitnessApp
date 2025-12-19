package com.example.classpass.ui.compose.program.detail

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.TextSecondaryDark

@Composable
fun WeekSelector(
    currentWeek: Int,
    totalWeeks: Int,
    onWeekChange: (Int) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { if (currentWeek > 1) onWeekChange(currentWeek - 1) },
                enabled = currentWeek > 1
            ) {
                Icon(
                    Icons.Default.ChevronLeft,
                    contentDescription = "Previous week",
                    tint = if (currentWeek > 1) Color.White else Color.Gray
                )
            }
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "Week $currentWeek",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    "of $totalWeeks",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
            }
            
            IconButton(
                onClick = { if (currentWeek < totalWeeks) onWeekChange(currentWeek + 1) },
                enabled = currentWeek < totalWeeks
            ) {
                Icon(
                    Icons.Default.ChevronRight,
                    contentDescription = "Next week",
                    tint = if (currentWeek < totalWeeks) Color.White else Color.Gray
                )
            }
        }
    }
}

