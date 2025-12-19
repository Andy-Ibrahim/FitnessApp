package com.example.classpass.ui.compose

import android.app.Application
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classpass.ui.theme.*
import com.example.classpass.ui.viewmodel.BuilderStep
import com.example.classpass.ui.viewmodel.CreateProgramViewModel
import com.example.classpass.ui.viewmodel.DayWorkout
import com.example.classpass.ui.viewmodel.ExerciseBuilder
import java.time.DayOfWeek

/**
 * Main screen for creating/editing a custom workout program.
 * Uses multi-step flow: Setup -> Weekly Template -> Create
 * 
 * @param isEditMode If true, screen is in edit mode for existing program
 * @param programId The ID of the program to edit (required if isEditMode = true)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateProgramScreen(
    onNavigateBack: () -> Unit,
    isEditMode: Boolean = false,
    programId: String? = null,
    viewModel: CreateProgramViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    val currentStep by viewModel.currentStep.observeAsState(BuilderStep.SETUP)
    val errorMessage by viewModel.errorMessage.observeAsState()
    val successMessage by viewModel.successMessage.observeAsState()
    
    // Load existing program data when in edit mode
    LaunchedEffect(isEditMode, programId) {
        if (isEditMode && programId != null) {
            viewModel.loadProgramForEdit(programId)
        }
    }
    
    // Show success message and navigate back
    LaunchedEffect(successMessage) {
        if (successMessage != null) {
            kotlinx.coroutines.delay(1000)
            onNavigateBack()
        }
    }
    
    // Show error snackbar
    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = BackgroundDark
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    // Determine if going forward or backward
                    val isForward = targetState.ordinal > initialState.ordinal
                    
                    if (isForward) {
                        // Going forward: slide in from right, slide out to left
                        slideInHorizontally { it } togetherWith slideOutHorizontally { -it }
                    } else {
                        // Going backward: slide in from left, slide out to right
                        slideInHorizontally { -it } togetherWith slideOutHorizontally { it }
                    }
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    BuilderStep.SETUP -> ProgramSetupStep(
                        viewModel = viewModel,
                        isEditMode = isEditMode,
                        onBack = onNavigateBack,
                        onNext = { viewModel.proceedToWeeklyTemplate() }
                    )
                    BuilderStep.WEEKLY_TEMPLATE -> WeeklyTemplateStep(
                        viewModel = viewModel,
                        isEditMode = isEditMode,
                        onBack = { viewModel.goBack() },
                        onCreate = { viewModel.createProgram {} } // Empty callback, navigation handled by LaunchedEffect
                    )
                    BuilderStep.PREVIEW -> {} // Optional preview step
                }
            }
        }
    }
}

// ========== Step 1: Program Setup ==========

@Composable
private fun ProgramSetupStep(
    viewModel: CreateProgramViewModel,
    isEditMode: Boolean,
    onBack: () -> Unit,
    onNext: () -> Unit
) {
    val programName by viewModel.programName.observeAsState("")
    val durationWeeks by viewModel.durationWeeks.observeAsState(12)
    val trainingDaysPerWeek by viewModel.trainingDaysPerWeek.observeAsState(4)
    val selectedDays by viewModel.selectedDays.observeAsState(emptySet())
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BackgroundDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Text(
                    if (isEditMode) "Edit Program" else "Create Program",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(start = 8.dp)
                )
            }
        }
        
        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Program Name
            Column {
                Text(
                    "Program Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BasicTextField(
                    value = programName,
                    onValueChange = { viewModel.updateProgramName(it) },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        color = Color.White
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = CardDark
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                if (programName.isEmpty()) {
                                    Text(
                                        "e.g., 12-Week Strength Builder",
                                        fontSize = 16.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Duration
            Text(
                "Program Duration",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(4, 8, 12, 16, 24).forEach { weeks ->
                    DurationChip(
                        weeks = weeks,
                        isSelected = durationWeeks == weeks,
                        onClick = { viewModel.updateDurationWeeks(weeks) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Training Days Per Week
            Text(
                "Training Days Per Week",
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White,
                modifier = Modifier.padding(bottom = 10.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                (1..7).forEach { days ->
                    DaysChip(
                        days = days,
                        isSelected = trainingDaysPerWeek == days,
                        onClick = { viewModel.updateTrainingDaysPerWeek(days) },
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Which Days
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Select Training Days",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Text(
                    "$trainingDaysPerWeek selected",
                    fontSize = 13.sp,
                    color = TextSecondaryDark
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DayOfWeek.values().forEach { day ->
                    DaySelectionItem(
                        day = day,
                        isSelected = selectedDays.contains(day),
                        onClick = { viewModel.toggleDay(day) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Next Button
            Button(
                onClick = onNext,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Next: Design Week", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun DurationChip(
    weeks: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrimaryGreen.copy(alpha = 0.2f) else CardDark
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "$weeks wks",
                fontSize = 14.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) PrimaryGreen else Color.White
            )
        }
    }
}

@Composable
private fun DaysChip(
    days: Int,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(44.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(10.dp),
        color = if (isSelected) PrimaryGreen.copy(alpha = 0.2f) else CardDark
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                "$days",
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) PrimaryGreen else Color.White
            )
        }
    }
}

@Composable
private fun DaySelectionItem(
    day: DayOfWeek,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) PrimaryGreen.copy(alpha = 0.15f) else CardDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                day.name.lowercase().replaceFirstChar { it.uppercase() },
                fontSize = 16.sp,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
            if (isSelected) {
                Icon(
                    Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = PrimaryGreen,
                    modifier = Modifier.size(22.dp)
                )
            }
        }
    }
}

// ========== Step 2: Weekly Template ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun WeeklyTemplateStep(
    viewModel: CreateProgramViewModel,
    isEditMode: Boolean,
    onBack: () -> Unit,
    onCreate: () -> Unit
) {
    val weeklyTemplate by viewModel.weeklyTemplate.observeAsState(emptyMap())
    val programName by viewModel.programName.observeAsState("")
    val durationWeeks by viewModel.durationWeeks.observeAsState(12)
    val isLoading by viewModel.isLoading.observeAsState(false)
    
    var showEditDaySheet by remember { mutableStateOf(false) }
    var selectedDayNumber by remember { mutableStateOf<Int?>(null) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // Top bar
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BackgroundDark
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = Color.White)
                }
                Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                    Text(
                        "Design Your Week",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        "This template repeats for $durationWeeks weeks",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }
        }
        
        // Content
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(weeklyTemplate.toList().sortedBy { it.first }) { (dayNumber, dayWorkout) ->
                DayWorkoutCard(
                    dayWorkout = dayWorkout,
                    onClick = {
                        selectedDayNumber = dayNumber
                        showEditDaySheet = true
                    }
                )
            }
        }
        
        // Create Button
        Surface(
            modifier = Modifier.fillMaxWidth(),
            color = BackgroundDark,
            shadowElevation = 8.dp
        ) {
            Button(
                onClick = onCreate,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = !isLoading
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text(
                        if (isEditMode) "Save Changes" else "Create Program",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
    
    // Edit Day Sheet
    if (showEditDaySheet && selectedDayNumber != null) {
        val dayWorkout = weeklyTemplate[selectedDayNumber]
        if (dayWorkout != null) {
            EditDaySheet(
                dayWorkout = dayWorkout,
                onDismiss = { showEditDaySheet = false },
                onSave = { updatedWorkout ->
                    viewModel.updateDayWorkout(selectedDayNumber!!, updatedWorkout)
                    showEditDaySheet = false
                }
            )
        }
    }
}

@Composable
private fun DayWorkoutCard(
    dayWorkout: DayWorkout,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = CardDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        dayWorkout.dayName,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    if (dayWorkout.isRestDay) {
                        Text(
                            " • Rest",
                            fontSize = 15.sp,
                            color = TextSecondaryDark
                        )
                    }
                }
                if (!dayWorkout.isRestDay) {
                    Text(
                        dayWorkout.workoutName,
                        fontSize = 13.sp,
                        color = TextSecondaryDark,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                    Text(
                        "${dayWorkout.exercises.size} exercises • ~${dayWorkout.estimatedDuration} min",
                        fontSize = 12.sp,
                        color = TextSecondaryDark.copy(alpha = 0.7f),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Icon(
                Icons.Default.Edit,
                contentDescription = "Edit",
                tint = PrimaryGreen,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// ========== Edit Day Sheet ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditDaySheet(
    dayWorkout: DayWorkout,
    onDismiss: () -> Unit,
    onSave: (DayWorkout) -> Unit
) {
    var workoutName by remember { mutableStateOf(dayWorkout.workoutName) }
    var isRestDay by remember { mutableStateOf(dayWorkout.isRestDay) }
    var exercises by remember { mutableStateOf(dayWorkout.exercises) }
    var showAddExerciseSheet by remember { mutableStateOf(false) }
    var editingExercise by remember { mutableStateOf<ExerciseBuilder?>(null) }
    var showRestDayConfirmation by remember { mutableStateOf(false) }
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Always show fully expanded
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${dayWorkout.dayName} Workout",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Rest Day Toggle
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { 
                        if (!isRestDay && !dayWorkout.isRestDay) {
                            // Switching from workout to rest - show confirmation
                            showRestDayConfirmation = true
                        } else {
                            // Switching from rest to workout - allow directly
                            isRestDay = !isRestDay
                        }
                    },
                shape = RoundedCornerShape(12.dp),
                color = CardDark
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Mark as Rest Day", fontSize = 15.sp, color = Color.White)
                    Switch(
                        checked = isRestDay,
                        onCheckedChange = { 
                            if (!isRestDay && !dayWorkout.isRestDay) {
                                showRestDayConfirmation = true
                            } else {
                                isRestDay = it
                            }
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = Color.White,
                            checkedTrackColor = PrimaryGreen,
                            uncheckedThumbColor = Color.White,
                            uncheckedTrackColor = Color.Gray
                        )
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Workout Name
            Column {
                Text(
                    "Workout Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = if (isRestDay) Color.Gray else Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BasicTextField(
                    value = workoutName,
                    onValueChange = { workoutName = it },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isRestDay,
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        color = if (isRestDay) Color.Gray else Color.White
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = if (isRestDay) CardDark.copy(alpha = 0.5f) else CardDark
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                if (workoutName.isEmpty() && !isRestDay) {
                                    Text(
                                        "e.g., Upper Body Push",
                                        fontSize = 16.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
            
            if (!isRestDay) {
                Spacer(modifier = Modifier.height(20.dp))
                
                // Exercises Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Exercises",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                    Surface(
                        modifier = Modifier.clickable { showAddExerciseSheet = true },
                        shape = RoundedCornerShape(8.dp),
                        color = PrimaryGreen.copy(alpha = 0.15f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Icon(
                                Icons.Default.Add,
                                "Add",
                                tint = PrimaryGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                "Add Exercise",
                                color = PrimaryGreen,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Exercise List
                if (exercises.isEmpty()) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = CardDark.copy(alpha = 0.5f)
                    ) {
                        Box(
                            modifier = Modifier.padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No exercises added yet",
                                fontSize = 13.sp,
                                color = TextSecondaryDark,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    exercises.forEachIndexed { index, exercise ->
                        ExerciseCard(
                            exercise = exercise,
                            onEdit = {
                                editingExercise = exercise
                                showAddExerciseSheet = true
                            },
                            onDelete = {
                                exercises = exercises.filter { it.id != exercise.id }
                            }
                        )
                        if (index < exercises.size - 1) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save Button
            Button(
                onClick = {
                    onSave(
                        dayWorkout.copy(
                            workoutName = if (isRestDay) "Rest" else workoutName,
                            isRestDay = isRestDay,
                            exercises = if (isRestDay) emptyList() else exercises
                        )
                    )
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Save Day", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
    
    // Rest Day Confirmation Dialog
    if (showRestDayConfirmation) {
        AlertDialog(
            onDismissRequest = { showRestDayConfirmation = false },
            title = {
                Text("Mark as Rest Day?", fontWeight = FontWeight.Bold)
            },
            text = {
                Text("This will reduce your training days per week. You can change it back anytime.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        isRestDay = true
                        showRestDayConfirmation = false
                    }
                ) {
                    Text("Mark as Rest", color = PrimaryGreen)
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestDayConfirmation = false }) {
                    Text("Cancel", color = Color.Gray)
                }
            },
            containerColor = CardDark,
            titleContentColor = Color.White,
            textContentColor = Color.White
        )
    }
    
    // Add/Edit Exercise Sheet
    if (showAddExerciseSheet) {
        AddEditExerciseSheet(
            exercise = editingExercise,
            onDismiss = {
                showAddExerciseSheet = false
                editingExercise = null
            },
            onSave = { exercise ->
                if (editingExercise != null) {
                    // Update existing
                    exercises = exercises.map { if (it.id == exercise.id) exercise else it }
                } else {
                    // Add new
                    exercises = exercises + exercise
                }
                showAddExerciseSheet = false
                editingExercise = null
            }
        )
    }
}

@Composable
private fun ExerciseCard(
    exercise: ExerciseBuilder,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        color = BackgroundDark.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    exercise.name,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
                Row(
                    modifier = Modifier.padding(top = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        "${exercise.sets} × ${exercise.reps}",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                    if (exercise.weight != null) {
                        Text(
                            "•",
                            fontSize = 12.sp,
                            color = TextSecondaryDark.copy(alpha = 0.5f)
                        )
                        Text(
                            "${exercise.weight}kg",
                            fontSize = 12.sp,
                            color = TextSecondaryDark
                        )
                    }
                    Text(
                        "•",
                        fontSize = 12.sp,
                        color = TextSecondaryDark.copy(alpha = 0.5f)
                    )
                    Text(
                        "${exercise.restSeconds}s rest",
                        fontSize = 12.sp,
                        color = TextSecondaryDark
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onEdit, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Edit, "Edit", tint = PrimaryGreen, modifier = Modifier.size(18.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.Delete, "Delete", tint = ErrorRed, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

// ========== Add/Edit Exercise Sheet ==========

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddEditExerciseSheet(
    exercise: ExerciseBuilder?,
    onDismiss: () -> Unit,
    onSave: (ExerciseBuilder) -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var sets by remember { mutableStateOf(exercise?.sets ?: 3) }
    var reps by remember { mutableStateOf(exercise?.reps ?: 10) }
    var weight by remember { mutableStateOf(exercise?.weight?.toString() ?: "") }
    var restSeconds by remember { mutableStateOf(exercise?.restSeconds ?: 90) }
    var notes by remember { mutableStateOf(exercise?.notes ?: "") }
    
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Always show fully expanded
    )
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    if (exercise != null) "Edit Exercise" else "Add Exercise",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close", tint = Color.White)
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Exercise Name
            Column {
                Text(
                    "Exercise Name",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BasicTextField(
                    value = name,
                    onValueChange = { name = it },
                    modifier = Modifier.fillMaxWidth(),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 16.sp,
                        color = Color.White
                    ),
                    singleLine = true,
                    decorationBox = { innerTextField ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = CardDark
                        ) {
                            Box(
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                if (name.isEmpty()) {
                                    Text(
                                        "e.g., Bench Press",
                                        fontSize = 16.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Sets & Reps in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    NumberPicker(
                        label = "Sets",
                        value = sets,
                        onValueChange = { sets = it.coerceIn(1, 10) }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    NumberPicker(
                        label = "Reps",
                        value = reps,
                        onValueChange = { reps = it.coerceIn(1, 50) }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Weight & Rest in a row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Weight (kg)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    BasicTextField(
                        value = weight,
                        onValueChange = { weight = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = androidx.compose.ui.text.TextStyle(
                            fontSize = 16.sp,
                            color = Color.White
                        ),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        decorationBox = { innerTextField ->
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(10.dp),
                                color = CardDark
                            ) {
                                Box(
                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
                                ) {
                                    if (weight.isEmpty()) {
                                        Text(
                                            "Optional",
                                            fontSize = 16.sp,
                                            color = TextSecondaryDark
                                        )
                                    }
                                    innerTextField()
                                }
                            }
                        }
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    NumberPicker(
                        label = "Rest (sec)",
                        value = restSeconds,
                        onValueChange = { restSeconds = it.coerceIn(30, 300) },
                        step = 15
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Notes (optional)
            Column {
                Text(
                    "Notes (Optional)",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                BasicTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = 80.dp),
                    textStyle = androidx.compose.ui.text.TextStyle(
                        fontSize = 15.sp,
                        color = Color.White,
                        lineHeight = 20.sp
                    ),
                    maxLines = 3,
                    decorationBox = { innerTextField ->
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = CardDark
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp, vertical = 12.dp)
                            ) {
                                if (notes.isEmpty()) {
                                    Text(
                                        "e.g., Focus on form",
                                        fontSize = 15.sp,
                                        color = TextSecondaryDark
                                    )
                                }
                                innerTextField()
                            }
                        }
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        onSave(
                            ExerciseBuilder(
                                id = exercise?.id ?: java.util.UUID.randomUUID().toString(),
                                name = name,
                                sets = sets,
                                reps = reps,
                                weight = weight.toFloatOrNull(),
                                restSeconds = restSeconds,
                                notes = notes.ifBlank { null }
                            )
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = PrimaryGreen,
                    disabledContainerColor = PrimaryGreen.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank()
            ) {
                Text(
                    if (exercise != null) "Save Changes" else "Add Exercise",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun NumberPicker(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    step: Int = 1
) {
    Column {
        Text(
            label,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(10.dp),
            color = CardDark
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { onValueChange(value - step) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Remove,
                        "Decrease",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                
                Text(
                    value.toString(),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
                
                IconButton(
                    onClick = { onValueChange(value + step) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        Icons.Default.Add,
                        "Increase",
                        tint = PrimaryGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomSheetDragHandle() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(40.dp)
                .height(4.dp),
            shape = RoundedCornerShape(2.dp),
            color = Color.Gray.copy(alpha = 0.5f)
        ) {}
    }
}

