package com.example.classpass.ui.compose.chat

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.classpass.ui.theme.PrimaryGreen

/**
 * Chat message bubbles for user and AI messages.
 */

// Constants for message bubble styling
private val MESSAGE_BUBBLE_MAX_WIDTH = 280.dp
private val MESSAGE_BUBBLE_CORNER_RADIUS = 20.dp
private val MESSAGE_BUBBLE_PADDING = 16.dp
private val MESSAGE_TEXT_SIZE = 15.sp

@Composable
fun UserMessageBubble(message: String) {
    MessageBubble(
        message = message,
        backgroundColor = PrimaryGreen,
        textColor = Color.White,
        alignment = Arrangement.End
    )
}

@Composable
fun AIMessageBubble(message: String) {
    MessageBubble(
        message = message,
        backgroundColor = Color.White.copy(alpha = 0.1f),
        textColor = Color.White,
        alignment = Arrangement.Start
    )
}

/**
 * Unified message bubble component to reduce code duplication.
 */
@Composable
private fun MessageBubble(
    message: String,
    backgroundColor: Color,
    textColor: Color,
    alignment: Arrangement.Horizontal
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = alignment
    ) {
        Surface(
            shape = RoundedCornerShape(MESSAGE_BUBBLE_CORNER_RADIUS),
            color = backgroundColor,
            modifier = Modifier.widthIn(max = MESSAGE_BUBBLE_MAX_WIDTH)
        ) {
            Text(
                text = message,
                modifier = Modifier.padding(MESSAGE_BUBBLE_PADDING),
                fontSize = MESSAGE_TEXT_SIZE,
                color = textColor
            )
        }
    }
}

@Composable
fun TypingIndicator() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start
    ) {
        Surface(
            shape = RoundedCornerShape(MESSAGE_BUBBLE_CORNER_RADIUS),
            color = Color.White.copy(alpha = 0.1f),
            modifier = Modifier.padding(4.dp)
        ) {
            Row(
                modifier = Modifier.padding(MESSAGE_BUBBLE_PADDING),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                repeat(3) { index ->
                    AnimatedDot(delayMillis = index * 150)
                }
            }
        }
    }
}

/**
 * Animated dot for typing indicator with pulsing effect.
 */
@Composable
private fun AnimatedDot(delayMillis: Int) {
    val infiniteTransition = rememberInfiniteTransition(label = "dot_animation")
    
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_scale"
    )
    
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 600,
                delayMillis = delayMillis,
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    
    Surface(
        modifier = Modifier
            .size(8.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                this.alpha = alpha
            },
        shape = CircleShape,
        color = PrimaryGreen
    ) {}
}
