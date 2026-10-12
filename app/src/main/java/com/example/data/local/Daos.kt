package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Query("SELECT * FROM matches ORDER BY matchDate DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches WHERE status = 'IN_PROGRESS' OR status = 'INNINGS_BREAK' ORDER BY lastUpdatedTimestamp DESC LIMIT 1")
    fun getActiveMatch(): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE status = 'IN_PROGRESS' OR status = 'INNINGS_BREAK' ORDER BY lastUpdatedTimestamp DESC LIMIT 1")
    suspend fun getActiveMatchSync(): MatchEntity?

    @Query("SELECT * FROM matches WHERE id = :matchId")
    fun getMatchById(matchId: String): Flow<MatchEntity?>

    @Query("SELECT * FROM matches WHERE id = :matchId")
    suspend fun getMatchByIdSync(matchId: String): MatchEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateMatch(match: MatchEntity)

    @Delete
    suspend fun deleteMatch(match: MatchEntity)
}

@Dao
interface InningsDao {
    @Query("SELECT * FROM innings WHERE matchId = :matchId ORDER BY inningsNumber ASC")
    fun getInningsForMatch(matchId: String): Flow<List<InningsEntity>>

    @Query("SELECT * FROM innings WHERE matchId = :matchId AND inningsNumber = :inningsNum")
    suspend fun getInningsSync(matchId: String, inningsNum: Int): InningsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateInnings(innings: InningsEntity)
}

@Dao
interface BallEventDao {
    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inningsNumber = :inningsNum ORDER BY deliveryIndex ASC")
    fun getBallsForInnings(matchId: String, inningsNum: Int): Flow<List<BallEventEntity>>

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId AND inningsNumber = :inningsNum ORDER BY deliveryIndex ASC")
    suspend fun getBallsForInningsSync(matchId: String, inningsNum: Int): List<BallEventEntity>

    @Query("SELECT * FROM ball_events WHERE matchId = :matchId ORDER BY deliveryIndex ASC")
    suspend fun getAllBallsForMatchSync(matchId: String): List<BallEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBall(ball: BallEventEntity)

    @Delete
    suspend fun deleteBall(ball: BallEventEntity)

    @Query("DELETE FROM ball_events WHERE matchId = :matchId AND inningsNumber = :inningsNum AND deliveryIndex = :deliveryIndex")
    suspend fun deleteBallByIndex(matchId: String, inningsNum: Int, deliveryIndex: Int)
}

@Dao
interface ActiveMatchStateDao {
    @Query("SELECT * FROM active_match_state WHERE matchId = :matchId")
    fun getStateForMatch(matchId: String): Flow<ActiveMatchStateEntity?>

    @Query("SELECT * FROM active_match_state WHERE matchId = :matchId")
    suspend fun getStateForMatchSync(matchId: String): ActiveMatchStateEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActiveState(state: ActiveMatchStateEntity)

    @Query("DELETE FROM active_match_state WHERE matchId = :matchId")
    suspend fun clearActiveState(matchId: String)
}

@Dao
interface TeamDao {
    @Query("SELECT * FROM teams ORDER BY name ASC")
    fun getAllTeams(): Flow<List<TeamEntity>>

    @Query("SELECT * FROM teams WHERE id = :teamId")
    suspend fun getTeamByIdSync(teamId: String): TeamEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateTeam(team: TeamEntity)
}

@Dao
interface PlayerDao {
    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY name ASC")
    fun getPlayersForTeam(teamId: String): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE teamId = :teamId ORDER BY name ASC")
    suspend fun getPlayersForTeamSync(teamId: String): List<PlayerEntity>

    @Query("SELECT * FROM players ORDER BY runs DESC LIMIT 20")
    fun getTopRunScorers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY wickets DESC LIMIT 20")
    fun getTopWicketTakers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players ORDER BY mvpPoints DESC LIMIT 20")
    fun getTopMvpPlayers(): Flow<List<PlayerEntity>>

    @Query("SELECT * FROM players WHERE id = :playerId")
    suspend fun getPlayerByIdSync(playerId: String): PlayerEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePlayer(player: PlayerEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlayers(players: List<PlayerEntity>)
}

@Dao
interface TournamentDao {
    @Query("SELECT * FROM tournaments ORDER BY createdAt DESC")
    fun getAllTournaments(): Flow<List<TournamentEntity>>

    @Query("SELECT * FROM tournament_teams WHERE tournamentId = :tournamentId ORDER BY points DESC, netRunRate DESC")
    fun getTournamentStandings(tournamentId: String): Flow<List<TournamentTeamEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournament(tournament: TournamentEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTournamentTeam(team: TournamentTeamEntity)
}

@Dao
interface NotificationDao {
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE isRead = 0")
    fun getUnreadCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllAsRead()

    @Query("DELETE FROM notifications")
    suspend fun clearAll()
}

@Dao
interface ScorerNoteDao {
    @Query("SELECT * FROM scorer_notes WHERE matchId = :matchId ORDER BY timestamp DESC")
    fun getNotesForMatch(matchId: String): Flow<List<ScorerNoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: ScorerNoteEntity)
}
