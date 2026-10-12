package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.*
import com.example.data.repository.MatchRepository
import com.example.data.repository.RoomMatchRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MahiCricViewModel(
    application: Application,
    private val repository: MatchRepository = RoomMatchRepository(
        MahiCricDatabase.getInstance(application),
        application
    )
) : AndroidViewModel(application) {

    val activeMatch: StateFlow<MatchEntity?> = repository.getActiveMatch()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val allMatches: StateFlow<List<MatchEntity>> = repository.getAllMatches()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTeams: StateFlow<List<TeamEntity>> = repository.getAllTeams()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTournaments: StateFlow<List<TournamentEntity>> = repository.getAllTournaments()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val notifications: StateFlow<List<NotificationEntity>> = repository.getNotifications()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topRunScorers: StateFlow<List<PlayerEntity>> = repository.getTopRunScorers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topWicketTakers: StateFlow<List<PlayerEntity>> = repository.getTopWicketTakers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val topMvpPlayers: StateFlow<List<PlayerEntity>> = repository.getTopMvpPlayers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Active Match State
    private val _selectedMatchId = MutableStateFlow<String?>(null)
    val selectedMatchId: StateFlow<String?> = _selectedMatchId.asStateFlow()

    val currentMatch: StateFlow<MatchEntity?> = _selectedMatchId
        .flatMapLatest { id ->
            if (id != null) repository.getMatch(id) else activeMatch
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val activeMatchState: StateFlow<ActiveMatchStateEntity?> = currentMatch
        .flatMapLatest { m ->
            if (m != null) repository.getActiveMatchState(m.id) else flowOf(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentInningsList: StateFlow<List<InningsEntity>> = currentMatch
        .flatMapLatest { m ->
            if (m != null) repository.getInnings(m.id) else flowOf(emptyList())
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentInnings: StateFlow<InningsEntity?> = combine(currentMatch, currentInningsList) { match, list ->
        if (match == null || list.isEmpty()) null
        else list.firstOrNull { it.inningsNumber == match.currentInnings } ?: list.firstOrNull()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val currentInningsBalls: StateFlow<List<BallEventEntity>> = combine(currentMatch, currentInnings) { match, inn ->
        Pair(match?.id, inn?.inningsNumber)
    }.flatMapLatest { (mId, innNum) ->
        if (mId != null && innNum != null) repository.getBallEvents(mId, innNum) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Striker, Non-Striker, Bowler
    val striker: StateFlow<PlayerEntity?> = activeMatchState
        .flatMapLatest { state ->
            if (!state?.strikerId.isNullOrBlank()) flow { emit(repository.getPlayer(state!!.strikerId)) }
            else flowOf<PlayerEntity?>(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val nonStriker: StateFlow<PlayerEntity?> = activeMatchState
        .flatMapLatest { state ->
            if (!state?.nonStrikerId.isNullOrBlank()) flow { emit(repository.getPlayer(state!!.nonStrikerId)) }
            else flowOf<PlayerEntity?>(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val bowler: StateFlow<PlayerEntity?> = activeMatchState
        .flatMapLatest { state ->
            if (!state?.bowlerId.isNullOrBlank()) flow { emit(repository.getPlayer(state!!.bowlerId)) }
            else flowOf<PlayerEntity?>(null)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // UI Dialog & Bottom Sheet visibility states
    val showWicketSheet = MutableStateFlow(false)
    val showCustomRunsDialog = MutableStateFlow(false)
    val showWideSheet = MutableStateFlow(false)
    val showNoBallSheet = MutableStateFlow(false)
    val showByeLegByeSheet = MutableStateFlow(false)
    val showPenaltySheet = MutableStateFlow(false)
    val showBowlerChangeSheet = MutableStateFlow(false)
    val showRetireSheet = MutableStateFlow(false)
    val showDlsSheet = MutableStateFlow(false)
    val showScorecardSheet = MutableStateFlow(false)
    val showCommentarySheet = MutableStateFlow(false)
    val showPartnershipsSheet = MutableStateFlow(false)
    val showWagonWheelSheet = MutableStateFlow(false)
    val showScorerNotesSheet = MutableStateFlow(false)
    val showMatchSettingsSheet = MutableStateFlow(false)
    val showLeaveConfirmDialog = MutableStateFlow(false)
    val showResumePrompt = MutableStateFlow(false)

    // App Preferences
    val isDarkMode = MutableStateFlow(true)
    val isLeftHandMode = MutableStateFlow(false)
    val isBatterySaver = MutableStateFlow(false)
    val scoringPadSize = MutableStateFlow("Medium")
    val isSoundEnabled = MutableStateFlow(true)

    init {
        viewModelScope.launch {
            repository.seedSampleDataIfEmpty()
            val active = repository.getActiveMatchSync()
            if (active != null) {
                _selectedMatchId.value = active.id
                showResumePrompt.value = true
            }
        }
    }

    fun selectMatch(matchId: String) {
        _selectedMatchId.value = matchId
    }

    fun recordDelivery(
        runsOffBat: Int = 0,
        extrasType: String = "NONE",
        extrasRuns: Int = 0,
        isWicket: Boolean = false,
        wicketType: String = "",
        dismissedPlayerId: String = "",
        fielderId: String = "",
        wagonZone: Int = -1,
        overthrows: Int = 0,
        didCross: Boolean = false,
        isShortRun: Boolean = false
    ) {
        val m = currentMatch.value ?: return
        val inn = currentInnings.value ?: return

        viewModelScope.launch {
            repository.recordBallEvent(
                matchId = m.id,
                inningsNum = inn.inningsNumber,
                runsOffBat = runsOffBat,
                extrasType = extrasType,
                extrasRuns = extrasRuns,
                isWicket = isWicket,
                wicketType = wicketType,
                dismissedPlayerId = dismissedPlayerId,
                fielderId = fielderId,
                wagonZone = wagonZone,
                overthrows = overthrows,
                didCross = didCross
            )
        }
    }

    fun undo() {
        val m = currentMatch.value ?: return
        val inn = currentInnings.value ?: return
        viewModelScope.launch {
            repository.undoLastBall(m.id, inn.inningsNumber)
        }
    }

    fun redo() {
        val m = currentMatch.value ?: return
        val inn = currentInnings.value ?: return
        viewModelScope.launch {
            repository.redoBall(m.id, inn.inningsNumber)
        }
    }

    fun swapStrike() {
        val m = currentMatch.value ?: return
        viewModelScope.launch {
            repository.swapStrikerNonStriker(m.id)
        }
    }

    fun changeBowler(newBowlerId: String, isMidOver: Boolean = false, reason: String = "Normal") {
        val m = currentMatch.value ?: return
        viewModelScope.launch {
            repository.changeBowler(m.id, newBowlerId, isMidOver, reason)
            showBowlerChangeSheet.value = false
        }
    }

    fun retireBatsman(playerId: String, isRetiredOut: Boolean) {
        val m = currentMatch.value ?: return
        viewModelScope.launch {
            repository.retireBatsman(m.id, playerId, isRetiredOut)
            showRetireSheet.value = false
        }
    }

    fun selectNextBatsman(playerId: String, makeStriker: Boolean) {
        val m = currentMatch.value ?: return
        viewModelScope.launch {
            repository.selectNextBatsman(m.id, playerId, makeStriker)
        }
    }

    fun startSecondInnings(strikerId: String, nonStrikerId: String, bowlerId: String) {
        val m = currentMatch.value ?: return
        viewModelScope.launch {
            repository.startSecondInnings(m.id, strikerId, nonStrikerId, bowlerId)
        }
    }

    fun completeMatch(resultSummary: String, motmPlayerId: String) {
        val m = currentMatch.value ?: return
        viewModelScope.launch {
            repository.completeMatch(m.id, resultSummary, motmPlayerId)
        }
    }

    fun applyDls(revisedTarget: Int, revisedOvers: Float) {
        val m = currentMatch.value ?: return
        viewModelScope.launch {
            repository.applyDlsTarget(m.id, revisedTarget, revisedOvers)
            showDlsSheet.value = false
        }
    }

    fun saveScorerNote(note: String) {
        val m = currentMatch.value ?: return
        viewModelScope.launch {
            repository.saveScorerNote(m.id, note)
            showScorerNotesSheet.value = false
        }
    }

    fun createMatch(
        teamAId: String,
        teamBId: String,
        teamAName: String,
        teamBName: String,
        teamAShort: String,
        teamBShort: String,
        overs: Int,
        ballsPerOver: Int,
        matchType: String,
        venue: String,
        tossWinnerId: String,
        tossDecision: String,
        strikerId: String,
        nonStrikerId: String,
        bowlerId: String
    ) {
        val matchId = "match_${System.currentTimeMillis()}"
        val battingTeam = if (tossDecision == "BAT") tossWinnerId else (if (tossWinnerId == teamAId) teamBId else teamAId)
        val bowlingTeam = if (battingTeam == teamAId) teamBId else teamAId

        val match = MatchEntity(
            id = matchId,
            teamAId = teamAId,
            teamBId = teamBId,
            teamAName = teamAName,
            teamBName = teamBName,
            teamAShortName = teamAShort,
            teamBShortName = teamBShort,
            tossWinnerId = tossWinnerId,
            tossDecision = tossDecision,
            totalOvers = overs,
            ballsPerOver = ballsPerOver,
            matchType = matchType,
            venue = venue,
            status = "IN_PROGRESS",
            currentInnings = 1,
            battingTeamId = battingTeam,
            bowlingTeamId = bowlingTeam
        )

        val inn1 = InningsEntity(
            id = "${matchId}_1",
            matchId = matchId,
            inningsNumber = 1,
            battingTeamId = battingTeam,
            bowlingTeamId = bowlingTeam
        )

        val activeState = ActiveMatchStateEntity(
            matchId = matchId,
            currentScreen = "SCORING",
            strikerId = strikerId,
            nonStrikerId = nonStrikerId,
            bowlerId = bowlerId
        )

        viewModelScope.launch {
            repository.startMatch(match, inn1, activeState)
            _selectedMatchId.value = matchId
        }
    }

    fun quickScore(
        teamA: String,
        teamB: String,
        overs: Int,
        rA: Int,
        wA: Int,
        rB: Int,
        wB: Int,
        result: String
    ) {
        val matchId = "qs_${System.currentTimeMillis()}"
        val match = MatchEntity(
            id = matchId,
            teamAId = "qa_$matchId",
            teamBId = "qb_$matchId",
            teamAName = teamA,
            teamBName = teamB,
            teamAShortName = teamA.take(4).uppercase(),
            teamBShortName = teamB.take(4).uppercase(),
            totalOvers = overs,
            status = "COMPLETED",
            isQuickScore = true,
            resultSummary = result
        )
        val inn1 = InningsEntity("${matchId}_1", matchId, 1, "qa_$matchId", "qb_$matchId", rA, wA, overs * 6)
        val inn2 = InningsEntity("${matchId}_2", matchId, 2, "qb_$matchId", "qa_$matchId", rB, wB, overs * 6)
        viewModelScope.launch {
            repository.saveMatch(match)
            dbRepository().saveInnings(inn1)
            dbRepository().saveInnings(inn2)
        }
    }

    private fun dbRepository() = object {
        suspend fun saveInnings(inn: InningsEntity) {
            MahiCricDatabase.getInstance(getApplication()).inningsDao().insertOrUpdateInnings(inn)
        }
    }
}
