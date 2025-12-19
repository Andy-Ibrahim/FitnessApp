package com.example.classpass.ui.compose.drawer

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.data.entity.ChatSession
import com.example.classpass.ui.theme.ErrorRed
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.SheetBackground
import com.example.classpass.ui.theme.SheetCardBackground
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Chat history item displayed in the navigation drawer.
 * Shows chat title, timestamp, and highlights if active.
 */

// Chat history item constants
private val CHAT_ITEM_PADDING_HORIZONTAL = 8.dp
private val CHAT_ITEM_PADDING_VERTICAL = 12.dp
private val CHAT_TITLE_TEXT_SIZE = 15.sp
private val CHAT_PREVIEW_TEXT_SIZE = 13.sp
private val CHAT_TIME_TEXT_SIZE = 12.sp

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatHistoryItem(
    session: ChatSession,
    lastMessagePreview: String? = null,
    isActive: Boolean = false,
    onClick: () -> Unit,
    onStar: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    
    Column(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { showBottomSheet = true }
            )
            .padding(
                horizontal = CHAT_ITEM_PADDING_HORIZONTAL,
                vertical = CHAT_ITEM_PADDING_VERTICAL
            )
    ) {
        // Chat title and timestamp in a row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Title with optional star icon
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star icon (if starred)
                if (session.isStarred) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Starred",
                        tint = Color(0xFFFFD700), // Gold color
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                }
                
                // Chat title
                Text(
                    text = session.title,
                    fontSize = CHAT_TITLE_TEXT_SIZE,
                    color = if (isActive) PrimaryGreen else Color.White,
                    fontWeight = if (isActive) FontWeight.Medium else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            // Timestamp
            if (session.messageCount > 0) {
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = formatTimestamp(session.lastUpdated),
                    fontSize = CHAT_TIME_TEXT_SIZE,
                    color = Color.Gray,
                    maxLines = 1
                )
            }
        }
        
        // Message preview
        if (!lastMessagePreview.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastMessagePreview,
                fontSize = CHAT_PREVIEW_TEXT_SIZE,
                color = Color.Gray,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    
    // Bottom sheet for actions
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            containerColor = com.example.classpass.ui.theme.CardDark,
            dragHandle = { BottomSheetDragHandle() }
        ) {
            ChatActionsSheet(
                chatTitle = session.title,
                isStarred = session.isStarred,
                onStar = {
                    showBottomSheet = false
                    onStar()
                },
                onRename = {
                    showBottomSheet = false
                    onRename()
                },
                onDelete = {
                    showBottomSheet = false
                    onDelete()
                },
                onDismiss = { showBottomSheet = false }
            )
        }
    }
}

/**
 * Drag handle for bottom sheets.
 */
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
            color = Color.Gray
        ) {}
    }
}

/**
 * Bottom sheet content for chat actions (Star, Rename, Delete).
 */
@Composable
private fun ChatActionsSheet(
    chatTitle: String,
    isStarred: Boolean,
    onStar: () -> Unit,
    onRename: () -> Unit,
    onDelete: () -> Unit,
    onDismiss: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 40.dp)
    ) {
        // Chat title
        Text(
            text = chatTitle,
            fontSize = 16.sp,
            fontWeight = FontWeight.Medium,
            color = Color.White,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
        )
        
        // Star/Unstar action
        ChatActionItem(
            icon = if (isStarred) Icons.Default.Star else Icons.Outlined.StarOutline,
            label = if (isStarred) "Unstar" else "Star",
            iconTint = if (isStarred) Color(0xFFFFD700) else Color.White, // Gold when starred
            onClick = onStar
        )
        
        // Rename action
        ChatActionItem(
            icon = Icons.Default.Edit,
            label = "Rename",
            onClick = onRename
        )
        
        // Delete action
        ChatActionItem(
            icon = Icons.Default.Delete,
            label = "Delete",
            isDestructive = true,
            onClick = onDelete
        )
    }
}

/**
 * Individual action item in the bottom sheet.
 */
@Composable
private fun ChatActionItem(
    icon: ImageVector,
    label: String,
    isDestructive: Boolean = false,
    iconTint: Color? = null,
    onClick: () -> Unit
) {
    val textColor = when {
        isDestructive -> ErrorRed
        iconTint != null -> iconTint
        else -> Color.White
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = label,
            fontSize = 16.sp,
            color = textColor
        )
    }
}

/**
 * Format timestamp for display (e.g., "2 hours ago", "Yesterday", "Jan 15", "Jan 15, 2024")
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now" // Less than 1 minute
        diff < 3_600_000 -> "${diff / 60_000}m ago" // Less than 1 hour
        diff < 86_400_000 -> "${diff / 3_600_000}h ago" // Less than 24 hours
        diff < 172_800_000 -> "Yesterday" // Less than 48 hours
        diff < 604_800_000 -> "${diff / 86_400_000}d ago" // Less than 7 days
        else -> {
            // Check if same year
            val timestampDate = Date(timestamp)
            val currentDate = Date(now)
            val timestampYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(timestampDate)
            val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(currentDate)
            
            // Format with year if different year
            val dateFormat = if (timestampYear == currentYear) {
                SimpleDateFormat("MMM d", Locale.getDefault())
            } else {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            }
            dateFormat.format(timestampDate)
        }
    }
}

