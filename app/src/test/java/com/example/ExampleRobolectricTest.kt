package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.MatchHistoryStorage
import com.example.model.InningsData
import com.example.model.MatchConfig
import com.example.model.MatchState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Gully Cricket", appName)
  }

  @Test
  fun `in progress match reflects in history immediately`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val storage = MatchHistoryStorage(context)
    storage.clearHistory()

    val matchState = MatchState(
      matchId = "match_123",
      config = MatchConfig(team1 = "Street Strikers", team2 = "Gully Kings", totalOvers = 5),
      innings1 = InningsData(
        inningsNumber = 1,
        battingTeam = "Street Strikers",
        bowlingTeam = "Gully Kings",
        totalRuns = 28,
        totalWickets = 1,
        legalBalls = 16
      ),
      isFinished = false
    )

    // Save in-progress match (simulating app sudden exit or crash)
    storage.saveActiveMatch(matchState)

    // 1. Verify active match slot holds the state
    val active = storage.getActiveMatch()
    assertNotNull(active)
    assertEquals("match_123", active?.matchId)
    assertEquals(28, active?.innings1?.totalRuns)
    assertFalse(active!!.isFinished)

    // 2. Verify match history contains the in-progress match
    val history = storage.getSavedMatches()
    assertEquals(1, history.size)
    val historyItem = history[0]
    assertEquals("match_123", historyItem.id)
    assertEquals("Street Strikers", historyItem.team1)
    assertEquals("28/1 (2.4 ov)", historyItem.team1Score)
    assertFalse(historyItem.isCompleted)
    assertTrue(historyItem.result.contains("In Progress"))

    // 3. Score another ball (e.g. boundary 4) -> verify history updates without creating duplicates
    val updatedState = matchState.copy(
      innings1 = matchState.innings1.copy(totalRuns = 32, legalBalls = 17)
    )
    storage.saveActiveMatch(updatedState)

    val updatedHistory = storage.getSavedMatches()
    assertEquals(1, updatedHistory.size) // No duplicates!
    assertEquals("32/1 (2.5 ov)", updatedHistory[0].team1Score)

    // 4. Finish the match -> verify active slot cleared and history marked completed
    val finishedState = updatedState.copy(
      isFinished = true,
      resultText = "Street Strikers won by 14 runs"
    )
    storage.saveActiveMatch(finishedState)

    assertNull(storage.getActiveMatch())
    val finalHistory = storage.getSavedMatches()
    assertEquals(1, finalHistory.size)
    assertTrue(finalHistory[0].isCompleted)
    assertEquals("Street Strikers won by 14 runs", finalHistory[0].result)
  }
}
