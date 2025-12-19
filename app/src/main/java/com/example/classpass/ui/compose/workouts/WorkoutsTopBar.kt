package com.example.classpass.ui.compose.workouts

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.classpass.ui.theme.BackgroundDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutsTopBar(
    onNavigateBack: () -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToCreateOwn: () -> Unit
) {
    TopAppBar(
        title = {
            Text(
                text = "Workouts",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        },
        navigationIcon = {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White
                )
            }
        },
        actions = {
            IconButton(onClick = onNavigateToCreateOwn) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "Create Your Own",
                    tint = Color.White
                )
            }
            IconButton(onClick = onNavigateToChat) {
                Icon(
                    Icons.Default.Chat,
                    contentDescription = "Chat",
                    tint = Color.White
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = BackgroundDark
        )
    )
}

