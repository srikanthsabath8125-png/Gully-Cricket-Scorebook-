package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.MatchState
import com.example.ui.theme.BoundaryGold
import com.example.ui.theme.CricketBallRed
import com.example.ui.theme.TurfGreenDark
import com.example.ui.theme.TurfGreenLight
import com.example.ui.theme.TurfGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    activeMatch: MatchState?,
    onResumeActiveMatch: () -> Unit,
    onDiscardActiveMatch: () -> Unit,
    onStartNewMatch: (overs: Int) -> Unit,
    onViewHistory: () -> Unit,
    onWatchLiveMatch: (code: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showRulesModal by remember { mutableStateOf(false) }
    var showEnterCodeDialog by remember { mutableStateOf(false) }
    var enteredCode by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = TurfGreenLight.copy(alpha = 0.2f),
                            modifier = Modifier.size(34.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.SportsCricket,
                                    contentDescription = "App Icon",
                                    tint = TurfGreenLight,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "Gully Cricket",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onViewHistory,
                        modifier = Modifier.testTag("history_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Match History",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                    IconButton(
                        onClick = { showRulesModal = true },
                        modifier = Modifier.testTag("rules_icon_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = "Gully Rules",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
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
            // In-Progress Match Recovery Card (survives app closure / crash!)
            if (activeMatch != null && !activeMatch.isFinished) {
                ActiveMatchRecoveryCard(
                    matchState = activeMatch,
                    onResume = onResumeActiveMatch,
                    onDiscard = onDiscardActiveMatch
                )
            }

            // Hero Stadium Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("hero_banner_card"),
                shape = RoundedCornerShape(22.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    TurfGreenDark,
                                    Color(0xFF0F3822),
                                    Color(0xFF071B11)
                                )
                            )
                        )
                        .border(
                            1.dp,
                            Brush.linearGradient(
                                colors = listOf(TurfGreenLight.copy(alpha = 0.6f), Color.Transparent)
                            ),
                            RoundedCornerShape(22.dp)
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = BoundaryGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BoundaryGold.copy(alpha = 0.6f))
                        ) {
                            Text(
                                text = "STREET & GULLY SCORER",
                                color = BoundaryGold,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.2.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = "Gully Cricket Scorebook",
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = Color.White
                        )

                        Text(
                            text = "Score matches effortlessly with coin toss, custom extras (0Wd, 0NB, WD+runs, NB+runs), and live chasing narration!",
                            fontSize = 13.sp,
                            color = Color.White.copy(alpha = 0.8f),
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                        )

                        Button(
                            onClick = { onStartNewMatch(5) },
                            colors = ButtonDefaults.buttonColors(containerColor = TurfGreenLight),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("start_match_hero_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = Color.Black
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Start a Match",
                                color = Color.Black,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                        }
                    }
                }
            }

            // Watch Live Match Card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showEnterCodeDialog = true }
                    .testTag("watch_live_match_card"),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)),
                border = androidx.compose.foundation.BorderStroke(1.dp, TurfGreenLight.copy(alpha = 0.5f))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = CircleShape,
                        color = CricketBallRed.copy(alpha = 0.15f),
                        modifier = Modifier.size(46.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = Icons.Default.SportsCricket,
                                contentDescription = null,
                                tint = CricketBallRed,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Watch Live Match",
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Surface(
                                shape = RoundedCornerShape(4.dp),
                                color = CricketBallRed
                            ) {
                                Text(
                                    text = "LIVE ONLINE",
                                    color = Color.White,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = "Enter match code to view live ball-to-ball scores",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = { showEnterCodeDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary),
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.testTag("enter_code_button")
                    ) {
                        Text("Enter Code", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Text(
                text = "Quick Match Formats",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Quick Format Cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormatCard(
                    title = "2-Over Blast",
                    subtitle = "Super Over shootout",
                    badge = "⚡ Fast",
                    badgeColor = BoundaryGold,
                    overs = 2,
                    onClick = { onStartNewMatch(2) },
                    modifier = Modifier.weight(1f).testTag("quick_format_2overs")
                )

                FormatCard(
                    title = "5-Over Match",
                    subtitle = "Standard gully game",
                    badge = "🔥 Popular",
                    badgeColor = TurfGreenLight,
                    overs = 5,
                    onClick = { onStartNewMatch(5) },
                    modifier = Modifier.weight(1f).testTag("quick_format_5overs")
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                FormatCard(
                    title = "8-Over Match",
                    subtitle = "Weekend derby clash",
                    badge = "🏏 T8",
                    badgeColor = Color(0xFF64B5F6),
                    overs = 8,
                    onClick = { onStartNewMatch(8) },
                    modifier = Modifier.weight(1f).testTag("quick_format_8overs")
                )

                FormatCard(
                    title = "10-Over Trophy",
                    subtitle = "Tournament mode",
                    badge = "🏆 Full",
                    badgeColor = CricketBallRed,
                    overs = 10,
                    onClick = { onStartNewMatch(10) },
                    modifier = Modifier.weight(1f).testTag("quick_format_10overs")
                )
            }

            // Past Matches & History Button
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onViewHistory() }
                    .testTag("view_history_card"),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Default.History,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Match History & In-Progress Games",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Review past scores or resume unfinished matches",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Open",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showEnterCodeDialog) {
        AlertDialog(
            onDismissRequest = {
                showEnterCodeDialog = false
                enteredCode = ""
            },
            title = {
                Text(
                    text = "Watch Live Match",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column {
                    Text(
                        text = "Enter the unique 6-character Match Code shared by the scorer (e.g. GC5823):",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = enteredCode,
                        onValueChange = { input ->
                            enteredCode = input.uppercase().take(8)
                        },
                        label = { Text("Match Code") },
                        placeholder = { Text("GCxxxx") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("match_code_input")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val code = enteredCode.trim()
                        if (code.isNotBlank()) {
                            showEnterCodeDialog = false
                            enteredCode = ""
                            onWatchLiveMatch(code)
                        }
                    },
                    enabled = enteredCode.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary),
                    modifier = Modifier.testTag("submit_match_code_button")
                ) {
                    Text("Watch Live")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showEnterCodeDialog = false
                        enteredCode = ""
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }

    if (showRulesModal) {
        AlertDialog(
            onDismissRequest = { showRulesModal = false },
            title = {
                Text(
                    text = "🏏 Gully Cricket Rules",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("• In-Progress Auto-Save: Matches are continuously saved on every ball bowled! Even if the app closes or is killed, it remains stored and can be resumed from Match History.", fontSize = 13.sp)
                    Text("• Wide Ball: 0 Run or 1 Run penalty option, ball re-bowled. Extra runs added via 'WD+ Runs'.", fontSize = 13.sp)
                    Text("• No Ball: 0 Run or 1 Run penalty option, ball re-bowled. Extra runs hit off bat added via 'NB+ Runs' (starting from 0+ runs).", fontSize = 13.sp)
                    Text("• Wickets: Bowled, Caught, Run Out, Stumped, Hit Wicket, LBW.", fontSize = 13.sp)
                    Text("• Undo: Revert any mistaken ball with instant state rollback.", fontSize = 13.sp)
                }
            },
            confirmButton = {
                Button(onClick = { showRulesModal = false }) {
                    Text("Got It!")
                }
            }
        )
    }
}

@Composable
fun ActiveMatchRecoveryCard(
    matchState: MatchState,
    onResume: () -> Unit,
    onDiscard: () -> Unit,
    modifier: Modifier = Modifier
) {
    val currentInnings = matchState.currentInnings
    var showDiscardConfirm by remember { mutableStateOf(false) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("active_match_recovery_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF14241B)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, TurfGreenLight)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = TurfGreenLight.copy(alpha = 0.2f),
                    border = androidx.compose.foundation.BorderStroke(1.dp, TurfGreenLight)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(TurfGreenLight)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "LIVE MATCH IN PROGRESS",
                            color = TurfGreenLight,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                IconButton(
                    onClick = { showDiscardConfirm = true },
                    modifier = Modifier.size(28.dp).testTag("discard_active_icon_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Discard Match", tint = Color.White.copy(alpha = 0.6f))
                }
            }

            Text(
                text = "${matchState.innings1.battingTeam} vs ${matchState.innings1.bowlingTeam}",
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = Color.White
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentInnings.battingTeam}: ${currentInnings.totalRuns}/${currentInnings.totalWickets} (${currentInnings.oversFormatted} ov)",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black,
                    color = BoundaryGold
                )

                Text(
                    text = "Overs: ${matchState.config.totalOvers}",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.7f)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.weight(1f).height(44.dp).testTag("resume_active_match_button")
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Resume Scoring", fontWeight = FontWeight.Bold)
                }

                OutlinedButton(
                    onClick = { showDiscardConfirm = true },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(44.dp).testTag("discard_active_button")
                ) {
                    Text("Discard", color = CricketBallRed)
                }
            }
        }
    }

    if (showDiscardConfirm) {
        AlertDialog(
            onDismissRequest = { showDiscardConfirm = false },
            title = { Text("Discard In-Progress Match?") },
            text = { Text("This match is currently in progress. Discarding will remove it from active scoring.") },
            confirmButton = {
                Button(
                    onClick = {
                        showDiscardConfirm = false
                        onDiscard()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CricketBallRed)
                ) {
                    Text("Discard Match")
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDiscardConfirm = false }) {
                    Text("Keep Match")
                }
            }
        )
    }
}

@Composable
fun FormatCard(
    title: String,
    subtitle: String,
    badge: String,
    badgeColor: Color,
    overs: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = badgeColor.copy(alpha = 0.2f)
                ) {
                    Text(
                        text = badge,
                        color = badgeColor,
                        fontWeight = FontWeight.Bold,
                        fontSize = 10.sp,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
                Text(
                    text = "${overs} ov",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
