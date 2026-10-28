package com.example.engine

import com.example.model.BallRecord
import com.example.model.InningsData
import java.util.Locale

object NarrationEngine {

    fun generateBallCommentary(ball: BallRecord, isChasing: Boolean, runsNeeded: Int? = null, ballsRemaining: Int? = null): String {
        return when {
            ball.isWicket -> {
                val wicketName = ball.wicketType?.displayName ?: "Wicket"
                when (wicketName) {
                    "Bowled" -> "CLEAN BOWLED! 💥 Timber shattered! The bails go flying across the street!"
                    "Caught" -> "OUT! 🧤 Brilliant catch in the deep! Fielder made no mistake!"
                    "Run Out" -> "RUN OUT! 🏃 Direct hit! Batter is well short of the crease!"
                    "Stumped" -> "STUMPED! ⚡ Keeper whips off the bails in a flash!"
                    "Hit Wicket" -> "HIT WICKET! 🤦 Batter dislodges their own stumps!"
                    else -> "WICKET! ☝️ The umpire's finger goes straight up! Huge breakthrough!"
                }
            }
            ball.displayText == "6" -> {
                "MAXIMUM! 🚀 Massive SIX over the rooftop! Lost ball alert in the gully!"
            }
            ball.displayText == "4" -> {
                "FOUR! 🏏 Pierces the gap between parked scooters! Crisp boundary!"
            }
            ball.displayText == "0" -> {
                "DOT BALL! ⚪ Solid defense. Bowler keeping it tight on the gully pitch."
            }
            ball.displayText.contains("NB") -> {
                val runs = ball.totalRuns
                if (runs > 1) {
                    "NO BALL & BOUNDARY! 🚨 Umpire signals No-Ball plus ${runs - 1} runs! Free hit opportunity!"
                } else if (runs == 1) {
                    "NO BALL! 🚨 Overstepping the crease! 1 extra run awarded. Re-bowl next!"
                } else {
                    "NO BALL! 🚨 Overstepping the crease! 0 runs penalty, ball will be re-bowled!"
                }
            }
            ball.displayText.contains("Wd") -> {
                val runs = ball.totalRuns
                if (runs > 1) {
                    "WIDE BALL + OVERTHROW! 🎁 Slipped down the leg side, batters scamper an extra run!"
                } else if (runs == 1) {
                    "WIDE BALL! ⚪ Bowler strays outside the chalk line. 1 run added and re-bowled."
                } else {
                    "WIDE BALL! ⚪ Bowler strays outside chalk line! 0 runs penalty, ball to be re-bowled."
                }
            }
            ball.totalRuns == 1 -> "Quick single! 🏃 Smart running between the wickets."
            ball.totalRuns == 2 -> "Pushed into the gap for a brisk double! ⚡ Great hustle."
            ball.totalRuns == 3 -> "Three runs taken! 🏃🏃 Great running on the concrete pitch!"
            else -> "${ball.totalRuns} runs added to the team total."
        }
    }

    fun generateChasingNarration(
        target: Int,
        innings2: InningsData
    ): String {
        val runsScored = innings2.totalRuns
        val runsNeeded = target - runsScored
        val totalBalls = innings2.maxOvers * 6
        val ballsBowled = innings2.legalBalls
        val ballsRemaining = (totalBalls - ballsBowled).coerceAtLeast(0)
        val wicketsLeft = (innings2.maxWickets - innings2.totalWickets).coerceAtLeast(0)
        val battingTeam = innings2.battingTeam
        val bowlingTeam = innings2.bowlingTeam

        // Check if finished
        if (runsNeeded <= 0) {
            val ballsSpare = ballsRemaining
            return "🎉 $battingTeam WINS! Chased down target of $target runs with $wicketsLeft wickets and $ballsSpare balls remaining! Outstanding gully chase! 🏆"
        }

        if (wicketsLeft <= 0 || ballsRemaining <= 0) {
            if (runsScored == target - 1) {
                return "🔥 THRILLING TIE! Match tied at $runsScored runs each! Pure gully cricket drama! 🏏"
            }
            val winMargin = (target - 1) - runsScored
            return "🏆 $bowlingTeam WINS by $winMargin runs! Defended their score in spectacular fashion! 🏏"
        }

        // Active Chase Commentary
        val rrr = if (ballsRemaining > 0) (runsNeeded.toDouble() * 6.0) / ballsRemaining else 0.0
        val rrrString = String.format(Locale.US, "%.1f", rrr)

        val situationPrefix = when {
            runsNeeded == 1 -> "⚡ JUST 1 RUN TO WIN! Fielders all inside the circle!"
            ballsRemaining == 1 -> "🔥 FINAL BALL SHOWDOWN! $battingTeam needs $runsNeeded runs off 1 ball!"
            ballsRemaining <= 6 -> "⚡ LAST OVER THRILLER! $runsNeeded runs required in $ballsRemaining balls (RRR: $rrrString)!"
            ballsRemaining <= 12 -> "🎯 Climax approaching! $runsNeeded runs needed in $ballsRemaining balls."
            rrr > 12.0 -> "🚀 Steep chase ahead! RRR is $rrrString ($runsNeeded runs in $ballsRemaining balls). Need boundaries!"
            rrr < 6.0 -> "👌 $battingTeam in cruise control! Need $runsNeeded runs in $ballsRemaining balls."
            else -> "🏏 Game on! $runsNeeded runs needed in $ballsRemaining balls (RRR: $rrrString)."
        }

        val wicketsNote = when {
            wicketsLeft == 1 -> " Last wicket partnership!"
            wicketsLeft <= 2 -> " Only $wicketsLeft wickets in hand!"
            else -> " $wicketsLeft wickets in hand."
        }

        return "$situationPrefix$wicketsNote"
    }
}
