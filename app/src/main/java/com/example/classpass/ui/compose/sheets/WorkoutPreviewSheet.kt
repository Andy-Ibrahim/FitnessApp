package com.example.classpass.ui.compose.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.ExerciseDto
import com.example.classpass.domain.model.WorkoutSessionDto
import com.example.classpass.ui.theme.*
import com.example.classpass.ui.compose.common.BottomSheetDragHandle

/**
 * Bottom sheet to preview and edit a workout's exercises
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutPreviewSheet(
    workout: WorkoutSessionDto,
    onDismiss: () -> Unit,
    onStartWorkout: () -> Unit,
    onEditExercise: (ExerciseDto, Int) -> Unit = { _, _ -> },
    onAddExercise: () -> Unit = {},
    onDeleteExercise: (Int) -> Unit = {}
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardDark,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = workout.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${workout.exercises.size} exercises",
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "•",
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            text = "${workout.estimatedDuration} min",
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
                
                // Add exercise button
                IconButton(
                    onClick = onAddExercise,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        contentDescription = "Add Exercise",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Exercise list
            if (workout.exercises.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(48.dp)
                        )
                        Text(
                            "No exercises yet",
                            fontSize = 16.sp,
                            color = TextSecondaryDark
                        )
                        Text(
                            "Tap + to add exercises",
                            fontSize = 14.sp,
                            color = TextSecondaryDark.copy(alpha = 0.7f)
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 400.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    itemsIndexed(workout.exercises) { index, exercise ->
                        ExercisePreviewCard(
                            exercise = exercise,
                            exerciseNumber = index + 1,
                            onEdit = { onEditExercise(exercise, index) },
                            onDelete = { onDeleteExercise(index) }
                        )
                    }
                }
            }
            
            // Start Workout button (only show if there are exercises)
            if (workout.exercises.isNotEmpty()) {
                Spacer(modifier = Modifier.height(20.dp))
                
                Button(
                    onClick = {
                        onDismiss()
                        onStartWorkout()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        "Start Workout",
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
private fun ExercisePreviewCard(
    exercise: ExerciseDto,
    exerciseNumber: Int,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit),
        shape = RoundedCornerShape(12.dp),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Exercise number
            Surface(
                modifier = Modifier.size(36.dp),
                shape = RoundedCornerShape(8.dp),
                color = CardDark
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        "$exerciseNumber",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = PrimaryGreen
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // Exercise details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    exercise.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${exercise.sets} × ${exercise.reps}",
                        fontSize = 14.sp,
                        color = TextSecondaryDark
                    )
                    if (exercise.weight != null && exercise.weight > 0) {
                        Text("•", fontSize = 14.sp, color = TextSecondaryDark)
                        Text(
                            "${exercise.weight} kg",
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                    }
                    if (exercise.restSeconds > 0) {
                        Text("•", fontSize = 14.sp, color = TextSecondaryDark)
                        Text(
                            "${exercise.restSeconds}s rest",
                            fontSize = 14.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
            }
            
            // More options button
            Box {
                IconButton(
                    onClick = { showMenu = true },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "More options",
                        tint = TextSecondaryDark
                    )
                }
                
                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false },
                    modifier = Modifier.background(CardDark)
                ) {
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
                                Text("Edit", color = Color.White)
                            }
                        },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
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
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}

