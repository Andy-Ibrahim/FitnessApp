package com.example.classpass.ui.compose.sheets

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.domain.model.ExerciseDto
import com.example.classpass.ui.theme.*
import com.example.classpass.ui.compose.common.BottomSheetDragHandle
import java.util.UUID

/**
 * Reusable bottom sheet for adding or editing an exercise
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddEditExerciseSheet(
    exercise: ExerciseDto?,
    onDismiss: () -> Unit,
    onSave: (ExerciseDto) -> Unit
) {
    var name by remember { mutableStateOf(exercise?.name ?: "") }
    var sets by remember { mutableStateOf(exercise?.sets ?: 3) }
    var reps by remember { mutableStateOf(exercise?.reps ?: 10) }
    var weight by remember { mutableStateOf(exercise?.weight?.toString() ?: "") }
    var restSeconds by remember { mutableStateOf(exercise?.restSeconds ?: 90) }
    var notes by remember { mutableStateOf(exercise?.notes ?: "") }
    
    val focusManager = LocalFocusManager.current
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true // Always show fully expanded
    )

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = BackgroundDark,
        dragHandle = { BottomSheetDragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding() // Adjust for keyboard
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    focusManager.clearFocus() // Dismiss keyboard on tap outside
                }
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Text(
                text = if (exercise == null) "Add Exercise" else "Edit Exercise",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Exercise Name
            Text(
                "Exercise Name",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            CleanInputField(
                value = name,
                onValueChange = { name = it },
                placeholder = "e.g. Bench Press"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Sets and Reps
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Sets",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NumberInputField(
                        value = sets,
                        onValueChange = { sets = it },
                        min = 1,
                        max = 20
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Reps",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NumberInputField(
                        value = reps,
                        onValueChange = { reps = it },
                        min = 1,
                        max = 100
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weight and Rest Time
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Weight (kg)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    CleanInputField(
                        value = weight,
                        onValueChange = { weight = it },
                        placeholder = "Optional",
                        keyboardType = KeyboardType.Decimal
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        "Rest (seconds)",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = TextSecondaryDark
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    NumberInputField(
                        value = restSeconds,
                        onValueChange = { restSeconds = it },
                        min = 0,
                        max = 600
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Notes
            Text(
                "Notes (Optional)",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = TextSecondaryDark
            )
            Spacer(modifier = Modifier.height(8.dp))
            CleanInputField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = "Any instructions or tips",
                minLines = 3
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Save Button
            Button(
                onClick = {
                    if (name.isNotBlank()) {
                        val exerciseDto = ExerciseDto(
                            id = exercise?.id ?: UUID.randomUUID().toString(),
                            name = name,
                            sets = sets,
                            reps = reps,
                            weight = weight.toFloatOrNull(),
                            restSeconds = restSeconds,
                            notes = notes.ifBlank { null }
                        )
                        onSave(exerciseDto)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PrimaryGreen),
                shape = RoundedCornerShape(12.dp),
                enabled = name.isNotBlank()
            ) {
                Text(
                    "Save Exercise",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }
        }
    }
}

@Composable
private fun CleanInputField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
    minLines: Int = 1
) {
    val focusManager = LocalFocusManager.current
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardDark
    ) {
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            textStyle = androidx.compose.ui.text.TextStyle(
                fontSize = 16.sp,
                color = Color.White
            ),
            cursorBrush = SolidColor(PrimaryGreen),
            keyboardOptions = KeyboardOptions(
                keyboardType = keyboardType,
                imeAction = if (minLines > 1) ImeAction.Default else ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = { focusManager.clearFocus() }
            ),
            minLines = minLines,
            decorationBox = { innerTextField ->
                if (value.isEmpty()) {
                    Text(
                        placeholder,
                        fontSize = 16.sp,
                        color = TextSecondaryDark.copy(alpha = 0.5f)
                    )
                }
                innerTextField()
            }
        )
    }
}

@Composable
private fun NumberInputField(
    value: Int,
    onValueChange: (Int) -> Unit,
    min: Int = 0,
    max: Int = Int.MAX_VALUE
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = CardDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Minus button
            IconButton(
                onClick = { if (value > min) onValueChange(value - 1) },
                enabled = value > min
            ) {
                Text(
                    "−",
                    fontSize = 24.sp,
                    color = if (value > min) PrimaryGreen else Color.Gray
                )
            }

            // Value display
            Text(
                value.toString(),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            // Plus button
            IconButton(
                onClick = { if (value < max) onValueChange(value + 1) },
                enabled = value < max
            ) {
                Text(
                    "+",
                    fontSize = 24.sp,
                    color = if (value < max) PrimaryGreen else Color.Gray
                )
            }
        }
    }
}

