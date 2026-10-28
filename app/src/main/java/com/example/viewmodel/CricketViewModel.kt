package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.MatchHistoryStorage
import com.example.engine.NarrationEngine
import com.example.model.BallRecord
import com.example.model.CoinSide
import com.example.model.InningsData
import com.example.model.MatchConfig
import com.example.model.MatchState
import com.example.model.SavedMatchSummary
import com.example.model.TossDecision
import com.example.model.TossResult
import com.example.model.WicketType
import com.example.model.toLiveMatch
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.random.Random

enum class AppScreen {
    HOME,
    SETUP,
    TOSS,
    SCORING,
    MATCH_SUMMARY,
    HISTORY,
    LIVE_SPECTATOR
}

class CricketViewModel(application: Application) : AndroidViewModel(application) {

    private val storage = MatchHistoryStorage(application.applicationContext)
    private val liveRepository = com.example.data.LiveMatchRepository(application.applicationContext)

    private val _currentScreen = MutableStateFlow(AppScreen.HOME)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _matchConfig = MutableStateFlow(MatchConfig())
    val matchConfig: StateFlow<MatchConfig> = _matchConfig.asStateFlow()

    private val _matchState = MutableStateFlow(MatchState())
    val matchState: StateFlow<MatchState> = _matchState.asStateFlow()

    private val _activeMatchState = MutableStateFlow<MatchState?>(null)
    val activeMatchState: StateFlow<MatchState?> = _activeMatchState.asStateFlow()

    private val _isCoinFlipping = MutableStateFlow(false)
    val isCoinFlipping: StateFlow<Boolean> = _isCoinFlipping.asStateFlow()

    private val _tossResult = MutableStateFlow<TossResult?>(null)
    val tossResult: StateFlow<TossResult?> = _tossResult.asStateFlow()

    private val _showInningsBreakDialog = MutableStateFlow(false)
    val showInningsBreakDialog: StateFlow<Boolean> = _showInningsBreakDialog.asStateFlow()

    private val _showMatchEndDialog = MutableStateFlow(false)
    val showMatchEndDialog: StateFlow<Boolean> = _showMatchEndDialog.asStateFlow()

    private val _historyList = MutableStateFlow<List<SavedMatchSummary>>(emptyList())
    val historyList: StateFlow<List<SavedMatchSummary>> = _historyList.asStateFlow()

    // Live Spectator state
    private val _liveSpectatorMatch = MutableStateFlow<com.example.model.LiveMatch?>(null)
    val liveSpectatorMatch: StateFlow<com.example.model.LiveMatch?> = _liveSpectatorMatch.asStateFlow()

    private val _isSpectatorLoading = MutableStateFlow(false)
    val isSpectatorLoading: StateFlow<Boolean> = _isSpectatorLoading.asStateFlow()

    private val _spectatorError = MutableStateFlow<String?>(null)
    val spectatorError: StateFlow<String?> = _spectatorError.asStateFlow()

    private val _spectatorMatchCode = MutableStateFlow("")
    val spectatorMatchCode: StateFlow<String> = _spectatorMatchCode.asStateFlow()

    private val _isPublishingLive = MutableStateFlow(false)
    val isPublishingLive: StateFlow<Boolean> = _isPublishingLive.asStateFlow()

    private var spectatorJob: kotlinx.coroutines.Job? = null

    init {
        loadHistory()
        checkActiveMatch()
    }

    fun loadHistory() {
        _historyList.value = storage.getSavedMatches()
    }

    fun checkActiveMatch() {
        val active = storage.getActiveMatch()
        if (active != null && !active.isFinished) {
            _activeMatchState.value = active
        } else {
            _activeMatchState.value = null
        }
    }

    fun navigateTo(screen: AppScreen) {
        if (screen == AppScreen.HISTORY) {
            loadHistory()
        }
        _currentScreen.value = screen
    }

    fun updateConfig(config: MatchConfig) {
        _matchConfig.value = config
    }

    fun startNewMatchSetup(overs: Int = 5) {
        _matchConfig.value = MatchConfig(totalOvers = overs)
        _tossResult.value = null
        _showInningsBreakDialog.value = false
        _showMatchEndDialog.value = false
        _currentScreen.value = AppScreen.SETUP
    }

    fun proceedToToss(config: MatchConfig) {
        _matchConfig.value = config
        _tossResult.value = null
        _currentScreen.value = AppScreen.TOSS
    }

    fun flipCoin(callerTeam: String, calledSide: CoinSide) {
        viewModelScope.launch {
            _isCoinFlipping.value = true
            delay(1600)
            val landed = if (Random.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
            val won = calledSide == landed
            val otherTeam = if (callerTeam == _matchConfig.value.team1) _matchConfig.value.team2 else _matchConfig.value.team1
            val winner = if (won) callerTeam else otherTeam

            _tossResult.value = TossResult(
                callerTeam = callerTeam,
                calledSide = calledSide,
                landedSide = landed,
                winnerTeam = winner,
                decision = TossDecision.BAT
            )
            _isCoinFlipping.value = false
        }
    }

    fun selectTossDecision(decision: TossDecision) {
        val toss = _tossResult.value ?: return
        val updatedToss = toss.copy(decision = decision)
        _tossResult.value = updatedToss

        val config = _matchConfig.value
        val (battingFirst, bowlingFirst) = if (decision == TossDecision.BAT) {
            val other = if (updatedToss.winnerTeam == config.team1) config.team2 else config.team1
            Pair(updatedToss.winnerTeam, other)
        } else {
            val other = if (updatedToss.winnerTeam == config.team1) config.team2 else config.team1
            Pair(other, updatedToss.winnerTeam)
        }

        val initialInnings1 = InningsData(
            inningsNumber = 1,
            battingTeam = battingFirst,
            bowlingTeam = bowlingFirst,
            maxOvers = config.totalOvers,
            maxWickets = config.wicketsPerSide
        )

        val newMatchState = MatchState(
            config = config,
            toss = updatedToss,
            currentInningsIndex = 1,
            innings1 = initialInnings1,
            innings2 = null,
            isFinished = false,
            lastBallCommentary = "${updatedToss.winnerTeam} won the toss and elected to ${decision.displayName}. Match begins!",
            chasingNarration = ""
        )
        
        _matchState.value = newMatchState
        _activeMatchState.value = newMatchState
        
        // Persist immediately: match shows in history the second it starts!
        storage.saveActiveMatch(newMatchState)
        loadHistory()

        _currentScreen.value = AppScreen.SCORING
    }

    // --- Resume & Recovery Actions ---

    fun resumeActiveMatch() {
        val active = _activeMatchState.value ?: storage.getActiveMatch()
        if (active != null) {
            _matchState.value = active
            _matchConfig.value = active.config
            _tossResult.value = active.toss
            _activeMatchState.value = active
            _currentScreen.value = AppScreen.SCORING
        }
    }

    fun resumeMatchById(matchId: String) {
        val item = _historyList.value.find { it.id == matchId }
        val state = item?.stateJson?.let { storage.jsonToMatchState(it) }
            ?: (if (_activeMatchState.value?.matchId == matchId) _activeMatchState.value else null)

        if (state != null) {
            _matchState.value = state
            _matchConfig.value = state.config
            _tossResult.value = state.toss
            if (!state.isFinished) {
                _activeMatchState.value = state
                _currentScreen.value = AppScreen.SCORING
            } else {
                _currentScreen.value = AppScreen.MATCH_SUMMARY
            }
        }
    }

    fun discardActiveMatch() {
        val active = _activeMatchState.value ?: storage.getActiveMatch()
        if (active != null) {
            storage.deleteMatch(active.matchId)
        }
        storage.clearActiveMatch()
        _activeMatchState.value = null
        loadHistory()
    }

    fun deleteMatch(matchId: String) {
        storage.deleteMatch(matchId)
        if (_activeMatchState.value?.matchId == matchId) {
            _activeMatchState.value = null
        }
        loadHistory()
    }

    // --- Scoring Actions ---

    fun addDotBall() {
        recordBall(
            displayText = "0",
            totalRuns = 0,
            isLegal = true,
            isWicket = false
        )
    }

    fun addRuns(runs: Int) {
        recordBall(
            displayText = runs.toString(),
            totalRuns = runs,
            isLegal = true,
            isWicket = false
        )
    }

    fun addWide(extraRuns: Int = 0) {
        val widePenalty = _matchState.value.config.wideBallRuns
        val totalRuns = widePenalty + extraRuns
        val display = if (extraRuns == 0) "${widePenalty}Wd" else "Wd+$extraRuns"
        recordBall(
            displayText = display,
            totalRuns = totalRuns,
            isLegal = false,
            isWicket = false
        )
    }

    fun addNoBall(runsOffBat: Int = 0) {
        val nbPenalty = _matchState.value.config.noBallRuns
        val totalRuns = nbPenalty + runsOffBat
        val display = if (runsOffBat == 0) "${nbPenalty}NB" else "NB+$runsOffBat"
        recordBall(
            displayText = display,
            totalRuns = totalRuns,
            isLegal = false,
            isWicket = false
        )
    }

    fun addWicket(type: WicketType, runsCompleted: Int = 0) {
        val display = if (runsCompleted > 0) "W+$runsCompleted" else "W"
        recordBall(
            displayText = display,
            totalRuns = runsCompleted,
            isLegal = true,
            isWicket = true,
            wicketType = type
        )
    }

    fun addBye(runs: Int, isLegBye: Boolean = false) {
        val prefix = if (isLegBye) "LB" else "B"
        recordBall(
            displayText = "$prefix$runs",
            totalRuns = runs,
            isLegal = true,
            isWicket = false
        )
    }

    private fun recordBall(
        displayText: String,
        totalRuns: Int,
        isLegal: Boolean,
        isWicket: Boolean,
        wicketType: WicketType? = null
    ) {
        val state = _matchState.value
        if (state.isFinished) return

        val activeInnings = state.currentInnings
        val currentLegalBalls = activeInnings.legalBalls
        val nextLegalBalls = if (isLegal) currentLegalBalls + 1 else currentLegalBalls
        val nextTotalRuns = activeInnings.totalRuns + totalRuns
        val nextTotalWickets = if (isWicket) activeInnings.totalWickets + 1 else activeInnings.totalWickets

        val overNum = (currentLegalBalls / 6) + 1
        val ballInOver = if (isLegal) (currentLegalBalls % 6) + 1 else (currentLegalBalls % 6)

        val ballRecord = BallRecord(
            inningsNumber = state.currentInningsIndex,
            overNumber = overNum,
            ballNumberInOver = ballInOver,
            displayText = displayText,
            totalRuns = totalRuns,
            isLegalBall = isLegal,
            isWicket = isWicket,
            wicketType = wicketType
        )

        val updatedBalls = activeInnings.balls + ballRecord
        val commentary = NarrationEngine.generateBallCommentary(
            ball = ballRecord,
            isChasing = state.currentInningsIndex == 2
        )

        val maxBalls = activeInnings.maxOvers * 6
        val isInningsOver = nextLegalBalls >= maxBalls || nextTotalWickets >= activeInnings.maxWickets

        val nextMatchState = if (state.currentInningsIndex == 1) {
            val updatedInnings1 = activeInnings.copy(
                totalRuns = nextTotalRuns,
                totalWickets = nextTotalWickets,
                legalBalls = nextLegalBalls,
                balls = updatedBalls,
                isCompleted = isInningsOver
            )

            if (isInningsOver) {
                val target = nextTotalRuns + 1
                val initialInnings2 = InningsData(
                    inningsNumber = 2,
                    battingTeam = updatedInnings1.bowlingTeam,
                    bowlingTeam = updatedInnings1.battingTeam,
                    maxOvers = state.config.totalOvers,
                    maxWickets = state.config.wicketsPerSide
                )
                _showInningsBreakDialog.value = true
                state.copy(
                    innings1 = updatedInnings1,
                    innings2 = initialInnings2,
                    lastBallCommentary = "Innings 1 Complete! Target: $target runs.",
                    chasingNarration = "${initialInnings2.battingTeam} needs $target runs in ${state.config.totalOvers} overs."
                )
            } else {
                state.copy(
                    innings1 = updatedInnings1,
                    lastBallCommentary = commentary
                )
            }
        } else {
            // Innings 2 (Chasing)
            val target = state.target ?: (state.innings1.totalRuns + 1)
            val updatedInnings2 = activeInnings.copy(
                totalRuns = nextTotalRuns,
                totalWickets = nextTotalWickets,
                legalBalls = nextLegalBalls,
                balls = updatedBalls,
                isCompleted = isInningsOver || nextTotalRuns >= target
            )

            val isTargetReached = nextTotalRuns >= target
            val isChaseFinished = isTargetReached || isInningsOver
            val chasingNarration = NarrationEngine.generateChasingNarration(target, updatedInnings2)

            if (isChaseFinished) {
                val (winner, result) = when {
                    isTargetReached -> {
                        val wicketsRemaining = updatedInnings2.maxWickets - nextTotalWickets
                        val ballsRemaining = (updatedInnings2.maxOvers * 6) - nextLegalBalls
                        Pair(
                            updatedInnings2.battingTeam,
                            "${updatedInnings2.battingTeam} won by $wicketsRemaining wickets ($ballsRemaining balls left)"
                        )
                    }
                    nextTotalRuns == target - 1 -> {
                        Pair(null, "Match Tied! Scores Level (${state.innings1.totalRuns} runs each)")
                    }
                    else -> {
                        val runDiff = (target - 1) - nextTotalRuns
                        Pair(
                            updatedInnings2.bowlingTeam,
                            "${updatedInnings2.bowlingTeam} won by $runDiff runs"
                        )
                    }
                }

                _showMatchEndDialog.value = true
                state.copy(
                    innings2 = updatedInnings2,
                    isFinished = true,
                    winnerTeam = winner,
                    resultText = result,
                    lastBallCommentary = commentary,
                    chasingNarration = chasingNarration
                )
            } else {
                state.copy(
                    innings2 = updatedInnings2,
                    lastBallCommentary = commentary,
                    chasingNarration = chasingNarration
                )
            }
        }

        _matchState.value = nextMatchState
        _activeMatchState.value = if (nextMatchState.isFinished) null else nextMatchState

        // Save immediately to disk: survives unexpected closure or process kill!
        storage.saveActiveMatch(nextMatchState)
        loadHistory()
        syncLiveMatchIfActive(nextMatchState)
    }

    fun startSecondInnings() {
        _showInningsBreakDialog.value = false
        val state = _matchState.value
        if (state.innings2 != null) {
            val updated = state.copy(
                currentInningsIndex = 2,
                lastBallCommentary = "2nd Innings started! ${state.innings2.battingTeam} chasing ${state.target} runs."
            )
            _matchState.value = updated
            _activeMatchState.value = updated
            storage.saveActiveMatch(updated)
            loadHistory()
            syncLiveMatchIfActive(updated)
        }
    }

    fun undoLastBall() {
        val state = _matchState.value
        if (state.currentInnings.balls.isEmpty()) {
            if (state.currentInningsIndex == 2 && state.innings2?.balls?.isEmpty() == true) {
                val reverted = state.copy(
                    currentInningsIndex = 1,
                    innings2 = null,
                    isFinished = false
                )
                _matchState.value = reverted
                _activeMatchState.value = reverted
                storage.saveActiveMatch(reverted)
                loadHistory()
                return
            }
            return
        }

        val activeInnings = state.currentInnings
        val ballsList = activeInnings.balls.toMutableList()
        val lastBall = ballsList.removeAt(ballsList.size - 1)

        val newTotalRuns = (activeInnings.totalRuns - lastBall.totalRuns).coerceAtLeast(0)
        val newTotalWickets = if (lastBall.isWicket) (activeInnings.totalWickets - 1).coerceAtLeast(0) else activeInnings.totalWickets
        val newLegalBalls = if (lastBall.isLegalBall) (activeInnings.legalBalls - 1).coerceAtLeast(0) else activeInnings.legalBalls

        val revertedInnings = activeInnings.copy(
            totalRuns = newTotalRuns,
            totalWickets = newTotalWickets,
            legalBalls = newLegalBalls,
            balls = ballsList,
            isCompleted = false
        )

        val revertedState = if (state.currentInningsIndex == 1) {
            _showInningsBreakDialog.value = false
            state.copy(
                innings1 = revertedInnings,
                isFinished = false,
                lastBallCommentary = "Last ball undone: ${lastBall.displayText}"
            )
        } else {
            val target = state.target ?: (state.innings1.totalRuns + 1)
            val narration = NarrationEngine.generateChasingNarration(target, revertedInnings)
            _showMatchEndDialog.value = false
            state.copy(
                innings2 = revertedInnings,
                isFinished = false,
                winnerTeam = null,
                resultText = null,
                lastBallCommentary = "Last ball undone: ${lastBall.displayText}",
                chasingNarration = narration
            )
        }

        _matchState.value = revertedState
        _activeMatchState.value = revertedState
        storage.saveActiveMatch(revertedState)
        loadHistory()
        syncLiveMatchIfActive(revertedState)
    }

    private fun syncLiveMatchIfActive(state: MatchState) {
        if (state.isLiveShared) {
            viewModelScope.launch {
                liveRepository.publishLiveMatch(state.toLiveMatch())
            }
        }
    }

    fun enableLiveSharing(
        scorerUid: String,
        onSuccess: (String) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch {
            _isPublishingLive.value = true
            val updatedState = _matchState.value.copy(
                isLiveShared = true,
                scorerUid = scorerUid
            )
            _matchState.value = updatedState
            _activeMatchState.value = updatedState
            storage.saveActiveMatch(updatedState)

            val result = liveRepository.publishLiveMatch(updatedState.toLiveMatch())
            _isPublishingLive.value = false
            if (result.isSuccess) {
                onSuccess(updatedState.matchCode)
            } else {
                onError(result.exceptionOrNull()?.localizedMessage ?: "Failed to publish live match")
            }
        }
    }

    fun watchLiveMatch(rawCode: String) {
        val cleanCode = rawCode.trim().uppercase().replace("-", "").replace(" ", "")
        if (cleanCode.isBlank()) {
            _spectatorError.value = "Please enter a valid match code"
            return
        }

        _spectatorMatchCode.value = cleanCode
        _isSpectatorLoading.value = true
        _spectatorError.value = null
        _currentScreen.value = AppScreen.LIVE_SPECTATOR

        spectatorJob?.cancel()
        spectatorJob = viewModelScope.launch {
            liveRepository.observeLiveMatch(cleanCode)
                .collect { match ->
                    _isSpectatorLoading.value = false
                    if (match != null) {
                        _liveSpectatorMatch.value = match
                        _spectatorError.value = null
                    } else {
                        _liveSpectatorMatch.value = null
                        _spectatorError.value = "Match '$cleanCode' not found or has ended."
                    }
                }
        }
    }

    fun refreshSpectatorMatch() {
        val code = _spectatorMatchCode.value
        if (code.isNotBlank()) {
            watchLiveMatch(code)
        }
    }

    fun dismissInningsBreakDialog() {
        _showInningsBreakDialog.value = false
    }

    fun dismissMatchEndDialog() {
        _showMatchEndDialog.value = false
    }

    fun rematchSameTeams() {
        val config = _matchState.value.config
        proceedToToss(config)
    }

    fun clearAllHistory() {
        storage.clearHistory()
        _activeMatchState.value = null
        loadHistory()
    }
}
