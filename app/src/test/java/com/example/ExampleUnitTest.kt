package com.example

import com.example.engine.NarrationEngine
import com.example.model.BallRecord
import com.example.model.CoinSide
import com.example.model.InningsData
import com.example.model.MatchConfig
import com.example.model.TossDecision
import com.example.model.WicketType
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun testOversFormatting() {
        val innings = InningsData(
            inningsNumber = 1,
            battingTeam = "Team A",
            bowlingTeam = "Team B",
            totalRuns = 42,
            totalWickets = 2,
            legalBalls = 14 // 2 overs and 2 balls
        )
        assertEquals("2.2", innings.oversFormatted)
        assertEquals(2, innings.completedOversCount)
        assertEquals(2, innings.currentOverBallsCount)
    }

    @Test
    fun testExtrasCalculation() {
        val balls = listOf(
            BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 0, displayText = "Wd", totalRuns = 1, isLegalBall = false),
            BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 0, displayText = "Wd+2", totalRuns = 3, isLegalBall = false),
            BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 1, displayText = "1", totalRuns = 1, isLegalBall = true),
            BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 1, displayText = "NB+4", totalRuns = 5, isLegalBall = false),
            BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 2, displayText = "6", totalRuns = 6, isLegalBall = true)
        )

        val innings = InningsData(
            inningsNumber = 1,
            battingTeam = "Team A",
            bowlingTeam = "Team B",
            totalRuns = 16,
            legalBalls = 2,
            balls = balls
        )

        val extras = innings.extras
        assertEquals(2, extras.wides)
        assertEquals(1, extras.noBalls)
        assertEquals(9, extras.totalExtraRuns) // 1 (Wd) + 3 (Wd+2) + 5 (NB+4)
    }

    @Test
    fun testChasingNarrationWinningCondition() {
        val innings2 = InningsData(
            inningsNumber = 2,
            battingTeam = "Gully Kings",
            bowlingTeam = "Street Strikers",
            totalRuns = 52,
            totalWickets = 2,
            legalBalls = 18,
            maxOvers = 5,
            maxWickets = 10
        )
        val target = 50
        val narration = NarrationEngine.generateChasingNarration(target, innings2)
        assertTrue(narration.contains("Gully Kings WINS"))
    }

    @Test
    fun testChasingNarrationLastOverDrama() {
        val innings2 = InningsData(
            inningsNumber = 2,
            battingTeam = "Team B",
            bowlingTeam = "Team A",
            totalRuns = 44,
            totalWickets = 3,
            legalBalls = 26, // 4.2 overs in a 5 over match = 4 balls remaining!
            maxOvers = 5,
            maxWickets = 10
        )
        val target = 50 // Need 6 runs in 4 balls
        val narration = NarrationEngine.generateChasingNarration(target, innings2)
        assertTrue(narration.contains("LAST OVER THRILLER") || narration.contains("6 runs required"))
    }

    @Test
    fun testWicketCommentary() {
        val ball = BallRecord(
            inningsNumber = 1,
            overNumber = 1,
            ballNumberInOver = 3,
            displayText = "W",
            totalRuns = 0,
            isLegalBall = true,
            isWicket = true,
            wicketType = WicketType.BOWLED
        )
        val commentary = NarrationEngine.generateBallCommentary(ball, isChasing = false)
        assertTrue(commentary.contains("CLEAN BOWLED") || commentary.contains("Timber"))
    }

    @Test
    fun testZeroRunsWideAndNoBall() {
        val config = MatchConfig(wideBallRuns = 0, noBallRuns = 0)
        assertEquals(0, config.wideBallRuns)
        assertEquals(0, config.noBallRuns)

        // 0Wd (0 penalty, 0 extra) -> 0 total runs, illegal ball
        val ball0Wd = BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 0, displayText = "0Wd", totalRuns = 0, isLegalBall = false)
        // 0NB (0 penalty, 0 off bat) -> 0 total runs, illegal ball
        val ball0NB = BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 0, displayText = "0NB", totalRuns = 0, isLegalBall = false)

        val innings = InningsData(
            inningsNumber = 1,
            battingTeam = "Team A",
            bowlingTeam = "Team B",
            totalRuns = 0,
            legalBalls = 0,
            balls = listOf(ball0Wd, ball0NB)
        )

        assertEquals(1, innings.extras.wides)
        assertEquals(1, innings.extras.noBalls)
        assertEquals(0, innings.extras.totalExtraRuns)
    }

    @Test
    fun testOneRunWideAndNoBallWithBatRuns() {
        // Wide penalty 1 + 2 extra runs -> 3 runs
        val ballWdPlus2 = BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 0, displayText = "Wd+2", totalRuns = 3, isLegalBall = false)
        // NB penalty 1 + 0+ runs off bat (0) -> 1 run
        val ballNBPlus0 = BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 0, displayText = "1NB", totalRuns = 1, isLegalBall = false)
        // NB penalty 1 + 4+ runs off bat (4) -> 5 runs
        val ballNBPlus4 = BallRecord(inningsNumber = 1, overNumber = 1, ballNumberInOver = 0, displayText = "NB+4", totalRuns = 5, isLegalBall = false)

        val innings = InningsData(
            inningsNumber = 1,
            battingTeam = "Team A",
            bowlingTeam = "Team B",
            totalRuns = 9,
            legalBalls = 0,
            balls = listOf(ballWdPlus2, ballNBPlus0, ballNBPlus4)
        )

        assertEquals(1, innings.extras.wides)
        assertEquals(2, innings.extras.noBalls)
        assertEquals(9, innings.extras.totalExtraRuns)
    }
}
