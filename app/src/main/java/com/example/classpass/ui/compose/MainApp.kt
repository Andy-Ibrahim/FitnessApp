package com.example.classpass.ui.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.viewmodel.ChatHistoryViewModel

/**
 * Main app navigation - Perplexity-style conversational interface.
 * Simple navigation: Chat (main) → Workouts, Nutrition, Progress (feature screens)
 */

// Navigation routes
private const val ROUTE_CHAT = "chat"
private const val ROUTE_ALL_CHATS = "all_chats"
private const val ROUTE_WORKOUTS = "workouts"
private const val ROUTE_NUTRITION = "nutrition"
private const val ROUTE_PROGRESS = "progress"
private const val ROUTE_PROGRAM = "program"
private const val ROUTE_PROGRAM_WITH_ID = "program/{programId}"
private const val ROUTE_CREATE_PROGRAM = "create_program"
private const val ROUTE_EDIT_PROGRAM = "edit_program/{programId}"
private const val ROUTE_WORKOUT_SESSION = "workout_session/{programId}/{weekNumber}/{dayNumber}"
private const val ROUTE_WORKOUT_SUMMARY = "workout_summary/{historyId}"
private const val ARG_PROGRAM_ID = "programId"
private const val ARG_WEEK_NUMBER = "weekNumber"
private const val ARG_DAY_NUMBER = "dayNumber"
private const val ARG_HISTORY_ID = "historyId"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainApp() {
    val navController = rememberNavController()
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark) // Dark background for entire app
    ) {
        NavHost(
            navController = navController,
            startDestination = ROUTE_CHAT,
            modifier = Modifier.fillMaxSize()
        ) {
            // Main chat interface (home screen)
            composable(ROUTE_CHAT) {
                MainChatScreen(
                    onNavigateToWorkouts = { navController.navigate(ROUTE_WORKOUTS) },
                    onNavigateToNutrition = { navController.navigate(ROUTE_NUTRITION) },
                    onNavigateToProgress = { navController.navigate(ROUTE_PROGRESS) },
                    onNavigateToAllChats = { navController.navigate(ROUTE_ALL_CHATS) }
                )
            }
            
            // All chats screen
            composable(ROUTE_ALL_CHATS) {
                val chatHistoryViewModel: ChatHistoryViewModel = viewModel(
                    factory = ViewModelProvider.AndroidViewModelFactory.getInstance(
                        LocalContext.current.applicationContext as android.app.Application
                    )
                )
                
                AllChatsScreen(
                    onBackClick = { navController.popBackStack() },
                    onChatClick = { sessionId ->
                        // Switch to selected chat
                        chatHistoryViewModel.switchToChat(sessionId)
                        navController.popBackStack()
                    },
                    onNewChatClick = {
                        // Create new chat
                        chatHistoryViewModel.createNewChat()
                        navController.popBackStack()
                    }
                )
            }
            
            // Workouts tracking screen
            composable(ROUTE_WORKOUTS) {
                com.example.classpass.ui.compose.workouts.WorkoutsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToProgram = { programId ->
                        navController.navigate("$ROUTE_PROGRAM/$programId")
                    },
                    onNavigateToChat = {
                        navController.navigate(ROUTE_CHAT)
                    },
                    onNavigateToCreateOwn = {
                        navController.navigate(ROUTE_CREATE_PROGRAM)
                    },
                    onStartWorkout = { programId, weekNumber, dayNumber ->
                        navController.navigate("workout_session/$programId/$weekNumber/$dayNumber")
                    }
                )
            }
            
            // Program detail screen (workspace)
            composable(
                ROUTE_PROGRAM_WITH_ID,
                arguments = listOf(navArgument(ARG_PROGRAM_ID) { type = NavType.StringType })
            ) { backStackEntry ->
                val programId = backStackEntry.arguments?.getString(ARG_PROGRAM_ID) ?: ""
                com.example.classpass.ui.compose.program.ProgramDetailScreen(
                    programId = programId,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToWorkoutSession = { pid, week, day ->
                        navController.navigate("workout_session/$pid/$week/$day")
                    },
                    onNavigateToEditProgram = { pid ->
                        navController.navigate("edit_program/$pid")
                    },
                    onNavigateToSummary = { historyId ->
                        navController.navigate("workout_summary/$historyId")
                    }
                )
            }
            
            // Create your own program screen
            composable(ROUTE_CREATE_PROGRAM) {
                CreateProgramScreen(
                    onNavigateBack = { navController.popBackStack() },
                    isEditMode = false
                )
            }
            
            // Edit program screen
            composable(
                ROUTE_EDIT_PROGRAM,
                arguments = listOf(navArgument(ARG_PROGRAM_ID) { type = NavType.StringType })
            ) { backStackEntry ->
                val programId = backStackEntry.arguments?.getString(ARG_PROGRAM_ID) ?: ""
                CreateProgramScreen(
                    onNavigateBack = { navController.popBackStack() },
                    isEditMode = true,
                    programId = programId
                )
            }
            
            // Workout Session screen (exercise execution)
            composable(
                ROUTE_WORKOUT_SESSION,
                arguments = listOf(
                    navArgument(ARG_PROGRAM_ID) { type = NavType.StringType },
                    navArgument(ARG_WEEK_NUMBER) { type = NavType.IntType },
                    navArgument(ARG_DAY_NUMBER) { type = NavType.IntType }
                )
            ) { backStackEntry ->
                val programId = backStackEntry.arguments?.getString(ARG_PROGRAM_ID) ?: ""
                val weekNumber = backStackEntry.arguments?.getInt(ARG_WEEK_NUMBER) ?: 1
                val dayNumber = backStackEntry.arguments?.getInt(ARG_DAY_NUMBER) ?: 1
                com.example.classpass.ui.compose.session.WorkoutSessionScreen(
                    programId = programId,
                    weekNumber = weekNumber,
                    dayNumber = dayNumber,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSummary = { historyId ->
                        navController.navigate("workout_summary/$historyId") {
                            popUpTo("program/$programId") { inclusive = false }
                        }
                    }
                )
            }
            
            // Workout Summary screen (after workout completion)
            composable(
                ROUTE_WORKOUT_SUMMARY,
                arguments = listOf(navArgument(ARG_HISTORY_ID) { type = NavType.LongType })
            ) { backStackEntry ->
                val historyId = backStackEntry.arguments?.getLong(ARG_HISTORY_ID) ?: 0L
                WorkoutSummaryScreen(
                    historyId = historyId,
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Nutrition logging screen (TODO: Implement in future tasks)
            composable(ROUTE_NUTRITION) {
                NutritionScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            // Progress/stats screen (TODO: Implement in future tasks)
            composable(ROUTE_PROGRESS) {
                ProgressScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}
