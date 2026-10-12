package com.example.engine

import com.example.data.local.PlayerEntity

object MvpCalculator {
    fun calculatePoints(player: PlayerEntity): Double {
        var points = 0.0

        // Batting
        points += player.runs * 1.0
        points += player.fours * 1.0
        points += player.sixes * 2.0
        points += player.fifties * 10.0
        points += player.hundreds * 25.0

        if (player.ballsFaced >= 10) {
            val sr = (player.runs.toDouble() / player.ballsFaced) * 100.0
            if (sr >= 150.0) points += 5.0
            if (sr >= 200.0) points += 10.0
        }

        // Bowling
        points += player.wickets * 25.0
        points += player.maidens * 15.0
        if (player.wickets >= 3) points += 10.0
        if (player.wickets >= 5) points += 25.0

        if (player.ballsBowled >= 12) {
            val overs = player.ballsBowled / 6.0
            val econ = player.runsConceded / overs
            if (econ <= 6.0) points += 10.0
            else if (econ <= 7.5) points += 5.0
        }

        // Fielding
        points += player.catches * 10.0
        points += player.stumpings * 12.0
        points += player.runOuts * 12.0

        return points
    }
}
