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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.SportsCricket
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
import com.example.model.CoinSide
import com.example.model.MatchConfig
import com.example.model.TossDecision
import com.example.model.TossResult
import com.example.ui.components.CoinFlipView
import com.example.ui.theme.BoundaryGold
import com.example.ui.theme.TurfGreenLight
import com.example.ui.theme.TurfGreenPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TossScreen(
    matchConfig: MatchConfig,
    isFlipping: Boolean,
    tossResult: TossResult?,
    onFlipCoin: (callerTeam: String, calledSide: CoinSide) -> Unit,
    onSelectDecision: (TossDecision) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedCaller by remember { mutableStateOf(matchConfig.team1) }
    var selectedCall by remember { mutableStateOf(CoinSide.HEADS) }
    var selectedDecision by remember { mutableStateOf<TossDecision?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Match Toss",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        enabled = !isFlipping,
                        modifier = Modifier.testTag("toss_back_button")
                    ) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Match Teams Banner
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = matchConfig.team1,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "VS",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        color = BoundaryGold
                    )
                    Text(
                        text = matchConfig.team2,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            // Coin Flip Widget
            Spacer(modifier = Modifier.height(10.dp))
            CoinFlipView(
                isFlipping = isFlipping,
                landedSide = tossResult?.landedSide,
                modifier = Modifier
                    .size(170.dp)
                    .testTag("coin_flip_view")
            )
            Spacer(modifier = Modifier.height(10.dp))

            if (tossResult == null) {
                // Step 4: Who calls and Heads or Tails
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        Text(
                            text = "Who calls the toss?",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(matchConfig.team1, matchConfig.team2).forEach { team ->
                                val isSelected = selectedCaller == team
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedCaller = team }
                                        .testTag("caller_team_${team.replace(" ", "_")}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) TurfGreenPrimary else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) TurfGreenLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = team,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }

                        Text(
                            text = "$selectedCaller calls:",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(CoinSide.HEADS, CoinSide.TAILS).forEach { side ->
                                val isSelected = selectedCall == side
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(10.dp))
                                        .clickable { selectedCall = side }
                                        .testTag("toss_call_${side.name}"),
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isSelected) BoundaryGold else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) Color(0xFFFF8F00) else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Box(
                                        modifier = Modifier.padding(vertical = 12.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = side.displayName,
                                            fontWeight = FontWeight.Bold,
                                            color = if (isSelected) Color.Black else MaterialTheme.colorScheme.onSurface,
                                            fontSize = 15.sp
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Button(
                    onClick = { onFlipCoin(selectedCaller, selectedCall) },
                    enabled = !isFlipping,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("flip_coin_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.Casino, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isFlipping) "Flipping Coin..." else "FLIP COIN!",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            } else {
                // Step 5: Toss Winner announced & Batting or Bowling Decision
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, BoundaryGold)
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = BoundaryGold.copy(alpha = 0.2f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BoundaryGold)
                        ) {
                            Text(
                                text = "🪙 RESULT: IT'S ${tossResult.landedSide.displayName.uppercase()}!",
                                color = BoundaryGold,
                                fontWeight = FontWeight.Black,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "${tossResult.winnerTeam} won the toss!",
                            fontWeight = FontWeight.Black,
                            fontSize = 20.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = "Choose to Bat or Bowl first:",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(TossDecision.BAT, TossDecision.BOWL).forEach { decision ->
                                val isSelected = selectedDecision == decision
                                Surface(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clip(RoundedCornerShape(12.dp))
                                        .clickable { selectedDecision = decision }
                                        .testTag("decision_${decision.name}"),
                                    shape = RoundedCornerShape(12.dp),
                                    color = if (isSelected) TurfGreenPrimary else MaterialTheme.colorScheme.surface,
                                    border = androidx.compose.foundation.BorderStroke(
                                        1.dp,
                                        if (isSelected) TurfGreenLight else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                    )
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 14.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = if (decision == TossDecision.BAT) "🏏 Bat First" else "⚾ Bowl First",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 15.sp,
                                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }

                        AnimatedVisibility(visible = selectedDecision != null) {
                            val decisionText = if (selectedDecision == TossDecision.BAT) "Batting first" else "Bowling first"
                            Text(
                                text = "👉 ${tossResult.winnerTeam} elected to $decisionText!",
                                color = TurfGreenLight,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }
                }

                Button(
                    onClick = {
                        selectedDecision?.let { onSelectDecision(it) }
                    },
                    enabled = selectedDecision != null,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                        .testTag("start_scoring_button"),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary)
                ) {
                    Icon(imageVector = Icons.Default.SportsCricket, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Start Scoring Match",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }

                OutlinedButton(
                    onClick = {
                        onFlipCoin(selectedCaller, selectedCall)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("reflip_button")
                ) {
                    Text("Re-flip Coin")
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}
