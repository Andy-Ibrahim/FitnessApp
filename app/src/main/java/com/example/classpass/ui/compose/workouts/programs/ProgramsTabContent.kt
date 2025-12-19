package com.example.classpass.ui.compose.workouts.programs

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.WorkoutProgramDto
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.PrimaryGreen

@Composable
fun ProgramsTabContent(
    programs: List<WorkoutProgramDto>,
    activeProgram: WorkoutProgramDto?,
    onNavigateToProgram: (String) -> Unit,
    onDeleteProgram: (WorkoutProgramDto) -> Unit,
    onRenameProgram: (WorkoutProgramDto, String) -> Unit,
    onNavigateToChat: () -> Unit = {},
    onNavigateToCreateOwn: () -> Unit = {}
) {
    var showDeleteSheet by remember { mutableStateOf(false) }
    var showRenameSheet by remember { mutableStateOf(false) }
    var selectedProgram by remember { mutableStateOf<WorkoutProgramDto?>(null) }
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "YOUR PROGRAMS (${programs.size})",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.Gray
            )
        }

        if (programs.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "No programs yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Get started by creating your first workout program",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    // Ask AI Button
                    Button(
                        onClick = onNavigateToChat,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = PrimaryGreen
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Ask AI to Create",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Create Your Own Button
                    OutlinedButton(
                        onClick = onNavigateToCreateOwn,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = Color.White
                        ),
                        border = BorderStroke(1.dp, Color.Gray),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Create Your Own",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        } else {
            // Active program first (if exists)
            activeProgram?.let { program ->
                item {
                    ProgramCard(
                        program = program,
                        onClick = { onNavigateToProgram(program.id) },
                        onDeleteClick = {
                            selectedProgram = it
                            showDeleteSheet = true
                        },
                        onRenameClick = {
                            selectedProgram = it
                            showRenameSheet = true
                        }
                    )
                }
            }
            
            // Other programs
            items(programs.filter { it.id != activeProgram?.id }) { program ->
                ProgramCard(
                    program = program,
                    onClick = { onNavigateToProgram(program.id) },
                    onDeleteClick = {
                        selectedProgram = it
                        showDeleteSheet = true
                    },
                    onRenameClick = {
                        selectedProgram = it
                        showRenameSheet = true
                    }
                )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "💬 Need a new program? Ask AI",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
    
    // Delete Confirmation Sheet
    if (showDeleteSheet && selectedProgram != null) {
        com.example.classpass.ui.compose.sheets.DeleteProgramSheet(
            programTitle = selectedProgram!!.title,
            onDismiss = {
                showDeleteSheet = false
                selectedProgram = null
            },
            onConfirm = {
                onDeleteProgram(selectedProgram!!)
                showDeleteSheet = false
                selectedProgram = null
            }
        )
    }
    
    // Rename Sheet
    if (showRenameSheet && selectedProgram != null) {
        RenameProgramSheet(
            currentName = selectedProgram!!.title,
            onDismiss = {
                showRenameSheet = false
                selectedProgram = null
            },
            onConfirm = { newName ->
                onRenameProgram(selectedProgram!!, newName)
                showRenameSheet = false
                selectedProgram = null
            }
        )
    }
}

