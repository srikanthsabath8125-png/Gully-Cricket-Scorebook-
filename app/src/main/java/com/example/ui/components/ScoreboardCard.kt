package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InningsData
import com.example.model.MatchState
import com.example.ui.theme.BoundaryGold
import com.example.ui.theme.CricketBallRed
import com.example.ui.theme.TurfGreenDark
import com.example.ui.theme.TurfGreenLight
import com.example.ui.theme.TurfGreenPrimary

@Composable
fun ScoreboardCard(
    matchState: MatchState,
    modifier: Modifier = Modifier
) {
    val currentInnings = matchState.currentInnings
    val isChasing = matchState.currentInningsIndex == 2
    val target = matchState.target

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("scoreboard_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            TurfGreenDark,
                            Color(0xFF0D2418),
                            Color(0xFF09160F)
                        )
                    )
                )
                .border(
                    width = 1.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(TurfGreenLight.copy(alpha = 0.5f), Color.Transparent)
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(18.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                // Top bar: Team names & Innings badge
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TurfGreenLight.copy(alpha = 0.2f),
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SportsCricket,
                                    contentDescription = "Batting",
                                    tint = TurfGreenLight,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = currentInnings.battingTeam,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 17.sp
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "🏏 Batting",
                                    color = TurfGreenLight,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Text(
                                text = "vs ${currentInnings.bowlingTeam}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }

                    // Innings Pill
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isChasing) BoundaryGold.copy(alpha = 0.2f) else TurfGreenPrimary.copy(alpha = 0.3f),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isChasing) BoundaryGold.copy(alpha = 0.8f) else TurfGreenLight.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = if (isChasing) "2nd Innings (Chase)" else "1st Innings",
                            color = if (isChasing) BoundaryGold else TurfGreenLight,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Big Score Display: RUNS / WICKETS
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.Bottom) {
                            Text(
                                text = "${currentInnings.totalRuns}",
                                fontSize = 52.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                modifier = Modifier.testTag("score_runs_text")
                            )
                            Text(
                                text = " / ${currentInnings.totalWickets}",
                                fontSize = 34.sp,
                                fontWeight = FontWeight.Bold,
                                color = CricketBallRed,
                                modifier = Modifier.testTag("score_wickets_text")
                            )
                        }
                        Text(
                            text = "Max Wickets: ${currentInnings.maxWickets}",
                            color = Color.White.copy(alpha = 0.5f),
                            fontSize = 11.sp
                        )
                    }

                    // Overs & Run Rate
                    Column(
                        horizontalAlignment = Alignment.End
                    ) {
                        Text(
                            text = "${currentInnings.oversFormatted} / ${currentInnings.maxOvers}.0",
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFFFD54F)
                        )
                        Text(
                            text = "OVERS",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp,
                            color = Color.White.copy(alpha = 0.6f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "CRR: ${currentInnings.currentRunRateFormatted}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TurfGreenLight
                        )
                    }
                }

                // If chasing: Target and Required Run Rate
                if (isChasing && target != null) {
                    val runsNeeded = (target - currentInnings.totalRuns).coerceAtLeast(0)
                    val ballsLeft = ((currentInnings.maxOvers * 6) - currentInnings.legalBalls).coerceAtLeast(0)
                    val rrr = if (ballsLeft > 0) (runsNeeded.toDouble() * 6.0) / ballsLeft else 0.0
                    val rrrStr = String.format(java.util.Locale.US, "%.2f", rrr)

                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = Color.Black.copy(alpha = 0.4f),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BoundaryGold.copy(alpha = 0.4f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Target: $target runs",
                                    color = BoundaryGold,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Need $runsNeeded runs from $ballsLeft balls",
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "RRR: $rrrStr",
                                    color = if (rrr > 10.0) CricketBallRed else Color(0xFFFFD54F),
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Over balls indicator (6 dots for legal balls in current over)
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "This Over Balls:",
                        fontSize = 11.sp,
                        color = Color.White.copy(alpha = 0.6f)
                    )

                    val legalInOver = currentInnings.currentOverBallsCount
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        for (i in 1..6) {
                            val isBowled = i <= legalInOver
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isBowled) TurfGreenLight else Color.White.copy(alpha = 0.2f)
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isBowled) TurfGreenLight else Color.White.copy(alpha = 0.4f),
                                        shape = CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
    }
}
