package com.example.classpass.ui.compose.workouts.programs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.WorkoutProgramDto
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.PrimaryGreen

@Composable
fun ProgramCard(
    program: WorkoutProgramDto,
    onClick: () -> Unit,
    onDeleteClick: ((WorkoutProgramDto) -> Unit)? = null,
    onRenameClick: ((WorkoutProgramDto) -> Unit)? = null
) {
    var showOptionsMenu by remember { mutableStateOf(false) }
    val totalSessions = program.totalWeeks * program.daysPerWeek
    val completedSessions = (program.completionPercentage * totalSessions).toInt()
    
    // Simple subtitle showing progress
    val subtitle = "Week ${program.currentWeek} of ${program.totalWeeks} • $completedSessions/$totalSessions workouts"
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = CardDark,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Program Icon
                Text(
                    text = program.icon,
                    fontSize = 32.sp
                )
                
                Column {
                    Text(
                        text = program.title,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 13.sp,
                        color = Color.Gray
                    )
                }
            }
            
            // Three-dot menu button
            Box {
                IconButton(
                    onClick = { showOptionsMenu = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = Color.White
                    )
                }
                
                DropdownMenu(
                    expanded = showOptionsMenu,
                    onDismissRequest = { showOptionsMenu = false },
                    modifier = Modifier.background(CardDark)
                ) {
                    onRenameClick?.let {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Edit,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Rename", color = Color.White)
                                }
                            },
                            onClick = {
                                showOptionsMenu = false
                                it(program)
                            }
                        )
                    }
                    
                    onDeleteClick?.let {
                        DropdownMenuItem(
                            text = {
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Delete,
                                        contentDescription = null,
                                        tint = Color.Red,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text("Delete", color = Color.Red)
                                }
                            },
                            onClick = {
                                showOptionsMenu = false
                                it(program)
                            }
                        )
                    }
                }
            }
        }
    }
}

