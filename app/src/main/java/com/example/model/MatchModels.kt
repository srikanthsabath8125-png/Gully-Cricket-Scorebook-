package com.example.model

import java.util.UUID

enum class CoinSide(val displayName: String) {
    HEADS("Heads"),
    TAILS("Tails")
}

enum class TossDecision(val displayName: String) {
    BAT("Bat First"),
    BOWL("Bowl First")
}

enum class WicketType(val displayName: String, val shortName: String) {
    BOWLED("Bowled", "b"),
    CAUGHT("Caught", "ct"),
    RUN_OUT("Run Out", "ro"),
    STUMPED("Stumped", "st"),
    HIT_WICKET("Hit Wicket", "hw"),
    LBW("LBW", "lbw")
}

data class BallRecord(
    val id: String = UUID.randomUUID().toString(),
    val inningsNumber: Int,
    val overNumber: Int, // 1-indexed (e.g. Over 1, Over 2)
    val ballNumberInOver: Int, // 1-indexed for legal balls
    val displayText: String,
    val totalRuns: Int,
    val isLegalBall: Boolean,
    val isWicket: Boolean = false,
    val wicketType: WicketType? = null,
    val commentary: String = ""
)

data class ExtrasStats(
    val wides: Int = 0,
    val noBalls: Int = 0,
    val byes: Int = 0,
    val legByes: Int = 0,
    val totalExtraRuns: Int = 0
)

data class OverSummary(
    val overNumber: Int,
    val balls: List<BallRecord>,
    val runs: Int,
    val wickets: Int,
    val legalBallsCount: Int
)

data class InningsData(
    val inningsNumber: Int,
    val battingTeam: String,
    val bowlingTeam: String,
    val totalRuns: Int = 0,
    val totalWickets: Int = 0,
    val legalBalls: Int = 0,
    val maxOvers: Int = 5,
    val maxWickets: Int = 10,
    val balls: List<BallRecord> = emptyList(),
    val isCompleted: Boolean = false
) {
    val completedOversCount: Int get() = legalBalls / 6
    val currentOverBallsCount: Int get() = legalBalls % 6

    val oversFormatted: String
        get() = "$completedOversCount.$currentOverBallsCount"

    val currentRunRate: Double
        get() = if (legalBalls > 0) (totalRuns.toDouble() * 6.0) / legalBalls else 0.0

    val currentRunRateFormatted: String
        get() = String.format(java.util.Locale.US, "%.2f", currentRunRate)

    // Current over's balls (since the start of the current over)
    val currentOverBalls: List<BallRecord>
        get() {
            var legalCount = 0
            val targetOverLegalStart = completedOversCount * 6
            val result = mutableListOf<BallRecord>()
            for (ball in balls) {
                if (ball.isLegalBall) {
                    legalCount++
                    if (legalCount > targetOverLegalStart) {
                        result.add(ball)
                    }
                } else {
                    if (legalCount >= targetOverLegalStart) {
                        result.add(ball)
                    }
                }
            }
            return result
        }

    val oversBreakdown: List<OverSummary>
        get() {
            val list = mutableListOf<OverSummary>()
            var legalInOver = 0
            var currentOverNum = 1
            var overBalls = mutableListOf<BallRecord>()
            var overRuns = 0
            var overWickets = 0

            for (ball in balls) {
                overBalls.add(ball)
                overRuns += ball.totalRuns
                if (ball.isWicket) overWickets++

                if (ball.isLegalBall) {
                    legalInOver++
                    if (legalInOver == 6) {
                        list.add(
                            OverSummary(
                                overNumber = currentOverNum,
                                balls = overBalls.toList(),
                                runs = overRuns,
                                wickets = overWickets,
                                legalBallsCount = 6
                            )
                        )
                        currentOverNum++
                        legalInOver = 0
                        overBalls = mutableListOf()
                        overRuns = 0
                        overWickets = 0
                    }
                }
            }
            if (overBalls.isNotEmpty()) {
                list.add(
                    OverSummary(
                        overNumber = currentOverNum,
                        balls = overBalls.toList(),
                        runs = overRuns,
                        wickets = overWickets,
                        legalBallsCount = legalInOver
                    )
                )
            }
            return list
        }

    val extras: ExtrasStats
        get() {
            var wides = 0
            var noBalls = 0
            var byes = 0
            var legByes = 0
            var totalExtraRuns = 0

            for (ball in balls) {
                if (ball.displayText.contains("Wd")) {
                    wides++
                    totalExtraRuns += ball.totalRuns
                } else if (ball.displayText.contains("NB")) {
                    noBalls++
                    totalExtraRuns += ball.totalRuns
                } else if (ball.displayText.startsWith("B")) {
                    byes++
                    totalExtraRuns += ball.totalRuns
                } else if (ball.displayText.startsWith("LB")) {
                    legByes++
                    totalExtraRuns += ball.totalRuns
                }
            }
            return ExtrasStats(wides, noBalls, byes, legByes, totalExtraRuns)
        }
}

data class MatchConfig(
    val team1: String = "Street Strikers",
    val team2: String = "Gully Kings",
    val totalOvers: Int = 5,
    val wicketsPerSide: Int = 10,
    val wideBallRuns: Int = 1,
    val noBallRuns: Int = 1
)

data class TossResult(
    val callerTeam: String,
    val calledSide: CoinSide,
    val landedSide: CoinSide,
    val winnerTeam: String,
    val decision: TossDecision
)

data class MatchState(
    val matchId: String = UUID.randomUUID().toString(),
    val matchCode: String = "GC" + (1000 + kotlin.random.Random.nextInt(9000)).toString(),
    val isLiveShared: Boolean = false,
    val scorerUid: String = "",
    val config: MatchConfig = MatchConfig(),
    val toss: TossResult? = null,
    val currentInningsIndex: Int = 1,
    val innings1: InningsData = InningsData(
        inningsNumber = 1,
        battingTeam = config.team1,
        bowlingTeam = config.team2,
        maxOvers = config.totalOvers,
        maxWickets = config.wicketsPerSide
    ),
    val innings2: InningsData? = null,
    val isFinished: Boolean = false,
    val winnerTeam: String? = null,
    val resultText: String? = null,
    val lastBallCommentary: String = "Match ready! Batter taking strike.",
    val chasingNarration: String = "",
    val timestamp: Long = System.currentTimeMillis()
) {
    val target: Int?
        get() = if (currentInningsIndex == 2 || isFinished) innings1.totalRuns + 1 else null

    val currentInnings: InningsData
        get() = if (currentInningsIndex == 2 && innings2 != null) innings2 else innings1
}

fun MatchState.toLiveMatch(): LiveMatch {
    val currentInn = currentInnings
    val recentBallsList = currentInn.currentOverBalls.map { it.displayText }
    return LiveMatch(
        matchCode = matchCode,
        scorerUid = scorerUid,
        team1Name = config.team1,
        team2Name = config.team2,
        totalOvers = config.totalOvers,
        tossWinner = toss?.winnerTeam ?: config.team1,
        tossDecision = toss?.decision?.name ?: "BAT",
        currentInnings = currentInningsIndex,
        battingTeam = currentInn.battingTeam,
        bowlingTeam = currentInn.bowlingTeam,
        runs = currentInn.totalRuns,
        wickets = currentInn.totalWickets,
        legalBallsBowled = currentInn.legalBalls,
        ballsInCurrentOver = currentInn.currentOverBallsCount,
        completedOvers = currentInn.completedOversCount,
        targetScore = target ?: 0,
        chaseNarration = chasingNarration,
        recentBalls = recentBallsList,
        status = if (isFinished) "COMPLETED" else "LIVE",
        resultSummary = resultText ?: "",
        wideRunsOption = config.wideBallRuns,
        noBallRunsOption = config.noBallRuns
    )
}

data class SavedMatchSummary(
    val id: String,
    val team1: String,
    val team2: String,
    val team1Score: String, // e.g. "54/4 (5.0)"
    val team2Score: String?, // e.g. "55/2 (4.2)"
    val result: String,
    val timestamp: Long,
    val isCompleted: Boolean = false,
    val stateJson: String? = null
)
