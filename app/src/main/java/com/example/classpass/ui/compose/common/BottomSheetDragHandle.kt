package com.example.classpass.ui.compose.common

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Reusable drag handle for bottom sheets.
 * Provides consistent styling across all bottom sheets in the app.
 */

// Drag handle constants
private val DRAG_HANDLE_WIDTH = 40.dp
private val DRAG_HANDLE_HEIGHT = 4.dp
private val DRAG_HANDLE_CORNER_RADIUS = 2.dp
private val DRAG_HANDLE_PADDING_VERTICAL = 12.dp

@Composable
fun BottomSheetDragHandle(
    color: Color = Color.Gray
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = DRAG_HANDLE_PADDING_VERTICAL),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .width(DRAG_HANDLE_WIDTH)
                .height(DRAG_HANDLE_HEIGHT),
            shape = RoundedCornerShape(DRAG_HANDLE_CORNER_RADIUS),
            color = color
        ) {}
    }
}

