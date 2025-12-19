package com.example.classpass.ui.compose.sheets

import android.app.Application
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classpass.ui.theme.DrawerSecondaryText
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.SheetCardBackground
import com.example.classpass.ui.viewmodel.WatchConnectionViewModel

/**
 * Bottom sheet for connecting fitness watches.
 */

// Watch sheet constants
private val SHEET_PADDING_HORIZONTAL = 20.dp
private val SHEET_PADDING_VERTICAL = 24.dp
private val ITEM_SPACING = 12.dp
private val CORNER_RADIUS = 12.dp
private val WATCH_ICON_SIZE = 40.dp
private val WATCH_ICON_INNER_SIZE = 20.dp
private val BUTTON_HEIGHT = 36.dp
private val BUTTON_CORNER_RADIUS = 8.dp
private val PROGRESS_SIZE = 20.dp
private val PROGRESS_STROKE_WIDTH = 2.dp

// Text sizes
private val TITLE_TEXT_SIZE = 22.sp
private val SUBTITLE_TEXT_SIZE = 14.sp
private val ITEM_TEXT_SIZE = 16.sp
private val SYNC_TEXT_SIZE = 12.sp
private val BUTTON_TEXT_SIZE = 14.sp

/**
 * Map watch types to their corresponding icons.
 */
private fun getWatchIcon(type: WatchConnectionViewModel.WatchType): ImageVector = when (type) {
    WatchConnectionViewModel.WatchType.APPLE_WATCH -> Icons.Default.Watch
    WatchConnectionViewModel.WatchType.FITBIT -> Icons.Default.FitnessCenter
    WatchConnectionViewModel.WatchType.GARMIN -> Icons.Default.DirectionsRun
    WatchConnectionViewModel.WatchType.SAMSUNG -> Icons.Default.Watch
}

@Composable
fun WatchConnectionSheet(
    viewModel: WatchConnectionViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    // Observe ViewModel state
    val watches by viewModel.watches.observeAsState(emptyList())
    val isConnecting by viewModel.isConnecting.observeAsState(false)
    val connectingWatchId by viewModel.connectingWatchId.observeAsState()
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = SHEET_PADDING_HORIZONTAL,
                vertical = SHEET_PADDING_VERTICAL
            )
    ) {
        // Title
        Text(
            text = "Connect Fitness Watch",
            fontSize = TITLE_TEXT_SIZE,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        
        Text(
            text = "Sync your workouts and health data",
            fontSize = SUBTITLE_TEXT_SIZE,
            color = DrawerSecondaryText,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        
        // Watch options list (from ViewModel)
        watches.forEach { watch ->
            WatchConnectionItem(
                name = watch.name,
                icon = getWatchIcon(watch.type),
                isConnected = watch.isConnected,
                lastSync = watch.lastSync,
                isConnecting = isConnecting && connectingWatchId == watch.id,
                onConnect = { viewModel.connectWatch(watch.id) },
                onDisconnect = { viewModel.disconnectWatch(watch.id) }
            )
            
            Spacer(modifier = Modifier.height(ITEM_SPACING))
        }
        
        Spacer(modifier = Modifier.height(40.dp))
    }
}

/**
 * Individual watch connection item with icon, name, and connect/disconnect button.
 */
@Composable
private fun WatchConnectionItem(
    name: String,
    icon: ImageVector,
    isConnected: Boolean,
    lastSync: String?,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(CORNER_RADIUS),
        color = SheetCardBackground
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Watch info
            Row(
                horizontalArrangement = Arrangement.spacedBy(ITEM_SPACING),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                WatchIcon(
                    icon = icon,
                    isConnected = isConnected,
                    isConnecting = isConnecting
                )
                
                Column {
                    Text(
                        text = name,
                        fontSize = ITEM_TEXT_SIZE,
                        color = Color.White,
                        fontWeight = FontWeight.Medium
                    )
                    
                    if (isConnected && lastSync != null) {
                        Text(
                            text = "Last synced: $lastSync",
                            fontSize = SYNC_TEXT_SIZE,
                            color = DrawerSecondaryText
                        )
                    }
                }
            }
            
            // Connect/Disconnect button
            ConnectionButton(
                isConnected = isConnected,
                isConnecting = isConnecting,
                onConnect = onConnect,
                onDisconnect = onDisconnect
            )
        }
    }
}

/**
 * Watch icon with connection status indicator.
 */
@Composable
private fun WatchIcon(
    icon: ImageVector,
    isConnected: Boolean,
    isConnecting: Boolean
) {
    Surface(
        modifier = Modifier.size(WATCH_ICON_SIZE),
        shape = CircleShape,
        color = if (isConnected) PrimaryGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.1f)
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            if (isConnecting) {
                CircularProgressIndicator(
                    modifier = Modifier.size(PROGRESS_SIZE),
                    strokeWidth = PROGRESS_STROKE_WIDTH,
                    color = PrimaryGreen
                )
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = if (isConnected) PrimaryGreen else Color.White,
                    modifier = Modifier.size(WATCH_ICON_INNER_SIZE)
                )
            }
        }
    }
}

/**
 * Connection button with connect/disconnect states.
 */
@Composable
private fun ConnectionButton(
    isConnected: Boolean,
    isConnecting: Boolean,
    onConnect: () -> Unit,
    onDisconnect: () -> Unit
) {
    Button(
        onClick = if (isConnected) onDisconnect else onConnect,
        enabled = !isConnecting,
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isConnected) Color.Transparent else PrimaryGreen,
            contentColor = Color.White,
            disabledContainerColor = Color.Gray,
            disabledContentColor = Color.White
        ),
        shape = RoundedCornerShape(BUTTON_CORNER_RADIUS),
        modifier = Modifier.height(BUTTON_HEIGHT),
        border = if (isConnected) BorderStroke(1.dp, Color.White.copy(alpha = 0.3f)) else null
    ) {
        Text(
            text = when {
                isConnecting -> "..."
                isConnected -> "Disconnect"
                else -> "Connect"
            },
            fontSize = BUTTON_TEXT_SIZE
        )
    }
}
