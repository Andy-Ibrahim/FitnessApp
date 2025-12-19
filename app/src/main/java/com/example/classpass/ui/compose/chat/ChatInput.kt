package com.example.classpass.ui.compose.chat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.ui.theme.BackgroundDark
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.SheetBackground
import com.example.classpass.ui.theme.SheetCardBackground
import com.example.classpass.ui.theme.TextSecondaryDark

/**
 * Chat input field with attachment options.
 * Includes a unified rounded container with text input and action buttons.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatInput(
    value: String,
    onValueChange: (String) -> Unit,
    onSend: () -> Unit,
    isProcessing: Boolean = false
) {
    var showBottomSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    // One unified rounded container
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
            .navigationBarsPadding(),
        shape = RoundedCornerShape(28.dp),
        color = CardDark
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Input text field - grows as you type
            androidx.compose.foundation.text.BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .padding(top = 20.dp),
                textStyle = TextStyle(
                    fontSize = 16.sp,
                    color = Color.White
                ),
                enabled = !isProcessing,
                maxLines = 5,
                decorationBox = { innerTextField ->
                    Box(
                        contentAlignment = Alignment.CenterStart
                    ) {
                        if (value.isEmpty()) {
                            Text(
                                text = if (isProcessing) "AI is thinking..." else "Ask your AI fitness coach...",
                                fontSize = 16.sp,
                                color = TextSecondaryDark.copy(alpha = 0.6f)
                            )
                        }
                        innerTextField()
                    }
                }
            )
            
            // Button bar at the bottom of the container
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left side buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { showBottomSheet = true },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Add,
                            contentDescription = "Add attachment",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
                
                // Right side buttons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { /* TODO: Add voice input */ },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            Icons.Default.Mic,
                            contentDescription = "Voice input",
                            tint = TextSecondaryDark,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    // Send/Waveform button - extracted for reusability
                    if (value.isNotBlank()) {
                        CircularIconButton(
                            icon = Icons.Default.Send,
                            contentDescription = "Send",
                            backgroundColor = PrimaryGreen,
                            iconTint = Color.White,
                            iconSize = 22.dp,
                            onClick = {
                                if (!isProcessing) {
                                    onSend()
                                }
                            }
                        )
                    } else {
                        CircularIconButton(
                            icon = Icons.Default.GraphicEq,
                            contentDescription = "Voice waveform",
                            backgroundColor = TextSecondaryDark.copy(alpha = 0.2f),
                            iconTint = TextSecondaryDark,
                            iconSize = 26.dp,
                            onClick = { /* Placeholder for voice input */ }
                        )
                    }
                }
            }
        }
    }
    
    // Bottom sheet for attachments
    if (showBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showBottomSheet = false },
            sheetState = sheetState,
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
            AttachmentOptionsSheet(onDismiss = { showBottomSheet = false })
        }
    }
}

/**
 * Reusable circular icon button with customizable appearance.
 */
@Composable
private fun CircularIconButton(
    icon: ImageVector,
    contentDescription: String,
    backgroundColor: Color,
    iconTint: Color,
    iconSize: androidx.compose.ui.unit.Dp,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        modifier = Modifier.size(48.dp)
    ) {
        Surface(
            shape = CircleShape,
            color = backgroundColor,
            modifier = Modifier.size(48.dp)
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    icon,
                    contentDescription = contentDescription,
                    tint = iconTint,
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}

@Composable
fun AttachmentOptionsSheet(onDismiss: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp)
            .padding(top = 16.dp, bottom = 32.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            AttachmentOption(
                icon = Icons.Default.CameraAlt,
                label = "Camera",
                onClick = { /* TODO: Open camera */ }
            )
            
            AttachmentOption(
                icon = Icons.Default.Photo,
                label = "Photos",
                onClick = { /* TODO: Open photo picker */ }
            )
            
            AttachmentOption(
                icon = Icons.Default.InsertDriveFile,
                label = "Files",
                onClick = { /* TODO: Open file picker */ }
            )
        }
    }
}

@Composable
private fun AttachmentOption(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(100.dp)
            .clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(64.dp),
            shape = RoundedCornerShape(16.dp),
            color = SheetCardBackground
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            ) {
                Icon(
                    icon,
                    contentDescription = label,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = label,
            fontSize = 14.sp,
            color = Color.White,
            textAlign = TextAlign.Center
        )
    }
}
