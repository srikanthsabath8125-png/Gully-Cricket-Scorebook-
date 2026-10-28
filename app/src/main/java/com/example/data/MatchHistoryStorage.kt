package com.example.data

import android.content.Context
import com.example.model.BallRecord
import com.example.model.CoinSide
import com.example.model.InningsData
import com.example.model.MatchConfig
import com.example.model.MatchState
import com.example.model.SavedMatchSummary
import com.example.model.TossDecision
import com.example.model.TossResult
import com.example.model.WicketType
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

class MatchHistoryStorage(context: Context) {
    private val prefs = context.getSharedPreferences("gully_cricket_history_v2", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_MATCHES_JSON = "matches_history_list"
        private const val KEY_ACTIVE_MATCH_JSON = "active_in_progress_match"
    }

    /**
     * Saves the current active match state to disk and immediately upserts
     * it into the history list so that even if the app gets closed or killed,
     * the match reflects in Match History in real time.
     */
    fun saveActiveMatch(state: MatchState) {
        val serializedState = matchStateToJson(state)
        
        // 1. If match is finished, clear the active in-progress slot; else update active slot
        if (state.isFinished) {
            prefs.edit().remove(KEY_ACTIVE_MATCH_JSON).apply()
        } else {
            prefs.edit().putString(KEY_ACTIVE_MATCH_JSON, serializedState).apply()
        }

        // 2. Reflect in Match History
        val i1 = state.innings1
        val i2 = state.innings2
        val statusText = when {
            state.isFinished -> state.resultText ?: "Match Completed"
            state.currentInningsIndex == 2 -> "In Progress (2nd Innings - Chase)"
            else -> "In Progress (1st Innings)"
        }

        val summary = SavedMatchSummary(
            id = state.matchId,
            team1 = i1.battingTeam,
            team2 = i1.bowlingTeam,
            team1Score = "${i1.totalRuns}/${i1.totalWickets} (${i1.oversFormatted} ov)",
            team2Score = i2?.let { "${it.totalRuns}/${it.totalWickets} (${it.oversFormatted} ov)" },
            result = statusText,
            timestamp = state.timestamp,
            isCompleted = state.isFinished,
            stateJson = serializedState
        )

        upsertMatchSummary(summary)
    }

    fun getActiveMatch(): MatchState? {
        val json = prefs.getString(KEY_ACTIVE_MATCH_JSON, null) ?: return null
        return jsonToMatchState(json)
    }

    fun clearActiveMatch() {
        prefs.edit().remove(KEY_ACTIVE_MATCH_JSON).apply()
    }

    private fun upsertMatchSummary(summary: SavedMatchSummary) {
        val list = getSavedMatches().toMutableList()
        val existingIndex = list.indexOfFirst { it.id == summary.id }
        if (existingIndex >= 0) {
            list[existingIndex] = summary
        } else {
            list.add(0, summary) // Newest at top
        }

        // Keep up to 60 matches
        val trimmed = if (list.size > 60) list.subList(0, 60) else list

        val array = JSONArray()
        for (item in trimmed) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("team1", item.team1)
            obj.put("team2", item.team2)
            obj.put("team1Score", item.team1Score)
            obj.put("team2Score", item.team2Score ?: "")
            obj.put("result", item.result)
            obj.put("timestamp", item.timestamp)
            obj.put("isCompleted", item.isCompleted)
            obj.put("stateJson", item.stateJson ?: "")
            array.put(obj)
        }
        prefs.edit().putString(KEY_MATCHES_JSON, array.toString()).apply()
    }

    fun getSavedMatches(): List<SavedMatchSummary> {
        val json = prefs.getString(KEY_MATCHES_JSON, null) ?: return emptyList()
        val result = mutableListOf<SavedMatchSummary>()
        try {
            val array = JSONArray(json)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val t2Score = obj.optString("team2Score", "")
                val stateJson = obj.optString("stateJson", "").ifEmpty { null }
                result.add(
                    SavedMatchSummary(
                        id = obj.getString("id"),
                        team1 = obj.getString("team1"),
                        team2 = obj.getString("team2"),
                        team1Score = obj.getString("team1Score"),
                        team2Score = if (t2Score.isEmpty()) null else t2Score,
                        result = obj.getString("result"),
                        timestamp = obj.optLong("timestamp", System.currentTimeMillis()),
                        isCompleted = obj.optBoolean("isCompleted", false),
                        stateJson = stateJson
                    )
                )
            }
        } catch (_: Exception) {
            // Ignore parse errors
        }
        return result
    }

    fun deleteMatch(id: String) {
        val list = getSavedMatches().filter { it.id != id }
        val array = JSONArray()
        for (item in list) {
            val obj = JSONObject()
            obj.put("id", item.id)
            obj.put("team1", item.team1)
            obj.put("team2", item.team2)
            obj.put("team1Score", item.team1Score)
            obj.put("team2Score", item.team2Score ?: "")
            obj.put("result", item.result)
            obj.put("timestamp", item.timestamp)
            obj.put("isCompleted", item.isCompleted)
            obj.put("stateJson", item.stateJson ?: "")
            array.put(obj)
        }
        prefs.edit().putString(KEY_MATCHES_JSON, array.toString()).apply()

        // If deleting active match, remove active state as well
        val active = getActiveMatch()
        if (active?.matchId == id) {
            clearActiveMatch()
        }
    }

    fun clearHistory() {
        prefs.edit().remove(KEY_MATCHES_JSON).remove(KEY_ACTIVE_MATCH_JSON).apply()
    }

    // --- JSON Serialization Helpers ---

    private fun ballToJson(ball: BallRecord): JSONObject {
        val obj = JSONObject()
        obj.put("id", ball.id)
        obj.put("inningsNumber", ball.inningsNumber)
        obj.put("overNumber", ball.overNumber)
        obj.put("ballNumberInOver", ball.ballNumberInOver)
        obj.put("displayText", ball.displayText)
        obj.put("totalRuns", ball.totalRuns)
        obj.put("isLegalBall", ball.isLegalBall)
        obj.put("isWicket", ball.isWicket)
        obj.put("wicketType", ball.wicketType?.name ?: "")
        obj.put("commentary", ball.commentary)
        return obj
    }

    private fun jsonToBall(obj: JSONObject): BallRecord {
        val wtStr = obj.optString("wicketType", "")
        val wt = if (wtStr.isNotEmpty()) {
            try { WicketType.valueOf(wtStr) } catch (_: Exception) { null }
        } else null

        return BallRecord(
            id = obj.optString("id", UUID.randomUUID().toString()),
            inningsNumber = obj.optInt("inningsNumber", 1),
            overNumber = obj.optInt("overNumber", 1),
            ballNumberInOver = obj.optInt("ballNumberInOver", 1),
            displayText = obj.optString("displayText", "0"),
            totalRuns = obj.optInt("totalRuns", 0),
            isLegalBall = obj.optBoolean("isLegalBall", true),
            isWicket = obj.optBoolean("isWicket", false),
            wicketType = wt,
            commentary = obj.optString("commentary", "")
        )
    }

    private fun inningsToJson(innings: InningsData): JSONObject {
        val obj = JSONObject()
        obj.put("inningsNumber", innings.inningsNumber)
        obj.put("battingTeam", innings.battingTeam)
        obj.put("bowlingTeam", innings.bowlingTeam)
        obj.put("totalRuns", innings.totalRuns)
        obj.put("totalWickets", innings.totalWickets)
        obj.put("legalBalls", innings.legalBalls)
        obj.put("maxOvers", innings.maxOvers)
        obj.put("maxWickets", innings.maxWickets)
        obj.put("isCompleted", innings.isCompleted)

        val ballsArr = JSONArray()
        for (b in innings.balls) {
            ballsArr.put(ballToJson(b))
        }
        obj.put("balls", ballsArr)
        return obj
    }

    private fun jsonToInnings(obj: JSONObject): InningsData {
        val ballsList = mutableListOf<BallRecord>()
        val ballsArr = obj.optJSONArray("balls")
        if (ballsArr != null) {
            for (i in 0 until ballsArr.length()) {
                ballsList.add(jsonToBall(ballsArr.getJSONObject(i)))
            }
        }
        return InningsData(
            inningsNumber = obj.optInt("inningsNumber", 1),
            battingTeam = obj.optString("battingTeam", "Team A"),
            bowlingTeam = obj.optString("bowlingTeam", "Team B"),
            totalRuns = obj.optInt("totalRuns", 0),
            totalWickets = obj.optInt("totalWickets", 0),
            legalBalls = obj.optInt("legalBalls", 0),
            maxOvers = obj.optInt("maxOvers", 5),
            maxWickets = obj.optInt("maxWickets", 10),
            balls = ballsList,
            isCompleted = obj.optBoolean("isCompleted", false)
        )
    }

    private fun configToJson(cfg: MatchConfig): JSONObject {
        val obj = JSONObject()
        obj.put("team1", cfg.team1)
        obj.put("team2", cfg.team2)
        obj.put("totalOvers", cfg.totalOvers)
        obj.put("wicketsPerSide", cfg.wicketsPerSide)
        obj.put("wideBallRuns", cfg.wideBallRuns)
        obj.put("noBallRuns", cfg.noBallRuns)
        return obj
    }

    private fun jsonToConfig(obj: JSONObject): MatchConfig {
        return MatchConfig(
            team1 = obj.optString("team1", "Team A"),
            team2 = obj.optString("team2", "Team B"),
            totalOvers = obj.optInt("totalOvers", 5),
            wicketsPerSide = obj.optInt("wicketsPerSide", 10),
            wideBallRuns = obj.optInt("wideBallRuns", 1),
            noBallRuns = obj.optInt("noBallRuns", 1)
        )
    }

    private fun tossToJson(toss: TossResult?): JSONObject? {
        if (toss == null) return null
        val obj = JSONObject()
        obj.put("callerTeam", toss.callerTeam)
        obj.put("calledSide", toss.calledSide.name)
        obj.put("landedSide", toss.landedSide.name)
        obj.put("winnerTeam", toss.winnerTeam)
        obj.put("decision", toss.decision.name)
        return obj
    }

    private fun jsonToToss(obj: JSONObject?): TossResult? {
        if (obj == null) return null
        return try {
            TossResult(
                callerTeam = obj.getString("callerTeam"),
                calledSide = CoinSide.valueOf(obj.getString("calledSide")),
                landedSide = CoinSide.valueOf(obj.getString("landedSide")),
                winnerTeam = obj.getString("winnerTeam"),
                decision = TossDecision.valueOf(obj.getString("decision"))
            )
        } catch (_: Exception) { null }
    }

    fun matchStateToJson(state: MatchState): String {
        val obj = JSONObject()
        obj.put("matchId", state.matchId)
        obj.put("config", configToJson(state.config))
        val tossObj = tossToJson(state.toss)
        if (tossObj != null) obj.put("toss", tossObj)
        obj.put("currentInningsIndex", state.currentInningsIndex)
        obj.put("innings1", inningsToJson(state.innings1))
        if (state.innings2 != null) {
            obj.put("innings2", inningsToJson(state.innings2))
        }
        obj.put("isFinished", state.isFinished)
        obj.put("winnerTeam", state.winnerTeam ?: "")
        obj.put("resultText", state.resultText ?: "")
        obj.put("lastBallCommentary", state.lastBallCommentary)
        obj.put("chasingNarration", state.chasingNarration)
        obj.put("timestamp", state.timestamp)
        return obj.toString()
    }

    fun jsonToMatchState(jsonStr: String): MatchState? {
        return try {
            val obj = JSONObject(jsonStr)
            val config = jsonToConfig(obj.getJSONObject("config"))
            val toss = if (obj.has("toss")) jsonToToss(obj.getJSONObject("toss")) else null
            val i1 = jsonToInnings(obj.getJSONObject("innings1"))
            val i2 = if (obj.has("innings2")) jsonToInnings(obj.getJSONObject("innings2")) else null
            val win = obj.optString("winnerTeam", "").ifEmpty { null }
            val res = obj.optString("resultText", "").ifEmpty { null }

            MatchState(
                matchId = obj.optString("matchId", UUID.randomUUID().toString()),
                config = config,
                toss = toss,
                currentInningsIndex = obj.optInt("currentInningsIndex", 1),
                innings1 = i1,
                innings2 = i2,
                isFinished = obj.optBoolean("isFinished", false),
                winnerTeam = win,
                resultText = res,
                lastBallCommentary = obj.optString("lastBallCommentary", "Match ready!"),
                chasingNarration = obj.optString("chasingNarration", ""),
                timestamp = obj.optLong("timestamp", System.currentTimeMillis())
            )
        } catch (_: Exception) { null }
    }
}
