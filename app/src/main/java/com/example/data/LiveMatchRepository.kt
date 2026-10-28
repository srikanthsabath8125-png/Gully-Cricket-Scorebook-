package com.example.data

import android.content.Context
import com.example.R
import com.example.model.LiveMatch
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class LiveMatchRepository(
    private val db: FirebaseFirestore
) {
    // Convenience constructor resolving the named database ID
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth get() = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    /**
     * Publishes or updates the live match state in Firestore.
     * Generates / retains timestamps cleanly to satisfy security rules.
     */
    suspend fun publishLiveMatch(match: LiveMatch): Result<String> {
        val uid = requireUserId()
        val docRef = db.collection("matches").document(match.matchCode)
        return try {
            val existingDoc = docRef.get().await()
            val existingCreatedAt: Timestamp? = if (existingDoc.exists()) {
                existingDoc.getTimestamp("createdAt")
            } else null

            val payload = mutableMapOf<String, Any>(
                "matchCode" to match.matchCode,
                "scorerUid" to uid,
                "team1Name" to match.team1Name,
                "team2Name" to match.team2Name,
                "totalOvers" to match.totalOvers,
                "tossWinner" to match.tossWinner,
                "tossDecision" to match.tossDecision,
                "currentInnings" to match.currentInnings,
                "battingTeam" to match.battingTeam,
                "bowlingTeam" to match.bowlingTeam,
                "runs" to match.runs,
                "wickets" to match.wickets,
                "legalBallsBowled" to match.legalBallsBowled,
                "ballsInCurrentOver" to match.ballsInCurrentOver,
                "completedOvers" to match.completedOvers,
                "targetScore" to match.targetScore,
                "chaseNarration" to match.chaseNarration,
                "strikerName" to match.strikerName,
                "strikerRuns" to match.strikerRuns,
                "strikerBalls" to match.strikerBalls,
                "nonStrikerName" to match.nonStrikerName,
                "nonStrikerRuns" to match.nonStrikerRuns,
                "nonStrikerBalls" to match.nonStrikerBalls,
                "currentBowlerName" to match.currentBowlerName,
                "bowlerRunsConceded" to match.bowlerRunsConceded,
                "bowlerWickets" to match.bowlerWickets,
                "bowlerOversDisplay" to match.bowlerOversDisplay,
                "recentBalls" to match.recentBalls,
                "status" to match.status,
                "resultSummary" to match.resultSummary,
                "wideRunsOption" to match.wideRunsOption,
                "noBallRunsOption" to match.noBallRunsOption,
                "updatedAt" to FieldValue.serverTimestamp()
            )

            if (existingCreatedAt != null) {
                payload["createdAt"] = existingCreatedAt
            } else {
                payload["createdAt"] = FieldValue.serverTimestamp()
            }

            docRef.set(payload).await()
            Result.success(match.matchCode)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    /**
     * Observes live ball-by-ball updates for a given match code.
     */
    fun observeLiveMatch(matchCode: String): Flow<LiveMatch?> {
        val docRef = db.collection("matches").document(matchCode)
        return docRef.snapshots()
            .map { snapshot ->
                if (snapshot.exists()) {
                    snapshot.toObject(LiveMatch::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
                } else {
                    null
                }
            }
            .catch { error ->
                if (error is Exception) {
                    handleFirestoreError(error, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    /**
     * One-shot fetch for a match by code.
     */
    suspend fun getLiveMatch(matchCode: String): Result<LiveMatch?> {
        val docRef = db.collection("matches").document(matchCode)
        return try {
            val snapshot = docRef.get().await()
            val match = if (snapshot.exists()) {
                snapshot.toObject(LiveMatch::class.java, DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
            } else null
            Result.success(match)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }
}
