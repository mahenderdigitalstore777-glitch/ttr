package com.example.engine

import kotlin.math.exp
import kotlin.math.roundToInt

data class DlsResult(
    val team1Resources: Double,
    val team2Resources: Double,
    val revisedTarget: Int,
    val parScore: Int,
    val explanation: String
)

object DlsCalculator {
    // Standard DLS G50 constant (average score in 50 overs)
    var g50: Int = 245

    // Standard exponential decay parameters for w = 0..9 wickets lost
    private val bParams = doubleArrayOf(0.035, 0.034, 0.032, 0.030, 0.027, 0.024, 0.020, 0.016, 0.011, 0.006)
    private val aParams = doubleArrayOf(1.000, 0.934, 0.851, 0.749, 0.627, 0.490, 0.341, 0.197, 0.091, 0.025)

    /**
     * Calculate resource percentage remaining given overs remaining (u) and wickets lost (w)
     */
    fun getResourcePercentage(oversRemaining: Float, wicketsLost: Int): Double {
        if (oversRemaining <= 0f || wicketsLost >= 10) return 0.0
        val w = wicketsLost.coerceIn(0, 9)
        val u = oversRemaining.coerceIn(0f, 50f)
        val b = bParams[w]
        val a = aParams[w]
        val raw = a * (1.0 - exp(-b * u))
        val maxRaw = aParams[0] * (1.0 - exp(-bParams[0] * 50.0))
        val percentage = (raw / maxRaw) * 100.0
        return percentage.coerceIn(0.0, 100.0)
    }

    /**
     * Compute revised target when Team 2 overs are reduced
     * S = Team 1 score
     * R1 = Team 1 resources (usually 100% or less if interrupted)
     * R2 = Team 2 resources
     */
    fun calculateDlsTarget(
        team1Score: Int,
        team1AllottedOvers: Float,
        team2AllottedOvers: Float,
        wicketsLostAtInterruption: Int = 0,
        oversCompletedAtInterruption: Float = 0f
    ): DlsResult {
        val r1 = getResourcePercentage(team1AllottedOvers, 0).coerceAtMost(100.0)
        val r2: Double

        if (oversCompletedAtInterruption == 0f) {
            // Interruption before Team 2 innings starts
            r2 = getResourcePercentage(team2AllottedOvers, 0)
        } else {
            // Interruption during Team 2 innings
            val rInitial = getResourcePercentage(team1AllottedOvers, 0)
            val rAtStop = getResourcePercentage(team1AllottedOvers - oversCompletedAtInterruption, wicketsLostAtInterruption)
            val rAtResume = getResourcePercentage(team2AllottedOvers - oversCompletedAtInterruption, wicketsLostAtInterruption)
            val rLost = rAtStop - rAtResume
            r2 = (rInitial - rLost).coerceAtLeast(0.0)
        }

        val target: Int
        val explanation: String
        if (r2 < r1) {
            // Target is reduced
            target = (team1Score * (r2 / r1)).toInt() + 1
            explanation = "Team 2 has less resources (${String.format("%.1f", r2)}% vs ${String.format("%.1f", r1)}%). Target scaled down."
        } else {
            // Target is increased (G50 rule)
            target = team1Score + ((r2 - r1) * g50 / 100.0).roundToInt() + 1
            explanation = "Team 2 has more resources (${String.format("%.1f", r2)}% vs ${String.format("%.1f", r1)}%). G50 factor of $g50 added."
        }

        val par = (target - 1).coerceAtLeast(0)
        return DlsResult(r1, r2, target, par, explanation)
    }

    fun calculateProRataTarget(team1Score: Int, team1Overs: Float, team2Overs: Float): Int {
        if (team1Overs <= 0) return team1Score + 1
        val runRate = team1Score.toDouble() / team1Overs
        return (runRate * team2Overs).toInt() + 1
    }
}
