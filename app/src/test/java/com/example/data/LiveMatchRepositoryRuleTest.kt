package com.example.data

import com.example.base.FirestoreEmulatorTestBase
import com.example.model.LiveMatch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class LiveMatchRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun publishLiveMatch_authenticatedScorer_createsDocument(): Unit = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = LiveMatchRepository(firestore)

        val code = "GC" + UUID.randomUUID().toString().take(4).uppercase()
        val match = LiveMatch(
            matchCode = code,
            scorerUid = uid,
            team1Name = "Royal Strikers",
            team2Name = "Gully Kings",
            totalOvers = 5,
            tossWinner = "Royal Strikers",
            tossDecision = "BAT",
            currentInnings = 1,
            battingTeam = "Royal Strikers",
            bowlingTeam = "Gully Kings",
            runs = 12,
            wickets = 0,
            status = "LIVE"
        )

        val result = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishLiveMatch(match)
        }
        assertTrue(result.isSuccess)

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getLiveMatch(code).getOrThrow()
        }
        assertNotNull(fetched)
        assertEquals(code, fetched?.matchCode)
        assertEquals(12, fetched?.runs)
    }

    @Test
    fun publishLiveMatch_updateScore_succeeds(): Unit = runBlocking {
        val uid = signInTestUser(ALICE_EMAIL)
        val repository = LiveMatchRepository(firestore)

        val code = "GC" + UUID.randomUUID().toString().take(4).uppercase()
        val match = LiveMatch(
            matchCode = code,
            scorerUid = uid,
            team1Name = "Royal Strikers",
            team2Name = "Gully Kings",
            totalOvers = 5,
            tossWinner = "Royal Strikers",
            tossDecision = "BAT",
            currentInnings = 1,
            battingTeam = "Royal Strikers",
            bowlingTeam = "Gully Kings",
            runs = 12,
            wickets = 0,
            status = "LIVE"
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishLiveMatch(match).getOrThrow()
        }

        val updatedMatch = match.copy(runs = 18, wickets = 1)
        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.publishLiveMatch(updatedMatch)
        }
        assertTrue(updateResult.isSuccess)

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getLiveMatch(code).getOrThrow()
        }
        assertEquals(18, fetched?.runs)
        assertEquals(1, fetched?.wickets)
    }

    @Test
    fun observeLiveMatch_authenticatedSpectator_emitsUpdates(): Unit = runBlocking {
        val scorerUid = signInTestUser(ALICE_EMAIL)
        val scorerRepo = LiveMatchRepository(firestore)

        val code = "GC" + UUID.randomUUID().toString().take(4).uppercase()
        val match = LiveMatch(
            matchCode = code,
            scorerUid = scorerUid,
            team1Name = "Royal Strikers",
            team2Name = "Gully Kings",
            totalOvers = 5,
            tossWinner = "Royal Strikers",
            tossDecision = "BAT",
            currentInnings = 1,
            battingTeam = "Royal Strikers",
            bowlingTeam = "Gully Kings",
            runs = 25,
            wickets = 2,
            status = "LIVE"
        )

        withTimeout(DEFAULT_TIMEOUT_MS) {
            scorerRepo.publishLiveMatch(match).getOrThrow()
        }

        // Switch to Bob (spectator)
        signInTestUser(BOB_EMAIL)
        val spectatorRepo = LiveMatchRepository(firestore)

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            spectatorRepo.observeLiveMatch(code).first { it != null }
        }
        assertNotNull(emitted)
        assertEquals(code, emitted?.matchCode)
        assertEquals(25, emitted?.runs)
    }

    @Test
    fun publishLiveMatch_unauthenticatedUser_fails(): Unit = runBlocking {
        auth.signOut()
        val repository = LiveMatchRepository(firestore)

        val code = "GC" + UUID.randomUUID().toString().take(4).uppercase()
        val match = LiveMatch(
            matchCode = code,
            scorerUid = "some_uid",
            team1Name = "Team A",
            team2Name = "Team B"
        )

        val result = runCatching { repository.publishLiveMatch(match) }
        assertTrue(result.isFailure)
    }

    private companion object {
        const val ALICE_EMAIL = "alice@cricket.com"
        const val BOB_EMAIL = "bob@cricket.com"
        const val DEFAULT_TIMEOUT_MS = 8000L
        const val FLOW_TIMEOUT_MS = 5000L
    }
}
