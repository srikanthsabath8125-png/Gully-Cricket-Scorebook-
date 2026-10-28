package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BallRecord
import com.example.model.InningsData
import com.example.ui.theme.BoundaryFourColor
import com.example.ui.theme.DotBallColor
import com.example.ui.theme.ExtraBadgeColor
import com.example.ui.theme.SingleRunColor
import com.example.ui.theme.SixerColor
import com.example.ui.theme.WicketBadgeColor

@Composable
fun OverTimelineRow(
    currentInnings: InningsData,
    modifier: Modifier = Modifier
) {
    val currentOverBalls = currentInnings.currentOverBalls
    val currentOverNum = (currentInnings.legalBalls / 6) + 1
    val scrollState = rememberScrollState()

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Over $currentOverNum :",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "(${currentInnings.currentOverBallsCount}/6 legal balls)",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                val overRuns = currentOverBalls.sumOf { it.totalRuns }
                val overWickets = currentOverBalls.count { it.isWicket }
                Text(
                    text = "$overRuns runs" + if (overWickets > 0) " • $overWickets wkts" else "",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (currentOverBalls.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Text(
                        text = "No balls bowled in Over $currentOverNum yet. Tap a button below to score!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(scrollState)
                        .testTag("over_timeline_row"),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    currentOverBalls.forEach { ball ->
                        BallBadge(ball = ball)
                    }
                }
            }
        }
    }
}

@Composable
fun BallBadge(
    ball: BallRecord,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when {
        ball.isWicket -> Pair(WicketBadgeColor, Color.White)
        ball.displayText == "6" -> Pair(SixerColor, Color.White)
        ball.displayText == "4" -> Pair(BoundaryFourColor, Color.White)
        ball.displayText.contains("Wd") || ball.displayText.contains("NB") -> Pair(ExtraBadgeColor, Color.White)
        ball.displayText == "0" -> Pair(DotBallColor, Color.White)
        else -> Pair(SingleRunColor, Color.White)
    }

    val isSpecial = ball.displayText.length > 2
    val size = if (isSpecial) 44.dp else 36.dp

    Box(
        modifier = modifier
            .size(width = if (isSpecial) 52.dp else 38.dp, height = 38.dp)
            .clip(RoundedCornerShape(if (isSpecial) 12.dp else 19.dp))
            .background(bgColor)
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(if (isSpecial) 12.dp else 19.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = ball.displayText,
            color = textColor,
            fontWeight = FontWeight.Bold,
            fontSize = if (isSpecial) 12.sp else 14.sp
        )
    }
}
