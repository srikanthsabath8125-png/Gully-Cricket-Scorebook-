package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.InningsData
import com.example.model.MatchState
import com.example.ui.components.BallBadge
import com.example.ui.theme.BoundaryGold
import com.example.ui.theme.CricketBallRed
import com.example.ui.theme.TurfGreenDark
import com.example.ui.theme.TurfGreenLight
import com.example.ui.theme.TurfGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchSummaryScreen(
    matchState: MatchState,
    onRematch: () -> Unit,
    onNewMatch: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val i1 = matchState.innings1
    val i2 = matchState.innings2

    val shareText = buildString {
        appendLine("🏏 GULLY CRICKET MATCH RESULT 🏆")
        appendLine("-------------------------------")
        appendLine("1st Innings: ${i1.battingTeam} - ${i1.totalRuns}/${i1.totalWickets} (${i1.oversFormatted} ov)")
        if (i2 != null) {
            appendLine("2nd Innings: ${i2.battingTeam} - ${i2.totalRuns}/${i2.totalWickets} (${i2.oversFormatted} ov)")
        }
        if (matchState.resultText != null) {
            appendLine("Result: ${matchState.resultText}")
        }
        if (matchState.chasingNarration.isNotEmpty()) {
            appendLine(matchState.chasingNarration)
        }
        appendLine("Scored with Gully Cricket Score App 🏏")
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Match Scorecard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("summary_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            clipboard.setPrimaryClip(ClipData.newPlainText("Gully Cricket Scorecard", shareText))
                            Toast.makeText(context, "Scorecard copied to clipboard! 📋", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.testTag("share_summary_button")
                    ) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share Scorecard")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Trophy Winner Banner
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("winner_banner_card"),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(TurfGreenDark, Color(0xFF0F3822), Color(0xFF091F13))
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(listOf(BoundaryGold, Color.Transparent)),
                            RoundedCornerShape(20.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(
                            imageVector = Icons.Default.EmojiEvents,
                            contentDescription = "Trophy",
                            tint = BoundaryGold,
                            modifier = Modifier.size(48.dp)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = matchState.resultText ?: "Match in Progress",
                            color = BoundaryGold,
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )

                        if (matchState.chasingNarration.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = matchState.chasingNarration,
                                color = Color.White.copy(alpha = 0.9f),
                                fontSize = 13.sp,
                                lineHeight = 18.sp
                            )
                        }

                        if (matchState.toss != null) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Toss: ${matchState.toss.winnerTeam} won and elected to ${matchState.toss.decision.displayName}",
                                color = Color.White.copy(alpha = 0.6f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }

            // Innings 1 Scorecard
            InningsScorecardSection(
                innings = i1,
                target = null,
                isCompleted = i1.isCompleted
            )

            // Innings 2 Scorecard (if started)
            if (i2 != null) {
                InningsScorecardSection(
                    innings = i2,
                    target = matchState.target,
                    isCompleted = i2.isCompleted
                )
            }

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onRematch,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("rematch_button"),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Rematch", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onNewMatch,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("new_match_button"),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("New Match", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun InningsScorecardSection(
    innings: InningsData,
    target: Int?,
    isCompleted: Boolean,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "${innings.inningsNumber}st Innings: ${innings.battingTeam}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Text(
                        text = "Bowled by ${innings.bowlingTeam}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${innings.totalRuns}/${innings.totalWickets}",
                        fontWeight = FontWeight.Black,
                        fontSize = 22.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "${innings.oversFormatted} / ${innings.maxOvers}.0 ov",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            if (target != null) {
                Text(
                    text = "Target: $target runs • CRR: ${innings.currentRunRateFormatted}",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = BoundaryGold
                )
            } else {
                Text(
                    text = "CRR: ${innings.currentRunRateFormatted}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Extras
            val extras = innings.extras
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = MaterialTheme.colorScheme.surface,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Extras: ${extras.totalExtraRuns} (Wd: ${extras.wides}, NB: ${extras.noBalls}, B: ${extras.byes}, LB: ${extras.legByes})",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }

            // Over by Over breakdown
            val oversList = innings.oversBreakdown
            if (oversList.isNotEmpty()) {
                Text(
                    text = "Overs Breakdown:",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    oversList.forEach { over ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "Over ${over.overNumber}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${over.runs} runs • ${over.wickets} wkts",
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .horizontalScroll(rememberScrollState()),
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    over.balls.forEach { ball ->
                                        BallBadge(ball = ball)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
