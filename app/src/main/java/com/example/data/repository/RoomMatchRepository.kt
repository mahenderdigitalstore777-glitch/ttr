package com.example.data.repository

import android.content.Context
import androidx.room.withTransaction
import com.example.data.local.*
import com.example.engine.CommentaryGenerator
import com.example.engine.MvpCalculator
import com.example.util.NotificationHelper
import com.example.util.SnapshotManager
import kotlinx.coroutines.flow.Flow
import org.json.JSONArray
import org.json.JSONObject

class RoomMatchRepository(
    private val db: MahiCricDatabase,
    private val context: Context
) : MatchRepository {

    // In-memory redo buffer stack per match
    private val redoStack = mutableMapOf<String, ArrayDeque<BallEventEntity>>()

    override fun getActiveMatch(): Flow<MatchEntity?> = db.matchDao().getActiveMatch()
    override suspend fun getActiveMatchSync(): MatchEntity? = db.matchDao().getActiveMatchSync()
    override fun getMatch(matchId: String): Flow<MatchEntity?> = db.matchDao().getMatchById(matchId)
    override suspend fun getMatchSync(matchId: String): MatchEntity? = db.matchDao().getMatchByIdSync(matchId)
    override fun getAllMatches(): Flow<List<MatchEntity>> = db.matchDao().getAllMatches()
    override fun getInnings(matchId: String): Flow<List<InningsEntity>> = db.inningsDao().getInningsForMatch(matchId)
    override fun getBallEvents(matchId: String, inningsNum: Int): Flow<List<BallEventEntity>> =
        db.ballEventDao().getBallsForInnings(matchId, inningsNum)

    override fun getActiveMatchState(matchId: String): Flow<ActiveMatchStateEntity?> =
        db.activeMatchStateDao().getStateForMatch(matchId)

    override suspend fun getActiveMatchStateSync(matchId: String): ActiveMatchStateEntity? =
        db.activeMatchStateDao().getStateForMatchSync(matchId)

    override fun getAllTeams(): Flow<List<TeamEntity>> = db.teamDao().getAllTeams()
    override suspend fun getTeam(teamId: String): TeamEntity? = db.teamDao().getTeamByIdSync(teamId)
    override fun getPlayersForTeam(teamId: String): Flow<List<PlayerEntity>> = db.playerDao().getPlayersForTeam(teamId)
    override suspend fun getPlayer(playerId: String): PlayerEntity? = db.playerDao().getPlayerByIdSync(playerId)
    override fun getTopRunScorers(): Flow<List<PlayerEntity>> = db.playerDao().getTopRunScorers()
    override fun getTopWicketTakers(): Flow<List<PlayerEntity>> = db.playerDao().getTopWicketTakers()
    override fun getTopMvpPlayers(): Flow<List<PlayerEntity>> = db.playerDao().getTopMvpPlayers()
    override fun getAllTournaments(): Flow<List<TournamentEntity>> = db.tournamentDao().getAllTournaments()
    override fun getTournamentStandings(tournamentId: String): Flow<List<TournamentTeamEntity>> =
        db.tournamentDao().getTournamentStandings(tournamentId)

    override fun getNotifications(): Flow<List<NotificationEntity>> = db.notificationDao().getAllNotifications()
    override fun getScorerNotes(matchId: String): Flow<List<ScorerNoteEntity>> =
        db.scorerNoteDao().getNotesForMatch(matchId)

    override suspend fun saveMatch(match: MatchEntity) = db.matchDao().insertOrUpdateMatch(match)
    override suspend fun saveActiveState(state: ActiveMatchStateEntity) = db.activeMatchStateDao().saveActiveState(state)

    override suspend fun startMatch(
        match: MatchEntity,
        inn1: InningsEntity,
        initialActiveState: ActiveMatchStateEntity
    ) {
        db.withTransaction {
            db.matchDao().insertOrUpdateMatch(match)
            db.inningsDao().insertOrUpdateInnings(inn1)
            db.activeMatchStateDao().saveActiveState(initialActiveState)
            db.notificationDao().insertNotification(
                NotificationEntity(
                    title = "Match Started: ${match.teamAName} vs ${match.teamBName}",
                    message = "Toss won by ${if (match.tossWinnerId == match.teamAId) match.teamAName else match.teamBName}, elected to ${match.tossDecision}.",
                    type = "MATCH_IN_PROGRESS",
                    matchId = match.id
                )
            )
        }
        NotificationHelper.createNotificationChannel(context)
        NotificationHelper.showMatchInProgressNotification(
            context,
            "${match.teamAShortName} vs ${match.teamBShortName}",
            "Match live! Score: 0/0 (0.0 ov)"
        )
    }

    override suspend fun recordBallEvent(
        matchId: String,
        inningsNum: Int,
        runsOffBat: Int,
        extrasType: String,
        extrasRuns: Int,
        isWicket: Boolean,
        wicketType: String,
        dismissedPlayerId: String,
        fielderId: String,
        wagonZone: Int,
        overthrows: Int,
        didCross: Boolean
    ): BallEventResult {
        return db.withTransaction {
            val match = db.matchDao().getMatchByIdSync(matchId) ?: return@withTransaction BallEventResult(
                false, false, false, false, false, false, "Match not found"
            )
            val innings = db.inningsDao().getInningsSync(matchId, inningsNum) ?: return@withTransaction BallEventResult(
                false, false, false, false, false, false, "Innings not found"
            )
            val state = db.activeMatchStateDao().getStateForMatchSync(matchId) ?: return@withTransaction BallEventResult(
                false, false, false, false, false, false, "Active state not found"
            )

            val existingBalls = db.ballEventDao().getBallsForInningsSync(matchId, inningsNum)
            val deliveryIndex = existingBalls.size + 1

            // Legal ball determination
            val isLegal = extrasType != "WIDE" && extrasType != "NO_BALL"
            val totalLegalBallsBefore = innings.legalBalls
            val ballsPerOver = match.ballsPerOver.coerceAtLeast(1)

            val currentOverNumber = totalLegalBallsBefore / ballsPerOver
            val ballInOver = (totalLegalBallsBefore % ballsPerOver) + (if (isLegal) 1 else 0)

            // Runs accounting
            val totalRunsThisBall = runsOffBat + extrasRuns + overthrows
            val newTotalRuns = innings.totalRuns + totalRunsThisBall
            val newLegalBalls = if (isLegal) totalLegalBallsBefore + 1 else totalLegalBallsBefore
            val newWickets = if (isWicket && wicketType != "RETIRED_HURT") innings.wickets + 1 else innings.wickets

            val newWideRuns = innings.wideRuns + (if (extrasType == "WIDE") extrasRuns else 0)
            val newNoBallRuns = innings.noBallRuns + (if (extrasType == "NO_BALL") extrasRuns else 0)
            val newByeRuns = innings.byeRuns + (if (extrasType == "BYE") extrasRuns else 0)
            val newLegByeRuns = innings.legByeRuns + (if (extrasType == "LEG_BYE") extrasRuns else 0)
            val newPenaltyRuns = innings.penaltyRuns + (if (extrasType == "PENALTY") extrasRuns else 0)
            val newTotalExtras = innings.totalExtras + (if (extrasType != "NONE") extrasRuns else 0)

            // Commentary
            val striker = db.playerDao().getPlayerByIdSync(state.strikerId)
            val bowler = db.playerDao().getPlayerByIdSync(state.bowlerId)
            val commentary = CommentaryGenerator.generate(
                bowlerName = bowler?.name ?: "Bowler",
                strikerName = striker?.name ?: "Striker",
                runs = runsOffBat + overthrows,
                extrasType = extrasType,
                extrasRuns = extrasRuns,
                isWicket = isWicket,
                wicketType = wicketType
            )

            // Ball Event
            val ballEvent = BallEventEntity(
                id = "${matchId}_${inningsNum}_$deliveryIndex",
                matchId = matchId,
                inningsNumber = inningsNum,
                deliveryIndex = deliveryIndex,
                overNumber = currentOverNumber,
                ballInOver = ballInOver,
                bowlerId = state.bowlerId,
                strikerId = state.strikerId,
                nonStrikerId = state.nonStrikerId,
                runsOffBat = runsOffBat + overthrows,
                extrasType = extrasType,
                extrasRuns = extrasRuns,
                isWicket = isWicket,
                wicketType = wicketType,
                dismissedPlayerId = if (isWicket) (dismissedPlayerId.ifBlank { state.strikerId }) else "",
                fielderId = fielderId,
                isFreeHit = state.isFreeHit,
                didBatsmenCross = didCross,
                commentary = commentary,
                wagonZone = wagonZone
            )
            db.ballEventDao().insertBall(ballEvent)

            // Update Striker and Bowler stats
            striker?.let { p ->
                val newRuns = p.runs + runsOffBat + overthrows
                val newBallsFaced = if (extrasType != "WIDE") p.ballsFaced + 1 else p.ballsFaced
                val new4s = p.fours + (if (runsOffBat == 4) 1 else 0)
                val new6s = p.sixes + (if (runsOffBat == 6) 1 else 0)
                val updatedPlayer = p.copy(
                    runs = newRuns,
                    ballsFaced = newBallsFaced,
                    fours = new4s,
                    sixes = new6s,
                    highestScore = maxOf(p.highestScore, newRuns),
                    fifties = if (newRuns in 50..99 && p.runs < 50) p.fifties + 1 else p.fifties,
                    hundreds = if (newRuns >= 100 && p.runs < 100) p.hundreds + 1 else p.hundreds
                )
                db.playerDao().insertOrUpdatePlayer(updatedPlayer.copy(mvpPoints = MvpCalculator.calculatePoints(updatedPlayer)))
            }

            bowler?.let { b ->
                val runsCharged = runsOffBat + overthrows + (if (extrasType == "WIDE" || extrasType == "NO_BALL") extrasRuns else 0)
                val newBallsBowled = if (isLegal) b.ballsBowled + 1 else b.ballsBowled
                val isCreditedWicket = isWicket && wicketType in listOf("BOWLED", "CAUGHT", "LBW", "STUMPED", "HIT_WICKET")
                val newWicketsBowled = if (isCreditedWicket) b.wickets + 1 else b.wickets
                val updatedBowler = b.copy(
                    ballsBowled = newBallsBowled,
                    runsConceded = b.runsConceded + runsCharged,
                    wickets = newWicketsBowled
                )
                db.playerDao().insertOrUpdatePlayer(updatedBowler.copy(mvpPoints = MvpCalculator.calculatePoints(updatedBowler)))
            }

            // Update Innings
            val updatedInnings = innings.copy(
                totalRuns = newTotalRuns,
                wickets = newWickets,
                legalBalls = newLegalBalls,
                totalExtras = newTotalExtras,
                wideRuns = newWideRuns,
                noBallRuns = newNoBallRuns,
                byeRuns = newByeRuns,
                legByeRuns = newLegByeRuns,
                penaltyRuns = newPenaltyRuns
            )
            db.inningsDao().insertOrUpdateInnings(updatedInnings)

            // Strike rotation:
            // Odd runs run: rotate. Boundaries (4/6 awarded) do not rotate.
            // Overthrow odd runs rotate.
            val physicalRunsRan = if (runsOffBat in listOf(4, 6)) 0 else (runsOffBat + overthrows + (if (extrasType in listOf("BYE", "LEG_BYE")) extrasRuns else 0))
            val runsRotates = physicalRunsRan % 2 != 0
            val shouldSwapStrike = (runsRotates xor didCross)

            var newStriker = if (shouldSwapStrike) state.nonStrikerId else state.strikerId
            var newNonStriker = if (shouldSwapStrike) state.strikerId else state.nonStrikerId

            // Check Over End
            val isOverComplete = isLegal && (newLegalBalls % ballsPerOver == 0)
            if (isOverComplete) {
                // End of over swaps strike
                val temp = newStriker
                newStriker = newNonStriker
                newNonStriker = temp
            }

            // Free hit logic: NO_BALL sets next ball free hit
            val nextFreeHit = extrasType == "NO_BALL"

            // Target chase completion check
            val targetReached = match.currentInnings == 2 && match.targetRuns > 0 && newTotalRuns >= match.targetRuns
            val allOut = newWickets >= (match.playersPerSide - 1)
            val oversCompleted = newLegalBalls >= (match.totalOvers * ballsPerOver)

            val isInningsComplete = allOut || oversCompleted || targetReached
            var isMatchComplete = false
            var resultSummary = match.resultSummary

            var nextScreen = "SCORING"

            if (targetReached) {
                isMatchComplete = true
                val wicketsLeft = match.playersPerSide - 1 - newWickets
                val ballsLeft = (match.totalOvers * ballsPerOver) - newLegalBalls
                resultSummary = "${match.teamBShortName} won by $wicketsLeft wickets ($ballsLeft balls left)!"
                nextScreen = "RESULT"
            } else if (isInningsComplete) {
                if (match.currentInnings == 1) {
                    nextScreen = "INNINGS_BREAK"
                } else {
                    isMatchComplete = true
                    if (newTotalRuns == match.targetRuns - 1) {
                        resultSummary = "Match Tied!"
                    } else {
                        val runsMargin = (match.targetRuns - 1) - newTotalRuns
                        resultSummary = "${match.teamAShortName} won by $runsMargin runs!"
                    }
                    nextScreen = "RESULT"
                }
            } else if (isWicket && wicketType != "RETIRED_HURT") {
                nextScreen = "SELECT_NEXT_BATSMAN"
            } else if (isOverComplete) {
                nextScreen = "SELECT_NEW_BOWLER"
            }

            // Update Match status
            val updatedMatch = match.copy(
                status = if (isMatchComplete) "COMPLETED" else if (nextScreen == "INNINGS_BREAK") "INNINGS_BREAK" else "IN_PROGRESS",
                resultSummary = resultSummary,
                targetRuns = if (match.currentInnings == 1 && nextScreen == "INNINGS_BREAK") newTotalRuns + 1 else match.targetRuns,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            db.matchDao().insertOrUpdateMatch(updatedMatch)

            // Update Active State
            val updatedState = state.copy(
                currentScreen = nextScreen,
                strikerId = newStriker,
                nonStrikerId = newNonStriker,
                isFreeHit = nextFreeHit,
                pendingWicketDismissedId = if (isWicket) (dismissedPlayerId.ifBlank { state.strikerId }) else "",
                pendingWicketType = if (isWicket) wicketType else "",
                previousBowlerId = if (isOverComplete) state.bowlerId else state.previousBowlerId,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            db.activeMatchStateDao().saveActiveState(updatedState)

            // Safety snapshot at end of over
            if (isOverComplete) {
                val snapshotJson = JSONObject().apply {
                    put("matchId", matchId)
                    put("inningsNum", inningsNum)
                    put("over", currentOverNumber + 1)
                    put("score", "$newTotalRuns/$newWickets")
                    put("balls", newLegalBalls)
                }.toString()
                SnapshotManager.saveRollingSnapshot(context, matchId, currentOverNumber + 1, snapshotJson)
            }

            // Update Ongoing Notification
            val oversStr = "${newLegalBalls / ballsPerOver}.${newLegalBalls % ballsPerOver}"
            NotificationHelper.showMatchInProgressNotification(
                context,
                "${match.teamAShortName} vs ${match.teamBShortName}",
                "$newTotalRuns/$newWickets in $oversStr ov | $commentary"
            )

            BallEventResult(
                success = true,
                isOverComplete = isOverComplete,
                isInningsComplete = isInningsComplete,
                isMatchComplete = isMatchComplete,
                isFreeHitNext = nextFreeHit,
                needsNextBatsman = nextScreen == "SELECT_NEXT_BATSMAN",
                summaryText = commentary
            )
        }
    }

    override suspend fun undoLastBall(matchId: String, inningsNum: Int): Boolean {
        return db.withTransaction {
            val balls = db.ballEventDao().getBallsForInningsSync(matchId, inningsNum)
            if (balls.isEmpty()) return@withTransaction false

            val lastBall = balls.last()
            // Push to redo stack
            val stack = redoStack.getOrPut(matchId) { ArrayDeque() }
            stack.addLast(lastBall)

            // Delete last ball
            db.ballEventDao().deleteBall(lastBall)

            // Recompute entire innings from remaining balls (bulletproof recalculation)
            verifyAndRepairIntegrity(matchId)
            true
        }
    }

    override suspend fun redoBall(matchId: String, inningsNum: Int): Boolean {
        val stack = redoStack[matchId] ?: return false
        if (stack.isEmpty()) return false
        val ball = stack.removeLast()
        db.ballEventDao().insertBall(ball)
        verifyAndRepairIntegrity(matchId)
        return true
    }

    override suspend fun startSecondInnings(
        matchId: String,
        strikerId: String,
        nonStrikerId: String,
        bowlerId: String
    ) {
        db.withTransaction {
            val match = db.matchDao().getMatchByIdSync(matchId) ?: return@withTransaction
            val inn1 = db.inningsDao().getInningsSync(matchId, 1)
            val target = (inn1?.totalRuns ?: 0) + 1

            val inn2 = InningsEntity(
                id = "${matchId}_2",
                matchId = matchId,
                inningsNumber = 2,
                battingTeamId = match.teamBId,
                bowlingTeamId = match.teamAId
            )
            db.inningsDao().insertOrUpdateInnings(inn2)

            val updatedMatch = match.copy(
                currentInnings = 2,
                status = "IN_PROGRESS",
                battingTeamId = match.teamBId,
                bowlingTeamId = match.teamAId,
                targetRuns = target,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            db.matchDao().insertOrUpdateMatch(updatedMatch)

            val newState = ActiveMatchStateEntity(
                matchId = matchId,
                currentScreen = "SCORING",
                strikerId = strikerId,
                nonStrikerId = nonStrikerId,
                bowlerId = bowlerId,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            db.activeMatchStateDao().saveActiveState(newState)
        }
    }

    override suspend fun completeMatch(matchId: String, resultSummary: String, motmPlayerId: String) {
        db.withTransaction {
            val match = db.matchDao().getMatchByIdSync(matchId) ?: return@withTransaction
            val updated = match.copy(
                status = "COMPLETED",
                resultSummary = resultSummary,
                manOfTheMatchPlayerId = motmPlayerId,
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            db.matchDao().insertOrUpdateMatch(updated)
            db.activeMatchStateDao().clearActiveState(matchId)
            NotificationHelper.dismissMatchNotification(context)
        }
    }

    override suspend fun changeBowler(matchId: String, newBowlerId: String, isMidOver: Boolean, reason: String) {
        val state = db.activeMatchStateDao().getStateForMatchSync(matchId) ?: return
        val updated = state.copy(
            bowlerId = newBowlerId,
            currentScreen = "SCORING",
            midOverOldBowlerId = if (isMidOver) state.bowlerId else "",
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        db.activeMatchStateDao().saveActiveState(updated)
    }

    override suspend fun swapStrikerNonStriker(matchId: String) {
        val state = db.activeMatchStateDao().getStateForMatchSync(matchId) ?: return
        val updated = state.copy(
            strikerId = state.nonStrikerId,
            nonStrikerId = state.strikerId,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        db.activeMatchStateDao().saveActiveState(updated)
    }

    override suspend fun retireBatsman(matchId: String, playerId: String, isRetiredOut: Boolean) {
        val state = db.activeMatchStateDao().getStateForMatchSync(matchId) ?: return
        val match = db.matchDao().getMatchByIdSync(matchId) ?: return
        val innings = db.inningsDao().getInningsSync(matchId, match.currentInnings) ?: return

        db.withTransaction {
            if (isRetiredOut) {
                db.inningsDao().insertOrUpdateInnings(innings.copy(wickets = innings.wickets + 1))
            }
            val updatedState = state.copy(
                currentScreen = "SELECT_NEXT_BATSMAN",
                pendingWicketDismissedId = playerId,
                pendingWicketType = if (isRetiredOut) "RETIRED_OUT" else "RETIRED_HURT",
                lastUpdatedTimestamp = System.currentTimeMillis()
            )
            db.activeMatchStateDao().saveActiveState(updatedState)
        }
    }

    override suspend fun selectNextBatsman(matchId: String, nextPlayerId: String, makeStriker: Boolean) {
        val state = db.activeMatchStateDao().getStateForMatchSync(matchId) ?: return
        val isReplacingStriker = state.pendingWicketDismissedId == state.strikerId || makeStriker

        val updated = state.copy(
            currentScreen = "SCORING",
            strikerId = if (isReplacingStriker) nextPlayerId else state.strikerId,
            nonStrikerId = if (!isReplacingStriker) nextPlayerId else state.nonStrikerId,
            pendingWicketDismissedId = "",
            pendingWicketType = "",
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        db.activeMatchStateDao().saveActiveState(updated)
    }

    override suspend fun applyDlsTarget(matchId: String, revisedTarget: Int, revisedOvers: Float) {
        val match = db.matchDao().getMatchByIdSync(matchId) ?: return
        val updated = match.copy(
            targetRuns = revisedTarget,
            targetOvers = revisedOvers,
            dlsMethodApplied = true,
            lastUpdatedTimestamp = System.currentTimeMillis()
        )
        db.matchDao().insertOrUpdateMatch(updated)
    }

    override suspend fun saveScorerNote(matchId: String, note: String) {
        db.scorerNoteDao().insertNote(
            ScorerNoteEntity(matchId = matchId, note = note)
        )
    }

    override suspend fun saveTeam(team: TeamEntity) = db.teamDao().insertOrUpdateTeam(team)
    override suspend fun savePlayer(player: PlayerEntity) = db.playerDao().insertOrUpdatePlayer(player)
    override suspend fun saveTournament(tournament: TournamentEntity, teams: List<TournamentTeamEntity>) {
        db.withTransaction {
            db.tournamentDao().insertTournament(tournament)
            teams.forEach { db.tournamentDao().insertTournamentTeam(it) }
        }
    }

    override suspend fun markNotificationsRead() = db.notificationDao().markAllAsRead()

    override suspend fun verifyAndRepairIntegrity(matchId: String): Boolean {
        return db.withTransaction {
            val match = db.matchDao().getMatchByIdSync(matchId) ?: return@withTransaction false
            val balls = db.ballEventDao().getAllBallsForMatchSync(matchId)

            for (innNum in 1..2) {
                val innBalls = balls.filter { it.inningsNumber == innNum }
                var totalRuns = 0
                var legalBalls = 0
                var wickets = 0
                var extras = 0
                var wides = 0
                var noBalls = 0
                var byes = 0
                var legByes = 0
                var penalties = 0

                innBalls.forEach { b ->
                    totalRuns += b.runsOffBat + b.extrasRuns
                    if (b.extrasType != "WIDE" && b.extrasType != "NO_BALL") legalBalls++
                    if (b.isWicket && b.wicketType != "RETIRED_HURT") wickets++
                    when (b.extrasType) {
                        "WIDE" -> { wides += b.extrasRuns; extras += b.extrasRuns }
                        "NO_BALL" -> { noBalls += b.extrasRuns; extras += b.extrasRuns }
                        "BYE" -> { byes += b.extrasRuns; extras += b.extrasRuns }
                        "LEG_BYE" -> { legByes += b.extrasRuns; extras += b.extrasRuns }
                        "PENALTY" -> { penalties += b.extrasRuns; extras += b.extrasRuns }
                    }
                }

                val currentInn = db.inningsDao().getInningsSync(matchId, innNum)
                if (currentInn != null) {
                    db.inningsDao().insertOrUpdateInnings(
                        currentInn.copy(
                            totalRuns = totalRuns,
                            wickets = wickets,
                            legalBalls = legalBalls,
                            totalExtras = extras,
                            wideRuns = wides,
                            noBallRuns = noBalls,
                            byeRuns = byes,
                            legByeRuns = legByes,
                            penaltyRuns = penalties
                        )
                    )
                }
            }

            // Restore active state striker/non-striker/bowler from last ball if needed
            val currentInningsBalls = balls.filter { it.inningsNumber == match.currentInnings }
            if (currentInningsBalls.isNotEmpty()) {
                val last = currentInningsBalls.last()
                val state = db.activeMatchStateDao().getStateForMatchSync(matchId)
                if (state != null) {
                    db.activeMatchStateDao().saveActiveState(
                        state.copy(
                            bowlerId = last.bowlerId,
                            currentScreen = if (last.isWicket && last.wicketType != "RETIRED_HURT") "SELECT_NEXT_BATSMAN" else "SCORING"
                        )
                    )
                }
            }
            true
        }
    }

    override suspend fun exportMatchJson(matchId: String): String {
        val match = db.matchDao().getMatchByIdSync(matchId) ?: return "{}"
        val balls = db.ballEventDao().getAllBallsForMatchSync(matchId)
        val inn1 = db.inningsDao().getInningsSync(matchId, 1)
        val inn2 = db.inningsDao().getInningsSync(matchId, 2)

        return JSONObject().apply {
            put("matchId", match.id)
            put("teamA", match.teamAName)
            put("teamB", match.teamBName)
            put("overs", match.totalOvers)
            put("status", match.status)
            put("result", match.resultSummary)
            put("inn1Runs", inn1?.totalRuns ?: 0)
            put("inn1Wickets", inn1?.wickets ?: 0)
            put("inn2Runs", inn2?.totalRuns ?: 0)
            put("inn2Wickets", inn2?.wickets ?: 0)
            put("totalDeliveries", balls.size)
        }.toString(2)
    }

    override suspend fun importMatchJson(jsonString: String): Boolean {
        return try {
            val obj = JSONObject(jsonString)
            val matchId = obj.optString("matchId", "imported_${System.currentTimeMillis()}")
            val teamA = obj.optString("teamA", "Team A")
            val teamB = obj.optString("teamB", "Team B")
            val overs = obj.optInt("overs", 20)
            val result = obj.optString("result", "Completed")

            val match = MatchEntity(
                id = matchId,
                teamAId = "team_a_$matchId",
                teamBId = "team_b_$matchId",
                teamAName = teamA,
                teamBName = teamB,
                teamAShortName = teamA.take(4).uppercase(),
                teamBShortName = teamB.take(4).uppercase(),
                totalOvers = overs,
                status = "COMPLETED",
                resultSummary = result
            )
            db.matchDao().insertOrUpdateMatch(match)
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    override suspend fun seedSampleDataIfEmpty() {
        try {
            // If no teams exist, seed friendly teams, players, tournament and sample match
            val teams = db.teamDao().getTeamByIdSync("csk")
            if (teams == null) {
                val csk = TeamEntity("csk", "Chennai Super Kings", "CSK", "Chennai", "#F5B700")
                val mi = TeamEntity("mi", "Mumbai Indians", "MI", "Mumbai", "#1E88E5")
                val rcb = TeamEntity("rcb", "Royal Challengers Bengaluru", "RCB", "Bengaluru", "#E53935")
                val kkr = TeamEntity("kkr", "Kolkata Knight Riders", "KKR", "Kolkata", "#8E24AA")

                db.teamDao().insertOrUpdateTeam(csk)
                db.teamDao().insertOrUpdateTeam(mi)
                db.teamDao().insertOrUpdateTeam(rcb)
                db.teamDao().insertOrUpdateTeam(kkr)

                val cskPlayers = listOf(
                    PlayerEntity("p_msd", "MS Dhoni", "csk", "Wicketkeeper", "Right-hand bat", "Right-arm medium", 7, isCaptain = true, isWicketKeeper = true, runs = 5243, matches = 264),
                    PlayerEntity("p_rutu", "Ruturaj Gaikwad", "csk", "Batsman", "Right-hand bat", "Right-arm offbreak", 31, runs = 2380, matches = 66),
                    PlayerEntity("p_jaddu", "Ravindra Jadeja", "csk", "All-Rounder", "Left-hand bat", "Slow left-arm", 8, runs = 2959, wickets = 160, matches = 240),
                    PlayerEntity("p_dube", "Shivam Dube", "csk", "All-Rounder", "Left-hand bat", "Right-arm medium", 25, runs = 1500, matches = 60),
                    PlayerEntity("p_deepak", "Deepak Chahar", "csk", "Bowler", "Right-hand bat", "Right-arm medium fast", 90, wickets = 77, matches = 81),
                    PlayerEntity("p_pathirana", "Matheesha Pathirana", "csk", "Bowler", "Right-hand bat", "Right-arm fast", 99, wickets = 34, matches = 20)
                )

                val miPlayers = listOf(
                    PlayerEntity("p_rohit", "Rohit Sharma", "mi", "Batsman", "Right-hand bat", "Right-arm offbreak", 45, runs = 6628, matches = 257),
                    PlayerEntity("p_surya", "Suryakumar Yadav", "mi", "Batsman", "Right-hand bat", "Right-arm offbreak", 63, runs = 3594, matches = 150),
                    PlayerEntity("p_hardik", "Hardik Pandya", "mi", "All-Rounder", "Right-hand bat", "Right-arm fast medium", 33, runs = 2525, wickets = 64, matches = 137),
                    PlayerEntity("p_bumrah", "Jasprit Bumrah", "mi", "Bowler", "Right-hand bat", "Right-arm fast", 93, wickets = 165, matches = 133),
                    PlayerEntity("p_ishan", "Ishan Kishan", "mi", "Wicketkeeper", "Left-hand bat", "Right-arm offbreak", 23, runs = 2644, matches = 105),
                    PlayerEntity("p_tilak", "Tilak Varma", "mi", "Batsman", "Left-hand bat", "Right-arm offbreak", 9, runs = 1000, matches = 38)
                )

                db.playerDao().insertPlayers(cskPlayers)
                db.playerDao().insertPlayers(miPlayers)

                // Seed a Tournament
                val tourney = TournamentEntity("tourney_ipl", "Pro Premier League 2026", "League", 20, "Wankhede Stadium")
                val tTeams = listOf(
                    TournamentTeamEntity("tourney_ipl_csk", "tourney_ipl", "csk", matchesPlayed = 4, won = 3, lost = 1, points = 6, netRunRate = 0.85),
                    TournamentTeamEntity("tourney_ipl_mi", "tourney_ipl", "mi", matchesPlayed = 4, won = 2, lost = 2, points = 4, netRunRate = 0.22),
                    TournamentTeamEntity("tourney_ipl_rcb", "tourney_ipl", "rcb", matchesPlayed = 4, won = 2, lost = 2, points = 4, netRunRate = -0.15),
                    TournamentTeamEntity("tourney_ipl_kkr", "tourney_ipl", "kkr", matchesPlayed = 4, won = 1, lost = 3, points = 2, netRunRate = -0.92)
                )
                saveTournament(tourney, tTeams)

                // Seed a Live Sample Match so user can immediately score and test
                val sampleMatch = MatchEntity(
                    id = "live_sample_match",
                    teamAId = "csk",
                    teamBId = "mi",
                    teamAName = "Chennai Super Kings",
                    teamBName = "Mumbai Indians",
                    teamAShortName = "CSK",
                    teamBShortName = "MI",
                    status = "IN_PROGRESS",
                    currentInnings = 1,
                    totalOvers = 20,
                    ballsPerOver = 6,
                    matchType = "T20",
                    venue = "Wankhede Stadium, Mumbai",
                    battingTeamId = "csk",
                    bowlingTeamId = "mi"
                )
                val sampleInn = InningsEntity(
                    id = "live_sample_match_1",
                    matchId = "live_sample_match",
                    inningsNumber = 1,
                    battingTeamId = "csk",
                    bowlingTeamId = "mi",
                    totalRuns = 32,
                    wickets = 0,
                    legalBalls = 18
                )
                val sampleState = ActiveMatchStateEntity(
                    matchId = "live_sample_match",
                    currentScreen = "SCORING",
                    strikerId = "p_rutu",
                    nonStrikerId = "p_msd",
                    bowlerId = "p_bumrah"
                )
                db.matchDao().insertOrUpdateMatch(sampleMatch)
                db.inningsDao().insertOrUpdateInnings(sampleInn)
                db.activeMatchStateDao().saveActiveState(sampleState)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
