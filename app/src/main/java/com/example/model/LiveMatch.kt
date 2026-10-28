package com.example.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class LiveMatch(
    @DocumentId val id: String = "",
    val matchCode: String = "",
    val scorerUid: String = "",
    val team1Name: String = "",
    val team2Name: String = "",
    val totalOvers: Int = 5,
    val tossWinner: String = "",
    val tossDecision: String = "BAT",
    val currentInnings: Int = 1,
    val battingTeam: String = "",
    val bowlingTeam: String = "",
    val runs: Int = 0,
    val wickets: Int = 0,
    val legalBallsBowled: Int = 0,
    val ballsInCurrentOver: Int = 0,
    val completedOvers: Int = 0,
    val targetScore: Int = 0,
    val chaseNarration: String = "",
    val strikerName: String = "Striker",
    val strikerRuns: Int = 0,
    val strikerBalls: Int = 0,
    val nonStrikerName: String = "Non-Striker",
    val nonStrikerRuns: Int = 0,
    val nonStrikerBalls: Int = 0,
    val currentBowlerName: String = "Bowler",
    val bowlerRunsConceded: Int = 0,
    val bowlerWickets: Int = 0,
    val bowlerOversDisplay: String = "0.0",
    val recentBalls: List<String> = emptyList(),
    val status: String = "LIVE", // "LIVE" or "COMPLETED"
    val resultSummary: String = "",
    val wideRunsOption: Int = 1,
    val noBallRunsOption: Int = 1,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
