package com.example.classpass.ui.compose.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.ui.theme.PrimaryGreen

/**
 * Empty state shown when there are no chat messages.
 * Displays a welcoming message to start the conversation.
 */
@Composable
fun EmptyChatState() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo/Icon - larger and more prominent
        Surface(
            modifier = Modifier.size(100.dp),
            shape = CircleShape,
            color = PrimaryGreen.copy(alpha = 0.15f)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    Icons.Default.FitnessCenter,
                    contentDescription = null,
                    modifier = Modifier.size(50.dp),
                    tint = PrimaryGreen
                )
            }
        }
        
        Spacer(modifier = Modifier.height(40.dp))
        
        // Main greeting text
        Text(
            text = "What's your fitness goal today?",
            fontSize = 28.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp
        )
    }
}

