package com.example.classpass.ui.compose

import android.app.Application
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.classpass.data.entity.ChatSession
import com.example.classpass.ui.compose.chat.ChatInput
import com.example.classpass.ui.compose.chat.EmptyChatState
import com.example.classpass.ui.compose.chat.UserMessageBubble
import com.example.classpass.ui.compose.chat.AIMessageBubble
import com.example.classpass.ui.compose.chat.TypingIndicator
import com.example.classpass.ui.compose.common.BottomSheetDragHandle
import com.example.classpass.ui.compose.drawer.NavigationDrawerContent
import com.example.classpass.ui.compose.sheets.SettingsSheet
import com.example.classpass.ui.compose.sheets.WatchConnectionSheet
import com.example.classpass.ui.compose.topbar.MainChatTopBar
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.SheetBackground
import com.example.classpass.ui.viewmodel.ChatHistoryViewModel
import com.example.classpass.ui.viewmodel.MainChatViewModel

/**
 * Main Chat Screen - Perplexity-style conversational interface with session support.
 * Clean, minimal, conversation-first design.
 */

// Chat screen constants
private val CHAT_CONTENT_PADDING = 16.dp
private val MESSAGE_SPACING = 16.dp
private val BOTTOM_SPACER_HEIGHT = 80.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainChatScreen(
    onNavigateToWorkouts: () -> Unit,
    onNavigateToNutrition: () -> Unit,
    onNavigateToProgress: () -> Unit,
    onNavigateToAllChats: () -> Unit = {},
    mainChatViewModel: MainChatViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    ),
    chatHistoryViewModel: ChatHistoryViewModel = viewModel(
        factory = androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.getInstance(
            LocalContext.current.applicationContext as Application
        )
    )
) {
    var userInput by remember { mutableStateOf("") }
    val messages by mainChatViewModel.currentMessages.observeAsState(emptyList())
    val isProcessing by mainChatViewModel.isProcessing.observeAsState(false)
    val activeSession by chatHistoryViewModel.activeSession.observeAsState()
    val listState = rememberLazyListState()
    val focusManager = LocalFocusManager.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    var showWatchBottomSheet by remember { mutableStateOf(false) }
    val watchSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var showSettingsBottomSheet by remember { mutableStateOf(false) }
    val settingsSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var sessionToRename by remember { mutableStateOf<ChatSession?>(null) }
    var sessionToDelete by remember { mutableStateOf<ChatSession?>(null) }
    
    // Initialize session on first launch
    LaunchedEffect(Unit) {
        mainChatViewModel.initializeSession()
    }
    
    // Update MainChatViewModel when active session changes
    LaunchedEffect(activeSession) {
        activeSession?.let {
            mainChatViewModel.switchSession(it.sessionId)
        }
    }
    
    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }
    
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            NavigationDrawerContent(
                onNavigateToWorkouts = {
                    scope.launch { drawerState.close() }
                    onNavigateToWorkouts()
                },
                onNavigateToNutrition = {
                    scope.launch { drawerState.close() }
                    onNavigateToNutrition()
                },
                onNavigateToProgress = {
                    scope.launch { drawerState.close() }
                    onNavigateToProgress()
                },
                onProfileClick = {
                    scope.launch { drawerState.close() }
                    showSettingsBottomSheet = true
                },
                onShowAllChats = {
                    scope.launch { drawerState.close() }
                    onNavigateToAllChats()
                },
                onNewChatCreated = {
                    scope.launch { drawerState.close() }
                },
                onShowRenameDialog = { session ->
                    sessionToRename = session
                },
                onShowDeleteDialog = { session ->
                    sessionToDelete = session
                },
                chatHistoryViewModel = chatHistoryViewModel
            )
        },
        gesturesEnabled = true
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(BackgroundDark)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus()
                }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding()
            ) {
                // Top bar with menu button, trainer mode button, and watch button
                MainChatTopBar(
                    onMenuClick = {
                        scope.launch { drawerState.open() }
                    },
                    onTrainerModeClick = {
                        // TODO: Navigate to Trainer Mode screen (Task 6)
                        // For now, just a placeholder
                    },
                    onWatchClick = {
                        showWatchBottomSheet = true
                    }
                )
            
                // Chat messages or empty state
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    if (messages.isEmpty()) {
                        EmptyChatState()
                    } else {
                        LazyColumn(
                            state = listState,
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(CHAT_CONTENT_PADDING),
                            verticalArrangement = Arrangement.spacedBy(MESSAGE_SPACING)
                        ) {
                            items(messages) { message ->
                                if (message.role == "user") {
                                    UserMessageBubble(message = message.content)
                                } else {
                                    AIMessageBubble(message = message.content)
                                }
                            }
                            
                            // Show typing indicator when processing
                            if (isProcessing) {
                                item {
                                    TypingIndicator()
                                }
                            }
                            
                            // Bottom padding
                            item {
                                Spacer(modifier = Modifier.height(BOTTOM_SPACER_HEIGHT))
                            }
                        }
                    }
                }
            
                // Chat input
                ChatInput(
                    value = userInput,
                    onValueChange = { userInput = it },
                    onSend = {
                        if (userInput.isNotBlank() && !isProcessing) {
                            mainChatViewModel.sendMessage(userInput)
                            userInput = ""
                        }
                    },
                    isProcessing = isProcessing
                )
            }
        }
        
        // Watch connection bottom sheet
        if (showWatchBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showWatchBottomSheet = false },
                sheetState = watchSheetState,
                containerColor = CardDark,
                dragHandle = { BottomSheetDragHandle() }
            ) {
                WatchConnectionSheet()
            }
        }
        
        // Settings bottom sheet
        if (showSettingsBottomSheet) {
            ModalBottomSheet(
                onDismissRequest = { showSettingsBottomSheet = false },
                sheetState = settingsSheetState,
                containerColor = CardDark,
                dragHandle = { BottomSheetDragHandle() }
            ) {
                SettingsSheet(onDismiss = { showSettingsBottomSheet = false })
            }
        }
        
        // Rename dialog
        sessionToRename?.let { session ->
            RenameChatDialog(
                currentTitle = session.title,
                onDismiss = { sessionToRename = null },
                onConfirm = { newTitle ->
                    chatHistoryViewModel.renameChat(session.sessionId, newTitle)
                    sessionToRename = null
                }
            )
        }
        
        // Delete confirmation dialog (P1-3)
        sessionToDelete?.let { session ->
            DeleteChatDialog(
                chatTitle = session.title,
                onDismiss = { sessionToDelete = null },
                onConfirm = {
                    chatHistoryViewModel.deleteChat(session)
                    sessionToDelete = null
                }
            )
        }
    }
}

/**
 * Reusable drag handle for bottom sheets.
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
            shape = androidx.compose.foundation.shape.RoundedCornerShape(2.dp),
            color = Color.Gray
        ) {}
    }
}

/**
 * Bottom sheet for renaming a chat session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RenameChatDialog(
    currentTitle: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var newTitle by remember { mutableStateOf(currentTitle) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
                .imePadding()
        ) {
            // Title
            Text(
                text = "Rename Chat",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 20.dp)
            )
            
            // Text field
            OutlinedTextField(
                value = newTitle,
                onValueChange = { newTitle = it },
                label = { Text("Chat Title", color = Color.Gray) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    focusedBorderColor = com.example.classpass.ui.theme.PrimaryGreen,
                    unfocusedBorderColor = Color.Gray,
                    cursorColor = com.example.classpass.ui.theme.PrimaryGreen,
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            )
            
            // Rename button
            Button(
                onClick = { if (newTitle.isNotBlank()) onConfirm(newTitle) },
                enabled = newTitle.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.classpass.ui.theme.PrimaryGreen,
                    disabledContainerColor = Color.Gray
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Rename",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Cancel button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SheetBackground
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}

/**
 * Bottom sheet for confirming chat deletion.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeleteChatDialog(
    chatTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 40.dp)
        ) {
            // Title
            Text(
                text = "Delete Chat?",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(bottom = 12.dp)
            )
            
            // Message
            Text(
                text = "Are you sure you want to delete \"$chatTitle\"? This action cannot be undone.",
                fontSize = 16.sp,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 24.dp)
            )
            
            // Delete button
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = com.example.classpass.ui.theme.ErrorRed
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Delete",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Cancel button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = SheetBackground
                ),
                shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }
}
