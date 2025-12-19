package com.example.classpass.ui.compose.sheets

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.WorkoutProgramDto
import com.example.classpass.ui.theme.CardDark
import com.example.classpass.ui.theme.PrimaryGreen
import com.example.classpass.ui.theme.TextSecondaryDark

/**
 * Program Options Bottom Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgramOptionsSheet(
    program: WorkoutProgramDto,
    onDismiss: () -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        contentColor = Color.White,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.Gray
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp)
        ) {
            // Sheet Title
            Text(
                text = "Program Options",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Edit Option
            ProgramOptionItem(
                icon = Icons.Default.Edit,
                title = "Edit Program",
                description = "Modify exercises and schedule",
                onClick = onEditClick,
                enabled = true
            )
            
            // Share Option (Future feature - enabled but no functionality yet)
            ProgramOptionItem(
                icon = Icons.Default.Share,
                title = "Share Program",
                description = "Share with friends or coach",
                onClick = { onDismiss() }, // Just dismiss for now
                enabled = true
            )
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 8.dp),
                color = Color.Gray.copy(alpha = 0.2f)
            )
            
            // Delete Option (Destructive)
            ProgramOptionItem(
                icon = Icons.Default.Delete,
                title = "Delete Program",
                description = "Permanently remove this program",
                onClick = onDeleteClick,
                isDestructive = true
            )
        }
    }
}

@Composable
private fun ProgramOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                modifier = Modifier.size(48.dp),
                shape = CircleShape,
                color = when {
                    !enabled -> Color.Gray.copy(alpha = 0.2f)
                    isDestructive -> Color.Red.copy(alpha = 0.15f)
                    else -> PrimaryGreen.copy(alpha = 0.15f)
                }
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        icon,
                        contentDescription = title,
                        tint = when {
                            !enabled -> Color.Gray.copy(alpha = 0.5f)
                            isDestructive -> Color.Red
                            else -> PrimaryGreen
                        },
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = when {
                        !enabled -> Color.Gray.copy(alpha = 0.5f)
                        isDestructive -> Color.Red
                        else -> Color.White
                    }
                )
                Text(
                    description,
                    fontSize = 13.sp,
                    color = when {
                        !enabled -> Color.Gray.copy(alpha = 0.4f)
                        else -> TextSecondaryDark
                    }
                )
            }
        }
    }
}

/**
 * Delete Program Confirmation Sheet
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeleteProgramSheet(
    programTitle: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        containerColor = CardDark,
        contentColor = Color.White,
        dragHandle = {
            Surface(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .width(40.dp)
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp)),
                color = Color.Gray
            ) {}
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(top = 16.dp, bottom = 32.dp)
        ) {
            // Title
            Text(
                text = "Delete Program?",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Text(
                text = "Are you sure you want to delete \"$programTitle\"? This action cannot be undone.",
                fontSize = 15.sp,
                color = TextSecondaryDark,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(28.dp))
            
            // Delete Button
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Delete Program",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Cancel Button
            TextButton(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
            ) {
                Text(
                    "Cancel",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White
                )
            }
        }
    }
}

