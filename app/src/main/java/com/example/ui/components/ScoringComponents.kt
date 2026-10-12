package com.example.ui.components

import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.*

/**
 * 3D Button with pressed animation, top highlight, layered shadow, and haptic feedback
 */
@Composable
fun Scoring3DButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    baseColor: Color = Color(0xFF22A860),
    textColor: Color = Color.White,
    fontSize: Int = 16,
    subText: String? = null,
    isDestructive: Boolean = false
) {
    val context = LocalContext.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val translationY by animateFloatAsState(
        targetValue = if (isPressed) 3f else 0f,
        label = "button3d_press"
    )

    val vibrator = remember {
        context.getSystemService(android.content.Context.VIBRATOR_SERVICE) as? Vibrator
    }

    val shadowColor = remember(baseColor) {
        // Darkened bottom layer for 3D effect
        baseColor.copy(
            red = (baseColor.red * 0.65f).coerceIn(0f, 1f),
            green = (baseColor.green * 0.65f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 0.65f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    val topHighlight = remember(baseColor) {
        baseColor.copy(
            red = (baseColor.red * 1.25f).coerceIn(0f, 1f),
            green = (baseColor.green * 1.25f).coerceIn(0f, 1f),
            blue = (baseColor.blue * 1.25f).coerceIn(0f, 1f),
            alpha = 1f
        )
    }

    Box(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp)
            .heightIn(min = 40.dp)
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) {
                // Haptic feedback
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        vibrator?.vibrate(VibrationEffect.createOneShot(25, VibrationEffect.DEFAULT_AMPLITUDE))
                    } else {
                        @Suppress("DEPRECATION")
                        vibrator?.vibrate(25)
                    }
                } catch (e: Exception) {
                    // Ignore haptic errors
                }
                onClick()
            }
    ) {
        // Bottom 3D shadow layer
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = 3.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(shadowColor)
        )

        // Top button surface with gradient & highlight
        Box(
            modifier = Modifier
                .matchParentSize()
                .offset(y = translationY.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    Brush.verticalGradient(
                        colors = listOf(topHighlight, baseColor)
                    )
                )
                .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(2.dp)
            ) {
                Text(
                    text = text,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = fontSize.sp,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                if (subText != null) {
                    Text(
                        text = subText,
                        color = textColor.copy(alpha = 0.8f),
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                }
            }
        }
    }
}

/**
 * Ball Bubble for the "This Over" row
 */
@Composable
fun BallBubble(
    text: String,
    runs: Int,
    isWicket: Boolean,
    extrasType: String,
    modifier: Modifier = Modifier
) {
    val bubbleColor = when {
        isWicket -> BallWicket
        extrasType == "NO_BALL" -> BallNoBall
        extrasType == "WIDE" -> BallWideExtras
        extrasType in listOf("BYE", "LEG_BYE") -> BallWideExtras
        runs == 0 -> BallDot
        runs in 1..3 -> BallRuns123
        runs == 4 -> BallFour
        runs == 6 -> BallSix
        else -> BallRuns123
    }

    Box(
        modifier = modifier
            .size(32.dp)
            .shadow(2.dp, CircleShape)
            .clip(CircleShape)
            .background(bubbleColor)
            .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            textAlign = TextAlign.Center
        )
    }
}
