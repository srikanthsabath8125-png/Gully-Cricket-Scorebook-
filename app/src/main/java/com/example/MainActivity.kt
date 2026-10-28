package com.example

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SportsCricket
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import com.example.auth.GoogleAuthHelper
import com.example.model.MatchState
import com.example.ui.screens.HistoryScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LiveSpectatorScreen
import com.example.ui.screens.MatchSummaryScreen
import com.example.ui.screens.ScoringScreen
import com.example.ui.screens.SetupScreen
import com.example.ui.screens.TossScreen
import com.example.ui.theme.GullyCricketTheme
import com.example.ui.theme.TurfGreenPrimary
import com.example.viewmodel.AppScreen
import com.example.viewmodel.CricketViewModel
import com.google.firebase.Firebase
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {
    private val viewModel: CricketViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GullyCricketTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    CricketApp(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun CricketApp(viewModel: CricketViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val currentScreen by viewModel.currentScreen.collectAsState()
    val matchConfig by viewModel.matchConfig.collectAsState()
    val matchState by viewModel.matchState.collectAsState()
    val activeMatch by viewModel.activeMatchState.collectAsState()
    val isFlipping by viewModel.isCoinFlipping.collectAsState()
    val tossResult by viewModel.tossResult.collectAsState()
    val showInningsBreak by viewModel.showInningsBreakDialog.collectAsState()
    val showMatchEnd by viewModel.showMatchEndDialog.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    // Spectator & Live state
    val liveSpectatorMatch by viewModel.liveSpectatorMatch.collectAsState()
    val isSpectatorLoading by viewModel.isSpectatorLoading.collectAsState()
    val spectatorError by viewModel.spectatorError.collectAsState()
    val spectatorMatchCode by viewModel.spectatorMatchCode.collectAsState()
    val isPublishingLive by viewModel.isPublishingLive.collectAsState()

    var showAuthDialog by remember { mutableStateOf(false) }
    var authErrorMessage by remember { mutableStateOf<String?>(null) }
    var isAuthLoading by remember { mutableStateOf(false) }

    // Attempt silent auto-sign-in on app start
    LaunchedEffect(Unit) {
        GoogleAuthHelper.attemptAutoSignIn(
            context = context,
            credentialManager = credentialManager,
            onAuthSuccess = {},
            onUnauthenticated = {},
            scope = coroutineScope
        )
    }

    when (currentScreen) {
        AppScreen.HOME -> {
            HomeScreen(
                activeMatch = activeMatch,
                onResumeActiveMatch = {
                    viewModel.resumeActiveMatch()
                },
                onDiscardActiveMatch = {
                    viewModel.discardActiveMatch()
                },
                onStartNewMatch = { overs ->
                    viewModel.startNewMatchSetup(overs)
                },
                onViewHistory = {
                    viewModel.navigateTo(AppScreen.HISTORY)
                },
                onWatchLiveMatch = { code ->
                    val user = Firebase.auth.currentUser
                    if (user == null) {
                        // Sign in first before querying Firestore (per zero-trust auth gating)
                        GoogleAuthHelper.signInWithGoogle(
                            activity = context as Activity,
                            credentialManager = credentialManager,
                            scope = coroutineScope,
                            onAuthSuccess = {
                                viewModel.watchLiveMatch(code)
                            },
                            onError = { err ->
                                Toast.makeText(context, "Sign-in required to watch live: $err", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        viewModel.watchLiveMatch(code)
                    }
                }
            )
        }

        AppScreen.SETUP -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.HOME)
            }
            SetupScreen(
                initialConfig = matchConfig,
                onBack = {
                    viewModel.navigateTo(AppScreen.HOME)
                },
                onProceedToToss = { config ->
                    viewModel.proceedToToss(config)
                }
            )
        }

        AppScreen.TOSS -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.SETUP)
            }
            TossScreen(
                matchConfig = matchConfig,
                isFlipping = isFlipping,
                tossResult = tossResult,
                onFlipCoin = { caller, side ->
                    viewModel.flipCoin(caller, side)
                },
                onSelectDecision = { decision ->
                    viewModel.selectTossDecision(decision)
                },
                onBack = {
                    viewModel.navigateTo(AppScreen.SETUP)
                }
            )
        }

        AppScreen.SCORING -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.HOME)
            }
            ScoringScreen(
                matchState = matchState,
                showInningsBreakDialog = showInningsBreak,
                showMatchEndDialog = showMatchEnd,
                isPublishingLive = isPublishingLive,
                onAddDot = { viewModel.addDotBall() },
                onAddRuns = { runs -> viewModel.addRuns(runs) },
                onAddWide = { extraRuns -> viewModel.addWide(extraRuns) },
                onAddNoBall = { runsOffBat -> viewModel.addNoBall(runsOffBat) },
                onAddWicket = { type, runs -> viewModel.addWicket(type, runs) },
                onAddBye = { runs, isLegBye -> viewModel.addBye(runs, isLegBye) },
                onUndoLastBall = { viewModel.undoLastBall() },
                onStartSecondInnings = { viewModel.startSecondInnings() },
                onViewScorecard = { viewModel.navigateTo(AppScreen.MATCH_SUMMARY) },
                onDismissInningsBreak = { viewModel.dismissInningsBreakDialog() },
                onDismissMatchEnd = { viewModel.dismissMatchEndDialog() },
                onGoLive = {
                    val user = Firebase.auth.currentUser
                    if (user != null) {
                        viewModel.enableLiveSharing(
                            scorerUid = user.uid,
                            onSuccess = { code ->
                                shareMatchMessage(context, code, matchState)
                            },
                            onError = { err ->
                                Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                            }
                        )
                    } else {
                        showAuthDialog = true
                    }
                },
                onShareLiveMatch = {
                    shareMatchMessage(context, matchState.matchCode, matchState)
                },
                onBack = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }

        AppScreen.MATCH_SUMMARY -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.SCORING)
            }
            MatchSummaryScreen(
                matchState = matchState,
                onRematch = { viewModel.rematchSameTeams() },
                onNewMatch = { viewModel.startNewMatchSetup() },
                onBack = { viewModel.navigateTo(AppScreen.SCORING) }
            )
        }

        AppScreen.HISTORY -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.HOME)
            }
            HistoryScreen(
                historyList = historyList,
                onResumeMatch = { matchId ->
                    viewModel.resumeMatchById(matchId)
                },
                onDeleteMatch = { matchId ->
                    viewModel.deleteMatch(matchId)
                },
                onClearAll = { viewModel.clearAllHistory() },
                onBack = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }

        AppScreen.LIVE_SPECTATOR -> {
            BackHandler {
                viewModel.navigateTo(AppScreen.HOME)
            }
            LiveSpectatorScreen(
                matchCode = spectatorMatchCode,
                liveMatch = liveSpectatorMatch,
                isLoading = isSpectatorLoading,
                errorMessage = spectatorError,
                onRefresh = { viewModel.refreshSpectatorMatch() },
                onBack = { viewModel.navigateTo(AppScreen.HOME) }
            )
        }
    }

    // Google Sign-In Dialog for Live Sharing
    if (showAuthDialog) {
        AlertDialog(
            onDismissRequest = {
                showAuthDialog = false
                authErrorMessage = null
            },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.SportsCricket,
                        contentDescription = null,
                        tint = TurfGreenPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sign in to Broadcast Live",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sign in with your Google account to broadcast live scores online. A unique match code will be generated to share with your team & opponent!",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (authErrorMessage != null) {
                        Text(
                            text = authErrorMessage ?: "",
                            color = MaterialTheme.colorScheme.error,
                            fontSize = 12.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        isAuthLoading = true
                        authErrorMessage = null
                        GoogleAuthHelper.signInWithGoogle(
                            activity = context as Activity,
                            credentialManager = credentialManager,
                            scope = coroutineScope,
                            onAuthSuccess = {
                                isAuthLoading = false
                                showAuthDialog = false
                                val user = Firebase.auth.currentUser
                                if (user != null) {
                                    viewModel.enableLiveSharing(
                                        scorerUid = user.uid,
                                        onSuccess = { code ->
                                            shareMatchMessage(context, code, matchState)
                                        },
                                        onError = { err ->
                                            Toast.makeText(context, err, Toast.LENGTH_LONG).show()
                                        }
                                    )
                                }
                            },
                            onError = { err ->
                                isAuthLoading = false
                                authErrorMessage = err
                            },
                            onCancelled = {
                                isAuthLoading = false
                            }
                        )
                    },
                    enabled = !isAuthLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = TurfGreenPrimary),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("google_sign_in_button")
                ) {
                    if (isAuthLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Sign in with Google", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showAuthDialog = false
                        authErrorMessage = null
                    }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

private fun shareMatchMessage(context: Context, code: String, state: MatchState) {
    val currentInn = state.currentInnings
    val message = """
    🏏 *Watch Gully Cricket LIVE!*
    *${state.config.team1} vs ${state.config.team2}*
    Score: ${currentInn.totalRuns}/${currentInn.totalWickets} (${currentInn.oversFormatted} ov)
    
    👉 Open the Gully Cricket app, tap *Watch Live Match*, and enter Code:
    *${code}*
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, message)
    }
    context.startActivity(Intent.createChooser(intent, "Share Live Match Code"))
}
