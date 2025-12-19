package com.example.classpass.ui.compose.topbar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Watch
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.StatusConnected
import com.example.classpass.ui.theme.StatusDisconnected

/**
 * Top bar for the main chat screen.
 * Contains menu button and watch connection button with status indicator.
 */

// Top bar constants
private val TOP_BAR_PADDING_HORIZONTAL = 16.dp
private val TOP_BAR_PADDING_VERTICAL = 12.dp
private val WATCH_BUTTON_SIZE = 40.dp
private val WATCH_ICON_SIZE = 24.dp
private val STATUS_INDICATOR_SIZE = 10.dp
private val STATUS_INDICATOR_OFFSET_X = (-2).dp
private val STATUS_INDICATOR_OFFSET_Y = 2.dp
private val TRAINER_BUTTON_SIZE = 48.dp
private val TRAINER_ICON_SIZE = 24.dp

@Composable
fun MainChatTopBar(
    onMenuClick: () -> Unit,
    onWatchClick: () -> Unit,
    onTrainerModeClick: () -> Unit, // New callback for Trainer Mode
    isWatchConnected: Boolean = false // TODO: Connect to WatchConnectionViewModel in Task 5
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(
                    horizontal = TOP_BAR_PADDING_HORIZONTAL,
                    vertical = TOP_BAR_PADDING_VERTICAL
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Menu button (top-left)
            IconButton(onClick = onMenuClick) {
                Icon(
                    Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = Color.White
                )
            }
            
            // Trainer Mode button (center)
            TrainerModeButton(onClick = onTrainerModeClick)
            
            // Watch connection button with status indicator (top-right)
            WatchConnectionButton(
                isConnected = isWatchConnected,
                onClick = onWatchClick
            )
        }
    }
}

/**
 * Watch connection button with dynamic status indicator.
 */
@Composable
private fun WatchConnectionButton(
    isConnected: Boolean,
    onClick: () -> Unit
) {
    Box {
        IconButton(
            onClick = onClick,
            modifier = Modifier.size(WATCH_BUTTON_SIZE)
        ) {
            Icon(
                Icons.Default.Watch,
                contentDescription = "Connect fitness watch",
                tint = Color.White,
                modifier = Modifier.size(WATCH_ICON_SIZE)
            )
        }
        
        // Connection status indicator (green when connected, gray when disconnected)
        StatusIndicator(
            isConnected = isConnected,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .offset(x = STATUS_INDICATOR_OFFSET_X, y = STATUS_INDICATOR_OFFSET_Y)
        )
    }
}

/**
 * Status indicator dot (green = connected, gray = disconnected).
 */
@Composable
private fun StatusIndicator(
    isConnected: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.size(STATUS_INDICATOR_SIZE),
        shape = CircleShape,
        color = if (isConnected) StatusConnected else StatusDisconnected
    ) {}
}

/**
 * Trainer Mode button with purple gradient background and sparkle icon.
 * Inspired by modern AI assistant UI design.
 */
@Composable
private fun TrainerModeButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(TRAINER_BUTTON_SIZE)
            .background(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color(0xFF8B5CF6), // Purple center
                        Color(0xFF6366F1), // Blue-purple
                        Color(0xFF4F46E5)  // Darker blue-purple edge
                    )
                ),
                shape = CircleShape
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.AutoAwesome,
            contentDescription = "Trainer Mode",
            tint = Color.White,
            modifier = Modifier.size(TRAINER_ICON_SIZE)
        )
    }
}
