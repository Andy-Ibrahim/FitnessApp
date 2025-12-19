package com.example.classpass.ui.compose.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.ui.compose.common.BottomSheetDragHandle
import com.example.classpass.ui.theme.*

/**
 * Bottom sheet for rest day logging and notes
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestDaySheet(
    dayName: String,
    existingNote: String = "",
    existingFeeling: String = "",
    existingActivities: List<String> = emptyList(),
    onDismiss: () -> Unit,
    onSave: (note: String, feeling: String, activities: List<String>) -> Unit = { _, _, _ -> }
) {
    var note by remember { mutableStateOf(existingNote) }
    var selectedFeeling by remember { mutableStateOf(existingFeeling) }
    var selectedActivities by remember { mutableStateOf(existingActivities.toSet()) }
    
    val feelings = listOf(
        "💪" to "Energized",
        "😊" to "Good",
        "😐" to "Normal",
        "😴" to "Tired",
        "😣" to "Sore"
    )
    
    val activities = listOf(
        "Stretching",
        "Light Walk",
        "Yoga",
        "Swimming",
        "Massage",
        "Foam Rolling",
        "Meditation"
    )
    
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CardDark,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Text(
                text = dayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "Rest & Recovery Day",
                fontSize = 14.sp,
                color = TextSecondaryDark,
                modifier = Modifier.padding(top = 4.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // How are you feeling?
            Text(
                text = "How are you feeling?",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Feeling Options
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                feelings.forEach { (emoji, label) ->
                    FeelingOption(
                        emoji = emoji,
                        label = label,
                        isSelected = selectedFeeling == label,
                        onClick = { selectedFeeling = if (selectedFeeling == label) "" else label }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Recovery Activities
            Text(
                text = "Recovery Activities",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Activity Chips
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                activities.forEach { activity ->
                    ActivityChip(
                        label = activity,
                        isSelected = selectedActivities.contains(activity),
                        onClick = {
                            selectedActivities = if (selectedActivities.contains(activity)) {
                                selectedActivities - activity
                            } else {
                                selectedActivities + activity
                            }
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Notes
            Text(
                text = "Notes",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp),
                placeholder = { 
                    Text(
                        "How's your body feeling? Any areas that need attention?",
                        color = TextSecondaryDark
                    ) 
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryGreen,
                    unfocusedBorderColor = TextSecondaryDark.copy(alpha = 0.3f),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = PrimaryGreen
                ),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Save Button
            Button(
                onClick = {
                    onSave(note, selectedFeeling, selectedActivities.toList())
                    onDismiss()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    "Save",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun FeelingOption(
    emoji: String,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Surface(
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(28.dp),
            color = if (isSelected) PrimaryGreen.copy(alpha = 0.2f) else BackgroundDark,
            border = if (isSelected) androidx.compose.foundation.BorderStroke(2.dp, PrimaryGreen) else null
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = emoji,
                    fontSize = 28.sp
                )
            }
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            color = if (isSelected) Color.White else TextSecondaryDark
        )
    }
}

@Composable
private fun ActivityChip(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = if (isSelected) PrimaryGreen.copy(alpha = 0.2f) else BackgroundDark,
        border = if (isSelected) androidx.compose.foundation.BorderStroke(1.dp, PrimaryGreen) else null,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Text(
            text = label,
            fontSize = 14.sp,
            color = if (isSelected) PrimaryGreen else Color.White,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun FlowRow(
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    verticalArrangement: Arrangement.Vertical = Arrangement.Top,
    content: @Composable androidx.compose.foundation.layout.FlowRowScope.() -> Unit
) {
    androidx.compose.foundation.layout.FlowRow(
        modifier = modifier,
        horizontalArrangement = horizontalArrangement,
        verticalArrangement = verticalArrangement,
        content = content
    )
}

