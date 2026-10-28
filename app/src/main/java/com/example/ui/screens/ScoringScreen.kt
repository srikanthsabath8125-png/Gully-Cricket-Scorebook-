package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MatchState
import com.example.model.WicketType
import com.example.ui.components.ByeLegByeDialog
import com.example.ui.components.ChasingNarrationCard
import com.example.ui.components.NoBallRunsDialog
import com.example.ui.components.OverTimelineRow
import com.example.ui.components.ScoreboardCard
import com.example.ui.components.WideRunsDialog
import com.example.ui.components.WicketDialog
import com.example.ui.theme.BoundaryFourColor
import com.example.ui.theme.BoundaryGold
import com.example.ui.theme.CricketBallRed
import com.example.ui.theme.DotBallColor
import com.example.ui.theme.ExtraBadgeColor
import com.example.ui.theme.SingleRunColor
import com.example.ui.theme.SixerColor
import com.example.ui.theme.TurfGreenDark
import com.example.ui.theme.TurfGreenLight
import com.example.ui.theme.TurfGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringScreen(
    matchState: MatchState,
    showInningsBreakDialog: Boolean,
    showMatchEndDialog: Boolean,
    onAddDot: () -> Unit,
    onAddRuns: (Int) -> Unit,
    onAddWide: (extraRuns: Int) -> Unit,
    onAddNoBall: (runsOffBat: Int) -> Unit,
    onAddWicket: (WicketType, runsCompleted: Int) -> Unit,
    onAddBye: (runs: Int, isLegBye: Boolean) -> Unit,
    onUndoLastBall: () -> Unit,
    onStartSecondInnings: () -> Unit,
    onViewScorecard: () -> Unit,
    onDismissInningsBreak: () -> Unit,
    onDismissMatchEnd: () -> Unit,
    onBack: () -> Unit,
    isPublishingLive: Boolean = false,
    onGoLive: () -> Unit = {},
    onShareLiveMatch: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showWideDialog by remember { mutableStateOf(false) }
    var showNoBallDialog by remember { mutableStateOf(false) }
    var showWicketDialog by remember { mutableStateOf(false) }
    var showByeDialog by remember { mutableStateOf(false) }

    val currentInnings = matchState.currentInnings

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Match Scoreboard",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("scoring_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Home")
                    }
                },
                actions = {
                    // Undo Button
                    IconButton(
                        onClick = onUndoLastBall,
                        modifier = Modifier.testTag("undo_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Undo,
                            contentDescription = "Undo Last Ball",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Scorecard button
                    IconButton(
                        onClick = onViewScorecard,
                        modifier = Modifier.testTag("view_scorecard_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "Full Scorecard",
                            tint = TurfGreenLight
                        )
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
                .padding(horizontal = 14.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Live Match Online Banner
            if (matchState.isLiveShared) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("live_shared_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = TurfGreenDark)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(TurfGreenLight)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "LIVE ONLINE",
                                    color = TurfGreenLight,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 11.sp
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = Color.Black.copy(alpha = 0.4f)
                                ) {
                                    Text(
                                        text = matchState.matchCode,
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 12.sp,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                text = "Ball-by-ball updates broadcasting live",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 11.sp
                            )
                        }

                        Button(
                            onClick = onShareLiveMatch,
                            colors = ButtonDefaults.buttonColors(containerColor = TurfGreenLight),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("share_live_code_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Share,
                                contentDescription = null,
                                tint = Color.Black,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Share Code",
                                color = Color.Black,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            } else {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onGoLive() }
                        .testTag("go_live_banner"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TurfGreenLight.copy(alpha = 0.35f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.SportsCricket,
                            contentDescription = null,
                            tint = TurfGreenLight,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Share Live Score Online",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "Unique code for Team & Opponent to watch",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Button(
                            onClick = onGoLive,
                            enabled = !isPublishingLive,
                            colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("go_live_button")
                        ) {
                            if (isPublishingLive) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(14.dp)
                                )
                            } else {
                                Text("Go Live", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 6. Scoreboard Card: Runs / Wickets, Overs, CRR, RRR, Target
            ScoreboardCard(matchState = matchState)

            // 8. Chasing Runs Narration & Live Commentary
            ChasingNarrationCard(matchState = matchState)

            // 7. Over Timeline: 1, 2, 3, 4, 5, 6, 0Wd, 0NB etc.
            OverTimelineRow(currentInnings = currentInnings)

            // Scoring Buttons Keypad
            Text(
                text = "Scoring Panel",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            // Row 1: 0 (Dot), 1, 2, 3
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreActionButton(
                    text = "0",
                    subText = "Dot",
                    bgColor = DotBallColor,
                    onClick = onAddDot,
                    modifier = Modifier.weight(1f).testTag("score_btn_0")
                )
                ScoreActionButton(
                    text = "1",
                    subText = "Single",
                    bgColor = SingleRunColor,
                    onClick = { onAddRuns(1) },
                    modifier = Modifier.weight(1f).testTag("score_btn_1")
                )
                ScoreActionButton(
                    text = "2",
                    subText = "Double",
                    bgColor = SingleRunColor,
                    onClick = { onAddRuns(2) },
                    modifier = Modifier.weight(1f).testTag("score_btn_2")
                )
                ScoreActionButton(
                    text = "3",
                    subText = "Triple",
                    bgColor = SingleRunColor,
                    onClick = { onAddRuns(3) },
                    modifier = Modifier.weight(1f).testTag("score_btn_3")
                )
            }

            // Row 2: 4 (Four), 5, 6 (Sixer), W (Wicket)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreActionButton(
                    text = "4",
                    subText = "FOUR!",
                    bgColor = BoundaryFourColor,
                    onClick = { onAddRuns(4) },
                    modifier = Modifier.weight(1f).testTag("score_btn_4")
                )
                ScoreActionButton(
                    text = "5",
                    subText = "Runs",
                    bgColor = SingleRunColor,
                    onClick = { onAddRuns(5) },
                    modifier = Modifier.weight(1f).testTag("score_btn_5")
                )
                ScoreActionButton(
                    text = "6",
                    subText = "SIXER!",
                    bgColor = SixerColor,
                    onClick = { onAddRuns(6) },
                    modifier = Modifier.weight(1f).testTag("score_btn_6")
                )
                ScoreActionButton(
                    text = "W",
                    subText = "Out ☝️",
                    bgColor = CricketBallRed,
                    onClick = { showWicketDialog = true },
                    modifier = Modifier.weight(1f).testTag("score_btn_W")
                )
            }

            // Row 3: Dynamic 0Wd/1Wd, WD + Runs..., Dynamic 0NB/1NB, NB + Runs...
            val widePenalty = matchState.config.wideBallRuns
            val noBallPenalty = matchState.config.noBallRuns
            val wideButtonText = "${widePenalty}Wd"
            val wideButtonSub = if (widePenalty == 0) "0 Runs" else "1 Run"
            val nbButtonText = "${noBallPenalty}NB"
            val nbButtonSub = if (noBallPenalty == 0) "0 Runs" else "1 Run"

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ScoreActionButton(
                    text = wideButtonText,
                    subText = wideButtonSub,
                    bgColor = ExtraBadgeColor,
                    onClick = { onAddWide(0) },
                    modifier = Modifier.weight(1f).testTag("score_btn_${wideButtonText}")
                )
                ScoreActionButton(
                    text = "WD+",
                    subText = "+Runs...",
                    bgColor = ExtraBadgeColor.copy(alpha = 0.85f),
                    onClick = { showWideDialog = true },
                    modifier = Modifier.weight(1f).testTag("score_btn_WD_plus")
                )
                ScoreActionButton(
                    text = nbButtonText,
                    subText = nbButtonSub,
                    bgColor = BoundaryGold,
                    textColor = Color.Black,
                    onClick = { onAddNoBall(0) },
                    modifier = Modifier.weight(1f).testTag("score_btn_${nbButtonText}")
                )
                ScoreActionButton(
                    text = "NB+",
                    subText = "0+ Runs",
                    bgColor = BoundaryGold.copy(alpha = 0.85f),
                    textColor = Color.Black,
                    onClick = { showNoBallDialog = true },
                    modifier = Modifier.weight(1f).testTag("score_btn_NB_plus")
                )
            }

            // Row 4: Bye/LegBye, Undo, Full Scorecard
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = { showByeDialog = true },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("score_btn_bye"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Bye / LB", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = onUndoLastBall,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .testTag("score_btn_undo"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Filled.Undo, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Undo", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onViewScorecard,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(48.dp)
                        .testTag("score_btn_scorecard"),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Assessment, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Scorecard", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }

    // Dialogs
    if (showWideDialog) {
        WideRunsDialog(
            widePenalty = matchState.config.wideBallRuns,
            onDismiss = { showWideDialog = false },
            onConfirm = { extraRuns ->
                onAddWide(extraRuns)
                showWideDialog = false
            }
        )
    }

    if (showNoBallDialog) {
        NoBallRunsDialog(
            noBallPenalty = matchState.config.noBallRuns,
            onDismiss = { showNoBallDialog = false },
            onConfirm = { runsOffBat ->
                onAddNoBall(runsOffBat)
                showNoBallDialog = false
            }
        )
    }

    if (showWicketDialog) {
        WicketDialog(
            onDismiss = { showWicketDialog = false },
            onConfirm = { wicketType, runsCompleted ->
                onAddWicket(wicketType, runsCompleted)
                showWicketDialog = false
            }
        )
    }

    if (showByeDialog) {
        ByeLegByeDialog(
            onDismiss = { showByeDialog = false },
            onConfirm = { runs, isLegBye ->
                onAddBye(runs, isLegBye)
                showByeDialog = false
            }
        )
    }

    // Innings Break Dialog
    if (showInningsBreakDialog) {
        val i1 = matchState.innings1
        val target = (i1.totalRuns + 1)
        AlertDialog(
            onDismissRequest = onDismissInningsBreak,
            icon = {
                Icon(imageVector = Icons.Default.Refresh, contentDescription = null, tint = TurfGreenLight, modifier = Modifier.size(36.dp))
            },
            title = {
                Text("Innings 1 Complete! 🏏", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "${i1.battingTeam} finished at ${i1.totalRuns}/${i1.totalWickets} in ${i1.oversFormatted} overs.",
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "🎯 Target for ${i1.bowlingTeam}: $target runs in ${matchState.config.totalOvers} overs (RRR: ${String.format(java.util.Locale.US, "%.2f", (target * 6.0) / (matchState.config.totalOvers * 6))}).",
                        fontSize = 14.sp,
                        color = BoundaryGold,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Ready to start the 2nd innings chase?",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onStartSecondInnings,
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary),
                    modifier = Modifier.testTag("start_chase_button")
                ) {
                    Text("Start 2nd Innings Chase 🚀")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissInningsBreak) {
                    Text("Later")
                }
            }
        )
    }

    // Match End Dialog
    if (showMatchEndDialog) {
        AlertDialog(
            onDismissRequest = onDismissMatchEnd,
            icon = {
                Icon(imageVector = Icons.Default.EmojiEvents, contentDescription = null, tint = BoundaryGold, modifier = Modifier.size(40.dp))
            },
            title = {
                Text("Match Finished! 🏆", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = matchState.resultText ?: "Match Complete",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = TurfGreenLight
                    )
                    Text(
                        text = matchState.chasingNarration,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onDismissMatchEnd()
                        onViewScorecard()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary),
                    modifier = Modifier.testTag("view_final_scorecard_button")
                ) {
                    Text("View Full Scorecard 📊")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismissMatchEnd) {
                    Text("Close")
                }
            }
        )
    }
}

@Composable
fun ScoreActionButton(
    text: String,
    subText: String,
    bgColor: Color,
    textColor: Color = Color.White,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(58.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = bgColor,
        shadowElevation = 3.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = text,
                color = textColor,
                fontWeight = FontWeight.Black,
                fontSize = 17.sp
            )
            Text(
                text = subText,
                color = textColor.copy(alpha = 0.85f),
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
        }
    }
}
