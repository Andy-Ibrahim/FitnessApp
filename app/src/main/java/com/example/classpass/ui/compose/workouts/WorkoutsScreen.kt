package com.example.classpass.ui.compose.workouts

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classpass.ClassPassApplication
import com.example.classpass.ui.compose.workouts.calendar.CalendarTabContent
import com.example.classpass.ui.compose.workouts.programs.ProgramsTabContent
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.viewmodel.WorkoutsViewModelNew

/**
 * Main Workouts screen with 2 tabs: Calendar, Programs
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToProgram: (String) -> Unit = {},
    onNavigateToChat: () -> Unit = {},
    onNavigateToCreateOwn: () -> Unit = {},
    onStartWorkout: (String, Int, Int) -> Unit = { _, _, _ -> }
) {
    val context = LocalContext.current
    val viewModel: WorkoutsViewModelNew = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            context.applicationContext as ClassPassApplication
        )
    )
    
    var selectedTab by remember { mutableStateOf(1) } // Default to Programs tab
    val tabs = listOf("Calendar", "Programs")
    
    // Observe data from ViewModel
    val allPrograms by viewModel.allPrograms.observeAsState(emptyList())
    val activeProgram by viewModel.activeProgram.observeAsState(null)
    val todaysWorkout by viewModel.todaysWorkout.observeAsState(null) // ScheduledWorkoutDto?
    val upcomingWorkouts by viewModel.upcomingWorkouts.observeAsState(emptyList()) // List<ScheduledWorkoutDto>
    val calendarWorkouts by viewModel.calendarWorkouts.observeAsState(emptyMap())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorMessage by viewModel.errorMessage.observeAsState(null)
    val successMessage by viewModel.successMessage.observeAsState(null)
    
    // Show snackbar for messages
    val snackbarHostState = remember { SnackbarHostState() }
    
    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }
    
    LaunchedEffect(successMessage) {
        successMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearSuccess()
        }
    }
    
    // Reload programs when screen becomes visible (e.g., after creating a new program)
    LaunchedEffect(Unit) {
        viewModel.loadPrograms()
    }

    Scaffold(
        topBar = {
            WorkoutsTopBar(
                onNavigateBack = onNavigateBack,
                onNavigateToChat = onNavigateToChat,
                onNavigateToCreateOwn = onNavigateToCreateOwn
            )
        },
        containerColor = BackgroundDark,
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Tab Row
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = BackgroundDark,
                    contentColor = Color.White
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTab == index) Color.White else Color.Gray
                                )
                            }
                        )
                    }
                }

                // Tab Content
                when (selectedTab) {
                    0 -> CalendarTabContent(
                        calendarWorkouts = calendarWorkouts,
                        todaysWorkout = todaysWorkout,
                        upcomingWorkouts = upcomingWorkouts,
                        onNavigateToProgram = onNavigateToProgram,
                        onStartWorkout = onStartWorkout,
                        allPrograms = allPrograms,
                        viewModel = viewModel
                    )
                    1 -> ProgramsTabContent(
                        programs = allPrograms,
                        activeProgram = activeProgram,
                        onNavigateToProgram = onNavigateToProgram,
                        onDeleteProgram = { program -> viewModel.deleteProgram(program) },
                        onRenameProgram = { program, newName -> viewModel.renameProgram(program, newName) },
                        onNavigateToChat = onNavigateToChat,
                        onNavigateToCreateOwn = onNavigateToCreateOwn
                    )
                }
            }
            
            // Loading indicator
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = PrimaryGreen
                )
            }
        }
    }
}

