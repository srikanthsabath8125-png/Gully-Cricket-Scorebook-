package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import kotlinx.coroutines.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.CoinSide
import kotlin.math.roundToInt

@Composable
fun CoinFlipView(
    isFlipping: Boolean,
    landedSide: CoinSide?,
    modifier: Modifier = Modifier
) {
    val rotationY = remember { Animatable(0f) }
    val jumpOffset = remember { Animatable(0f) }

    LaunchedEffect(isFlipping) {
        if (isFlipping) {
            // Flip 5 full rotations (1800 deg)
            launch {
                rotationY.animateTo(
                    targetValue = rotationY.value + 1800f,
                    animationSpec = tween(durationMillis = 1500, easing = FastOutSlowInEasing)
                )
            }
            // Vertical toss physics
            launch {
                jumpOffset.animateTo(
                    targetValue = -90f,
                    animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
                )
                jumpOffset.animateTo(
                    targetValue = 0f,
                    animationSpec = tween(durationMillis = 750, easing = FastOutSlowInEasing)
                )
            }
        }
    }

    val currentSide = when {
        isFlipping -> {
            val normalized = (rotationY.value % 360f).let { if (it < 0) it + 360f else it }
            if (normalized in 90f..270f) CoinSide.TAILS else CoinSide.HEADS
        }
        landedSide != null -> landedSide
        else -> CoinSide.HEADS
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Shadow on the ground
        Box(
            modifier = Modifier
                .size(width = 110.dp, height = 24.dp)
                .offset(y = 75.dp)
                .background(
                    color = Color.Black.copy(alpha = if (isFlipping) 0.15f else 0.35f),
                    shape = CircleShape
                )
        )

        // Animated Coin
        Box(
            modifier = Modifier
                .offset { IntOffset(0, jumpOffset.value.roundToInt()) }
                .graphicsLayer {
                    this.rotationY = rotationY.value
                    cameraDistance = 16f * density
                }
                .size(130.dp)
                .shadow(elevation = 12.dp, shape = CircleShape)
                .clip(CircleShape)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color(0xFFFFECB3),
                            Color(0xFFFFD54F),
                            Color(0xFFFFB300),
                            Color(0xFFE69500)
                        )
                    )
                )
                .border(
                    width = 4.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFFFF8E1), Color(0xFFFF8F00))
                    ),
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            // Coin inner rim
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .border(1.5.dp, Color(0xFFC68400), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (currentSide == CoinSide.HEADS) "H" else "T",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = if (currentSide == CoinSide.HEADS) "HEADS" else "TAILS",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = Color(0xFF795548)
                    )
                }
            }
        }
    }
}
