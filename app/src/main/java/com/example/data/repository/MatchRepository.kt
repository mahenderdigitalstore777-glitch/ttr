package com.example.data.repository

import com.example.data.local.*
import kotlinx.coroutines.flow.Flow

interface MatchRepository {
    fun getActiveMatch(): Flow<MatchEntity?>
    suspend fun getActiveMatchSync(): MatchEntity?
    fun getMatch(matchId: String): Flow<MatchEntity?>
    suspend fun getMatchSync(matchId: String): MatchEntity?
    fun getAllMatches(): Flow<List<MatchEntity>>
    fun getInnings(matchId: String): Flow<List<InningsEntity>>
    fun getBallEvents(matchId: String, inningsNum: Int): Flow<List<BallEventEntity>>
    fun getActiveMatchState(matchId: String): Flow<ActiveMatchStateEntity?>
    suspend fun getActiveMatchStateSync(matchId: String): ActiveMatchStateEntity?

    fun getAllTeams(): Flow<List<TeamEntity>>
    suspend fun getTeam(teamId: String): TeamEntity?
    fun getPlayersForTeam(teamId: String): Flow<List<PlayerEntity>>
    suspend fun getPlayer(playerId: String): PlayerEntity?
    fun getTopRunScorers(): Flow<List<PlayerEntity>>
    fun getTopWicketTakers(): Flow<List<PlayerEntity>>
    fun getTopMvpPlayers(): Flow<List<PlayerEntity>>

    fun getAllTournaments(): Flow<List<TournamentEntity>>
    fun getTournamentStandings(tournamentId: String): Flow<List<TournamentTeamEntity>>
    fun getNotifications(): Flow<List<NotificationEntity>>
    fun getScorerNotes(matchId: String): Flow<List<ScorerNoteEntity>>

    // Transactions & Mutations
    suspend fun saveMatch(match: MatchEntity)
    suspend fun saveActiveState(state: ActiveMatchStateEntity)
    suspend fun startMatch(match: MatchEntity, inn1: InningsEntity, initialActiveState: ActiveMatchStateEntity)
    suspend fun recordBallEvent(
        matchId: String,
        inningsNum: Int,
        runsOffBat: Int,
        extrasType: String, // NONE, WIDE, NO_BALL, BYE, LEG_BYE, PENALTY
        extrasRuns: Int,
        isWicket: Boolean,
        wicketType: String,
        dismissedPlayerId: String,
        fielderId: String,
        wagonZone: Int = -1,
        overthrows: Int = 0,
        didCross: Boolean = false
    ): BallEventResult

    suspend fun undoLastBall(matchId: String, inningsNum: Int): Boolean
    suspend fun redoBall(matchId: String, inningsNum: Int): Boolean
    suspend fun startSecondInnings(matchId: String, strikerId: String, nonStrikerId: String, bowlerId: String)
    suspend fun completeMatch(matchId: String, resultSummary: String, motmPlayerId: String)
    suspend fun changeBowler(matchId: String, newBowlerId: String, isMidOver: Boolean, reason: String)
    suspend fun swapStrikerNonStriker(matchId: String)
    suspend fun retireBatsman(matchId: String, playerId: String, isRetiredOut: Boolean)
    suspend fun selectNextBatsman(matchId: String, nextPlayerId: String, makeStriker: Boolean)
    suspend fun applyDlsTarget(matchId: String, revisedTarget: Int, revisedOvers: Float)
    suspend fun saveScorerNote(matchId: String, note: String)
    suspend fun saveTeam(team: TeamEntity)
    suspend fun savePlayer(player: PlayerEntity)
    suspend fun saveTournament(tournament: TournamentEntity, teams: List<TournamentTeamEntity>)
    suspend fun markNotificationsRead()
    suspend fun seedSampleDataIfEmpty()
    suspend fun exportMatchJson(matchId: String): String
    suspend fun importMatchJson(jsonString: String): Boolean
    suspend fun verifyAndRepairIntegrity(matchId: String): Boolean
}

data class BallEventResult(
    val success: Boolean,
    val isOverComplete: Boolean,
    val isInningsComplete: Boolean,
    val isMatchComplete: Boolean,
    val isFreeHitNext: Boolean,
    val needsNextBatsman: Boolean,
    val summaryText: String
)
