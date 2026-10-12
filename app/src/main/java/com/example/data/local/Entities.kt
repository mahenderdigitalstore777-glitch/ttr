package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "teams")
data class TeamEntity(
    @PrimaryKey val id: String,
    val name: String,
    val shortName: String,
    val city: String = "",
    val colorHex: String = "#0B6E3A",
    val logoUri: String = "",
    val matchesPlayed: Int = 0,
    val wins: Int = 0,
    val losses: Int = 0,
    val ties: Int = 0,
    val noResults: Int = 0
)

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey val id: String,
    val name: String,
    val teamId: String,
    val role: String = "All-Rounder", // Batsman, Bowler, All-Rounder, Wicketkeeper
    val battingStyle: String = "Right-hand bat",
    val bowlingStyle: String = "Right-arm medium",
    val jerseyNumber: Int = 7,
    val isCaptain: Boolean = false,
    val isWicketKeeper: Boolean = false,
    val matches: Int = 0,
    val innings: Int = 0,
    val runs: Int = 0,
    val ballsFaced: Int = 0,
    val fours: Int = 0,
    val sixes: Int = 0,
    val ducks: Int = 0,
    val fifties: Int = 0,
    val hundreds: Int = 0,
    val highestScore: Int = 0,
    val wickets: Int = 0,
    val ballsBowled: Int = 0,
    val runsConceded: Int = 0,
    val maidens: Int = 0,
    val catches: Int = 0,
    val stumpings: Int = 0,
    val runOuts: Int = 0,
    val mvpPoints: Double = 0.0
)

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey val id: String,
    val teamAId: String,
    val teamBId: String,
    val teamAName: String,
    val teamBName: String,
    val teamAShortName: String,
    val teamBShortName: String,
    val tossWinnerId: String = "",
    val tossDecision: String = "BAT", // BAT or BOWL
    val totalOvers: Int = 20,
    val ballsPerOver: Int = 6,
    val playersPerSide: Int = 11,
    val maxOversPerBowler: Int = 4,
    val matchType: String = "T20", // T5, T10, T20, 50-Over, Custom, Test
    val venue: String = "Local Ground",
    val matchDate: Long = System.currentTimeMillis(),
    val status: String = "NOT_STARTED", // NOT_STARTED, IN_PROGRESS, INNINGS_BREAK, COMPLETED, ABANDONED
    val currentInnings: Int = 1,
    val battingTeamId: String = "",
    val bowlingTeamId: String = "",
    val targetRuns: Int = 0,
    val targetOvers: Float = 0f,
    val dlsMethodApplied: Boolean = false,
    val resultSummary: String = "",
    val manOfTheMatchPlayerId: String = "",
    val isSuperOver: Boolean = false,
    val isQuickScore: Boolean = false,
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "innings")
data class InningsEntity(
    @PrimaryKey val id: String, // matchId_inningsNum
    val matchId: String,
    val inningsNumber: Int,
    val battingTeamId: String,
    val bowlingTeamId: String,
    val totalRuns: Int = 0,
    val wickets: Int = 0,
    val legalBalls: Int = 0,
    val totalExtras: Int = 0,
    val wideRuns: Int = 0,
    val noBallRuns: Int = 0,
    val byeRuns: Int = 0,
    val legByeRuns: Int = 0,
    val penaltyRuns: Int = 0,
    val isCompleted: Boolean = false
)

@Entity(tableName = "ball_events")
data class BallEventEntity(
    @PrimaryKey val id: String, // matchId_innings_deliveryIdx
    val matchId: String,
    val inningsNumber: Int,
    val deliveryIndex: Int, // strictly incremental 1, 2, 3...
    val overNumber: Int, // 0-based over count: 0 for 1st over
    val ballInOver: Int, // legal ball count 1..6
    val bowlerId: String,
    val strikerId: String,
    val nonStrikerId: String,
    val runsOffBat: Int = 0,
    val extrasType: String = "NONE", // NONE, WIDE, NO_BALL, BYE, LEG_BYE, PENALTY
    val extrasRuns: Int = 0,
    val isWicket: Boolean = false,
    val wicketType: String = "", // BOWLED, CAUGHT, LBW, RUN_OUT, STUMPED, HIT_WICKET, etc.
    val dismissedPlayerId: String = "",
    val fielderId: String = "",
    val isFreeHit: Boolean = false,
    val isShortRun: Boolean = false,
    val didBatsmenCross: Boolean = false,
    val commentary: String = "",
    val wagonZone: Int = -1, // 0 to 7
    val pitchZone: Int = -1,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "active_match_state")
data class ActiveMatchStateEntity(
    @PrimaryKey val matchId: String,
    val currentScreen: String, // SCORING, WIZARD, SELECT_NEXT_BATSMAN, SELECT_NEW_BOWLER, MID_OVER_CHANGE, INNINGS_BREAK, SECOND_INNINGS_SETUP, RESULT
    val strikerId: String = "",
    val nonStrikerId: String = "",
    val bowlerId: String = "",
    val previousBowlerId: String = "",
    val isFreeHit: Boolean = false,
    val pendingWicketDismissedId: String = "",
    val pendingWicketType: String = "",
    val pendingDismissalCrossed: Boolean = false,
    val midOverBallsRemaining: Int = 0,
    val midOverOldBowlerId: String = "",
    val statePayloadJson: String = "",
    val lastUpdatedTimestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "tournaments")
data class TournamentEntity(
    @PrimaryKey val id: String,
    val name: String,
    val format: String = "League",
    val totalOvers: Int = 20,
    val venue: String = "Main Ground",
    val pointsWin: Int = 2,
    val pointsTie: Int = 1,
    val pointsNoResult: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "tournament_teams")
data class TournamentTeamEntity(
    @PrimaryKey val id: String, // tournamentId_teamId
    val tournamentId: String,
    val teamId: String,
    val matchesPlayed: Int = 0,
    val won: Int = 0,
    val lost: Int = 0,
    val tied: Int = 0,
    val noResult: Int = 0,
    val points: Int = 0,
    val runsFor: Int = 0,
    val ballsFor: Int = 0,
    val runsAgainst: Int = 0,
    val ballsAgainst: Int = 0,
    val netRunRate: Double = 0.0
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val message: String,
    val type: String = "GENERAL", // MATCH_IN_PROGRESS, MILESTONE, REMINDER, BACKUP
    val matchId: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "scorer_notes")
data class ScorerNoteEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val matchId: String,
    val note: String,
    val timestamp: Long = System.currentTimeMillis()
)
