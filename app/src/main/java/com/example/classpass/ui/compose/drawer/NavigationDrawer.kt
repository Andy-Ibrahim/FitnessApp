package com.example.classpass.ui.compose.drawer

import android.app.Application
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classpass.data.entity.ChatSession
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.DrawerDivider
import com.example.classpass.ui.theme.DrawerSecondaryText
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.viewmodel.ChatHistoryViewModel

/**
 * Navigation drawer content with chat history and navigation options.
 */

// Drawer layout constants
private val DRAWER_WIDTH = 300.dp
private val DRAWER_PADDING_HORIZONTAL = 20.dp
private val DRAWER_PADDING_VERTICAL = 16.dp
private val DRAWER_ITEM_PADDING_VERTICAL = 12.dp
private val DRAWER_ICON_SIZE = 20.dp
private val DRAWER_AVATAR_SIZE = 40.dp

// Text size constants
private val HEADER_TEXT_SIZE = 24.sp
private val ITEM_TEXT_SIZE = 16.sp
private val SECONDARY_TEXT_SIZE = 14.sp
private val AVATAR_TEXT_SIZE = 18.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavigationDrawerContent(
    onNavigateToWorkouts: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onProfileClick: () -> Unit,
    onShowAllChats: () -> Unit,
    onNewChatCreated: () -> Unit,
    onShowRenameDialog: (ChatSession) -> Unit,
    onShowDeleteDialog: (ChatSession) -> Unit,
    userName: String = "Andy", // TODO: Get from ViewModel in Task 4
    userInitial: String = "A", // TODO: Get from ViewModel in Task 4
    chatHistoryViewModel: ChatHistoryViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    // Observe chat sessions
    val recentSessions by chatHistoryViewModel.recentSessions.observeAsState(emptyList())
    val activeSession by chatHistoryViewModel.activeSession.observeAsState()
    val messagePreviews by chatHistoryViewModel.messagePreviews.observeAsState(emptyMap())
    
    // Load message previews when sessions change
    LaunchedEffect(recentSessions) {
        if (recentSessions.isNotEmpty()) {
            chatHistoryViewModel.loadMessagePreviews(recentSessions)
        }
    }
    
    ModalDrawerSheet(
        modifier = Modifier.width(DRAWER_WIDTH),
        drawerContainerColor = BackgroundDark
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = DRAWER_PADDING_VERTICAL)
        ) {
            // Header with app name
            Text(
                text = "VoiceFitness",
                fontSize = HEADER_TEXT_SIZE,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(
                    horizontal = DRAWER_PADDING_HORIZONTAL,
                    vertical = DRAWER_PADDING_VERTICAL
                )
            )
            
            // Chats section header with new chat button
            ChatsHeaderWithButton(
                onNewChatClick = {
                    chatHistoryViewModel.createNewChat()
                    onNewChatCreated()
                }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Recent chats section label
            Text(
                text = "Recents",
                fontSize = SECONDARY_TEXT_SIZE,
                color = DrawerSecondaryText,
                modifier = Modifier.padding(
                    horizontal = DRAWER_PADDING_HORIZONTAL,
                    vertical = 8.dp
                )
            )
            
            // Recent chats list
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 12.dp)
            ) {
                if (recentSessions.isEmpty()) {
                    // Empty state
                    item {
                        Text(
                            text = "No chats yet",
                            fontSize = SECONDARY_TEXT_SIZE,
                            color = DrawerSecondaryText,
                            modifier = Modifier.padding(
                                horizontal = 8.dp,
                                vertical = 12.dp
                            )
                        )
                    }
                } else {
                    // Show recent chats
                    items(recentSessions) { session ->
                        ChatHistoryItem(
                            session = session,
                            lastMessagePreview = messagePreviews[session.sessionId],
                            isActive = session.sessionId == activeSession?.sessionId,
                            onClick = { chatHistoryViewModel.switchToChat(session.sessionId) },
                            onStar = { chatHistoryViewModel.toggleStarChat(session.sessionId, !session.isStarred) },
                            onRename = { onShowRenameDialog(session) },
                            onDelete = { onShowDeleteDialog(session) }
                        )
                    }
                    
                    // "All chats" link - navigates to AllChatsScreen
                    item {
                        Text(
                            text = "All chats >",
                            fontSize = SECONDARY_TEXT_SIZE,
                            color = DrawerSecondaryText,
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onShowAllChats() }
                                .padding(
                                    horizontal = 8.dp,
                                    vertical = DRAWER_ITEM_PADDING_VERTICAL
                                )
                        )
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = DrawerDivider
            )
            
            // Navigation options
            NavigationDrawerItem(
                icon = Icons.Default.FitnessCenter,
                label = "Workouts",
                onClick = onNavigateToWorkouts
            )
            
            NavigationDrawerItem(
                icon = Icons.Default.Restaurant,
                label = "Nutrition",
                onClick = onNavigateToNutrition
            )
            
            NavigationDrawerItem(
                icon = Icons.Default.TrendingUp,
                label = "Progress",
                onClick = onNavigateToProgress
            )
            
            Spacer(modifier = Modifier.height(DRAWER_PADDING_VERTICAL))
            
            // User profile at bottom
            UserProfileSection(
                userName = userName,
                userInitial = userInitial,
                onClick = onProfileClick
            )
        }
    }
}

/**
 * Chats header with circular new chat button on the right.
 */
@Composable
private fun ChatsHeaderWithButton(onNewChatClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = DRAWER_PADDING_HORIZONTAL,
                vertical = 8.dp
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Chats label with icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                Icons.Default.Chat,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(DRAWER_ICON_SIZE)
            )
            Text(
                text = "Chats",
                fontSize = ITEM_TEXT_SIZE,
                color = Color.White,
                fontWeight = FontWeight.Medium
            )
        }
        
        // Circular new chat button
        IconButton(
            onClick = onNewChatClick,
            modifier = Modifier.size(32.dp)
        ) {
            Surface(
                modifier = Modifier.size(32.dp),
                shape = CircleShape,
                color = PrimaryGreen
            ) {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "New Chat",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

/**
 * Navigation drawer item with icon and label.
 */
@Composable
private fun NavigationDrawerItem(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = DRAWER_PADDING_HORIZONTAL,
                vertical = DRAWER_ITEM_PADDING_VERTICAL
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(
            icon,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(DRAWER_ICON_SIZE)
        )
        Text(
            text = label,
            fontSize = ITEM_TEXT_SIZE,
            color = Color.White
        )
    }
}

/**
 * User profile section at the bottom of the drawer.
 */
@Composable
private fun UserProfileSection(
    userName: String,
    userInitial: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = DRAWER_PADDING_HORIZONTAL,
                vertical = DRAWER_ITEM_PADDING_VERTICAL
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        UserAvatar(
            initial = userInitial,
            size = DRAWER_AVATAR_SIZE
        )
        
        Text(
            text = userName,
            fontSize = ITEM_TEXT_SIZE,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )
    }
}

/**
 * Reusable user avatar component with initial.
 */
@Composable
fun UserAvatar(
    initial: String,
    size: Dp = 40.dp,
    backgroundColor: Color = PrimaryGreen,
    textColor: Color = Color.White
) {
    Surface(
        modifier = Modifier.size(size),
        shape = CircleShape,
        color = backgroundColor
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initial.take(1).uppercase(),
                color = textColor,
                fontSize = AVATAR_TEXT_SIZE,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
