package com.example.classpass.ui.compose

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.classpass.data.entity.ChatSession
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.ErrorRed
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.SheetBackground
import com.example.classpass.ui.viewmodel.ChatHistoryViewModel
import java.text.SimpleDateFormat
import java.util.*

/**
 * All Chats Screen - Full screen view of all chat sessions.
 * Matches the Claude app design with search and full chat list.
 */

// Constants
private val SEARCH_BAR_HEIGHT = 44.dp
private val SEARCH_BAR_MARGIN = 16.dp
private val SEARCH_BAR_TOP_MARGIN = 8.dp
private val CHAT_ITEM_PADDING_HORIZONTAL = 16.dp
private val CHAT_ITEM_PADDING_VERTICAL = 14.dp
private val TITLE_FONT_SIZE = 20.sp
private val CHAT_TITLE_FONT_SIZE = 16.sp
private val TIMESTAMP_FONT_SIZE = 13.sp
private val NEW_CHAT_BUTTON_SIZE = 32.dp
private val NEW_CHAT_ICON_SIZE = 18.dp
private val BACK_BUTTON_SIZE = 24.dp
private val ARROW_ICON_SIZE = 18.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AllChatsScreen(
    onBackClick: () -> Unit,
    onChatClick: (String) -> Unit,
    onNewChatClick: () -> Unit,
    viewModel: ChatHistoryViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    var searchQuery by remember { mutableStateOf("") }
    val allSessions by viewModel.allSessions.observeAsState(emptyList())
    val messagePreviews by viewModel.messagePreviews.observeAsState(emptyMap())
    val scope = rememberCoroutineScope()
    
    // State for action sheets
    var selectedChatForAction by remember { mutableStateOf<ChatSession?>(null) }
    var showRenameSheet by remember { mutableStateOf(false) }
    var showDeleteSheet by remember { mutableStateOf(false) }
    
    // Load message previews when sessions change
    LaunchedEffect(allSessions) {
        if (allSessions.isNotEmpty()) {
            viewModel.loadMessagePreviews(allSessions)
        }
    }
    
    // Filter sessions based on search query
    val filteredSessions = remember(allSessions, searchQuery) {
        if (searchQuery.isBlank()) {
            allSessions
        } else {
            allSessions.filter { session ->
                session.title.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // Top bar
        AllChatsTopBar(
            onBackClick = onBackClick,
            onNewChatClick = onNewChatClick
        )
        
        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = SEARCH_BAR_MARGIN, vertical = SEARCH_BAR_TOP_MARGIN)
        )
        
        // Chat list
        if (filteredSessions.isEmpty()) {
            EmptySearchState(searchQuery = searchQuery)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredSessions) { session ->
                    ChatListItem(
                        session = session,
                        lastMessagePreview = messagePreviews[session.sessionId],
                        onClick = { onChatClick(session.sessionId) },
                        onStar = {
                            viewModel.toggleStarChat(session.sessionId, !session.isStarred)
                        },
                        onRename = {
                            selectedChatForAction = session
                            showRenameSheet = true
                        },
                        onDelete = {
                            selectedChatForAction = session
                            showDeleteSheet = true
                        }
                    )
                }
            }
        }
    }
    
    // Rename Chat Bottom Sheet
    if (showRenameSheet && selectedChatForAction != null) {
        val chatToRename = selectedChatForAction!!
        var newTitle by remember { mutableStateOf(chatToRename.title) }
        val renameSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        ModalBottomSheet(
            onDismissRequest = { 
                showRenameSheet = false
                selectedChatForAction = null
            },
            sheetState = renameSheetState,
            containerColor = CardDark,
            dragHandle = {
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
                    .imePadding()
            ) {
                Text(
                    text = "Rename Chat",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                OutlinedTextField(
                    value = newTitle,
                    onValueChange = { newTitle = it },
                    label = { Text("Chat Title", color = Color.Gray) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryGreen,
                        unfocusedBorderColor = Color.Gray,
                        cursorColor = PrimaryGreen,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        focusedLabelColor = PrimaryGreen,
                        unfocusedLabelColor = Color.Gray,
                        unfocusedContainerColor = SheetBackground,
                        focusedContainerColor = SheetBackground
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = {
                        if (newTitle.isNotBlank()) {
                            viewModel.renameChat(chatToRename.sessionId, newTitle)
                            showRenameSheet = false
                            selectedChatForAction = null
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = newTitle.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryGreen,
                        disabledContainerColor = PrimaryGreen.copy(alpha = 0.5f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Rename", color = Color.White, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { 
                        showRenameSheet = false
                        selectedChatForAction = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = SheetBackground,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
    
    // Delete Confirmation Bottom Sheet
    if (showDeleteSheet && selectedChatForAction != null) {
        val chatToDelete = selectedChatForAction!!
        val deleteSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        
        ModalBottomSheet(
            onDismissRequest = { 
                showDeleteSheet = false
                selectedChatForAction = null
            },
            sheetState = deleteSheetState,
            containerColor = CardDark,
            dragHandle = {
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                Text(
                    text = "Delete Chat?",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
                Text(
                    text = "Are you sure you want to delete \"${chatToDelete.title}\"? This action cannot be undone.",
                    fontSize = 16.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(
                    onClick = {
                        viewModel.deleteChat(chatToDelete)
                        showDeleteSheet = false
                        selectedChatForAction = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = ErrorRed),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Delete", color = Color.White, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(
                    onClick = { 
                        showDeleteSheet = false
                        selectedChatForAction = null
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = SheetBackground,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Cancel", fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

/**
 * Top bar with back button, title, and new chat button.
 */
@Composable
private fun AllChatsTopBar(
    onBackClick: () -> Unit,
    onNewChatClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = BackgroundDark
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Back button
            IconButton(
                onClick = onBackClick,
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                    modifier = Modifier.size(BACK_BUTTON_SIZE)
                )
            }
            
            // Title
            Text(
                text = "Chats",
                fontSize = TITLE_FONT_SIZE,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            
            // New chat button (green circular button) - smaller, matching main chat watch button
            Box {
                IconButton(
                    onClick = onNewChatClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Surface(
                        modifier = Modifier.size(NEW_CHAT_BUTTON_SIZE),
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
                                modifier = Modifier.size(NEW_CHAT_ICON_SIZE)
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Search bar component.
 */
@Composable
private fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(SEARCH_BAR_HEIGHT),
        shape = RoundedCornerShape(10.dp),
        color = Color(0xFF1C1C1C)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = Color(0xFF666666),
                modifier = Modifier.size(22.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            BasicTextField(
                value = query,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    color = Color.White,
                    fontSize = 16.sp
                ),
                singleLine = true,
                cursorBrush = androidx.compose.ui.graphics.SolidColor(PrimaryGreen),
                decorationBox = @Composable { innerTextField ->
                    Box {
                        if (query.isEmpty()) {
                            Text(
                                text = "Search",
                                color = Color(0xFF666666),
                                fontSize = 16.sp
                            )
                        }
                        innerTextField()
                    }
                }
            )
        }
    }
}

/**
 * Individual chat list item.
 */
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun ChatListItem(
    session: ChatSession,
    lastMessagePreview: String? = null,
    onClick: () -> Unit,
    onStar: () -> Unit = {},
    onRename: () -> Unit = {},
    onDelete: () -> Unit = {}
) {
    var showActionsSheet by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = onClick,
                onLongClick = { 
                    showActionsSheet = true
                }
            )
            .padding(
                start = SEARCH_BAR_MARGIN, // Align with search bar outer edge
                end = CHAT_ITEM_PADDING_HORIZONTAL,
                top = CHAT_ITEM_PADDING_VERTICAL,
                bottom = CHAT_ITEM_PADDING_VERTICAL
            )
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star icon (if starred)
            if (session.isStarred) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Starred",
                    tint = Color(0xFFFFD700), // Gold color
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            
            // Title
            Text(
                text = session.title,
                fontSize = CHAT_TITLE_FONT_SIZE,
                fontWeight = FontWeight.Normal,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Timestamp
            Text(
                text = formatTimestamp(session.lastUpdated),
                fontSize = TIMESTAMP_FONT_SIZE,
                color = Color(0xFF888888)
            )
            
            Spacer(modifier = Modifier.width(8.dp))
            
            // Arrow icon
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = "Open chat",
                tint = Color(0xFF666666),
                modifier = Modifier.size(ARROW_ICON_SIZE)
            )
        }
        
        // Message preview (if available)
        if (!lastMessagePreview.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = lastMessagePreview,
                fontSize = 13.sp,
                color = Color(0xFF888888),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
    
    // Chat Actions Bottom Sheet
    if (showActionsSheet) {
        ModalBottomSheet(
            onDismissRequest = { showActionsSheet = false },
            containerColor = CardDark,
            dragHandle = { 
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
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            ) {
                // Chat Title at the top of the sheet
                Text(
                    text = session.title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
                )
                
                // Star/Unstar Chat
                ChatActionItem(
                    icon = if (session.isStarred) Icons.Default.Star else Icons.Default.StarBorder,
                    text = if (session.isStarred) "Unstar Chat" else "Star Chat",
                    textColor = if (session.isStarred) Color(0xFFFFD700) else Color.White, // Gold when starred
                    onClick = {
                        showActionsSheet = false
                        onStar()
                    }
                )
                
                // Rename Chat
                ChatActionItem(
                    icon = Icons.Default.Edit,
                    text = "Rename Chat",
                    onClick = {
                        showActionsSheet = false
                        onRename()
                    }
                )
                
                // Delete Chat
                ChatActionItem(
                    icon = Icons.Default.Delete,
                    text = "Delete Chat",
                    textColor = Color(0xFFFF4444),
                    onClick = {
                        showActionsSheet = false
                        onDelete()
                    }
                )
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

/**
 * Chat action item for bottom sheet.
 */
@Composable
private fun ChatActionItem(
    icon: ImageVector,
    text: String,
    onClick: () -> Unit,
    textColor: Color = Color.White
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = text,
            tint = textColor,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = text,
            color = textColor,
            fontSize = 16.sp
        )
    }
}

/**
 * Empty state when search returns no results.
 */
@Composable
private fun EmptySearchState(searchQuery: String) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = if (searchQuery.isBlank()) "No chats yet" else "No results found",
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
            Text(
                text = if (searchQuery.isBlank()) 
                    "Start a new conversation to get started" 
                else 
                    "Try a different search term",
                fontSize = 14.sp,
                color = Color.Gray
            )
        }
    }
}

/**
 * Format timestamp for display.
 */
private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3_600_000 -> "${diff / 60_000}m ago"
        diff < 86_400_000 -> "${diff / 3_600_000}h ago"
        diff < 172_800_000 -> "Yesterday"
        diff < 604_800_000 -> "${diff / 86_400_000}d ago"
        diff < 2_592_000_000 -> "${diff / 604_800_000}w ago"
        else -> {
            val timestampDate = Date(timestamp)
            val currentDate = Date(now)
            val timestampYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(timestampDate)
            val currentYear = SimpleDateFormat("yyyy", Locale.getDefault()).format(currentDate)
            
            val dateFormat = if (timestampYear == currentYear) {
                SimpleDateFormat("MMM d", Locale.getDefault())
            } else {
                SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
            }
            dateFormat.format(timestampDate)
        }
    }
}

