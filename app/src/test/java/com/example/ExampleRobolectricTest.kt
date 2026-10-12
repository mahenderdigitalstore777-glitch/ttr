package com.example

import android.content.Context
import android.content.Intent
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.*
import com.example.data.repository.RoomMatchRepository
import com.example.engine.DlsCalculator
import com.example.receiver.BootReceiver
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    private lateinit var context: Context
    private lateinit var db: MahiCricDatabase
    private lateinit var repository: RoomMatchRepository

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, MahiCricDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = RoomMatchRepository(db, context)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun testAppNameString() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Mahi Cric Pro", appName)
    }

    @Test
    fun testKillAppAfterBall_ResumesWithExactScore() = runBlocking {
        val matchId = "test_match_1"
        val match = MatchEntity(
            id = matchId,
            teamAId = "csk",
            teamBId = "mi",
            teamAName = "Chennai Super Kings",
            teamBName = "Mumbai Indians",
            teamAShortName = "CSK",
            teamBShortName = "MI",
            status = "IN_PROGRESS",
            currentInnings = 1
        )
        val inn1 = InningsEntity(
            id = "${matchId}_1",
            matchId = matchId,
            inningsNumber = 1,
            battingTeamId = "csk",
            bowlingTeamId = "mi"
        )
        val state = ActiveMatchStateEntity(
            matchId = matchId,
            currentScreen = "SCORING",
            strikerId = "p_msd",
            nonStrikerId = "p_rutu",
            bowlerId = "p_bumrah"
        )
        repository.startMatch(match, inn1, state)

        // Record a 4 and a 6
        repository.recordBallEvent(
            matchId = matchId,
            inningsNum = 1,
            runsOffBat = 4,
            extrasType = "NONE",
            extrasRuns = 0,
            isWicket = false,
            wicketType = "",
            dismissedPlayerId = "",
            fielderId = ""
        )
        repository.recordBallEvent(
            matchId = matchId,
            inningsNum = 1,
            runsOffBat = 6,
            extrasType = "NONE",
            extrasRuns = 0,
            isWicket = false,
            wicketType = "",
            dismissedPlayerId = "",
            fielderId = ""
        )

        // Simulate app kill & process restart: read afresh from database
        val activeMatch = repository.getActiveMatchSync()
        assertNotNull(activeMatch)
        assertEquals(matchId, activeMatch!!.id)

        val retrievedInnings = db.inningsDao().getInningsSync(matchId, 1)
        assertNotNull(retrievedInnings)
        assertEquals(10, retrievedInnings!!.totalRuns)
        assertEquals(2, retrievedInnings.legalBalls)

        val retrievedBalls = db.ballEventDao().getBallsForInningsSync(matchId, 1)
        assertEquals(2, retrievedBalls.size)
        assertEquals(4, retrievedBalls[0].runsOffBat)
        assertEquals(6, retrievedBalls[1].runsOffBat)
    }

    @Test
    fun testKillDuringSelectBatsmanStep_ResumesOnSelectBatsmanScreen() = runBlocking {
        val matchId = "test_match_wkt"
        val match = MatchEntity(
            id = matchId,
            teamAId = "csk",
            teamBId = "mi",
            teamAName = "CSK",
            teamBName = "MI",
            teamAShortName = "CSK",
            teamBShortName = "MI",
            status = "IN_PROGRESS",
            currentInnings = 1
        )
        val inn1 = InningsEntity("${matchId}_1", matchId, 1, "csk", "mi")
        val state = ActiveMatchStateEntity(matchId, "SCORING", "p_msd", "p_rutu", "p_bumrah")
        repository.startMatch(match, inn1, state)

        // Bowled out
        repository.recordBallEvent(
            matchId = matchId,
            inningsNum = 1,
            runsOffBat = 0,
            extrasType = "NONE",
            extrasRuns = 0,
            isWicket = true,
            wicketType = "BOWLED",
            dismissedPlayerId = "p_msd",
            fielderId = ""
        )

        // Process killed during batsman selection: check saved active match state
        val savedState = repository.getActiveMatchStateSync(matchId)
        assertNotNull(savedState)
        assertEquals("SELECT_NEXT_BATSMAN", savedState!!.currentScreen)
        assertEquals("p_msd", savedState.pendingWicketDismissedId)
        assertEquals("BOWLED", savedState.pendingWicketType)
    }

    @Test
    fun testKillDuringInningsBreak_ResumesWithTarget() = runBlocking {
        val matchId = "test_inn_break"
        val match = MatchEntity(
            id = matchId,
            teamAId = "csk",
            teamBId = "mi",
            teamAName = "CSK",
            teamBName = "MI",
            teamAShortName = "CSK",
            teamBShortName = "MI",
            totalOvers = 1, // 1-over match
            status = "IN_PROGRESS",
            currentInnings = 1
        )
        val inn1 = InningsEntity("${matchId}_1", matchId, 1, "csk", "mi")
        val state = ActiveMatchStateEntity(matchId, "SCORING", "p_msd", "p_rutu", "p_bumrah")
        repository.startMatch(match, inn1, state)

        // Bowl 6 balls of 1 run each = 6 runs
        repeat(6) {
            repository.recordBallEvent(matchId, 1, 1, "NONE", 0, false, "", "", "")
        }

        val savedMatch = repository.getMatchSync(matchId)
        assertEquals("INNINGS_BREAK", savedMatch!!.status)
        assertEquals(7, savedMatch.targetRuns) // 6 + 1 = 7 runs target

        val savedState = repository.getActiveMatchStateSync(matchId)
        assertEquals("INNINGS_BREAK", savedState!!.currentScreen)
    }

    @Test
    fun testDlsTargetCalculation() {
        // Team 1 scores 180 in 20 overs. Rain reduces Team 2 to 10 overs.
        val result = DlsCalculator.calculateDlsTarget(
            team1Score = 180,
            team1AllottedOvers = 20f,
            team2AllottedOvers = 10f
        )
        assertTrue(result.revisedTarget in 95..120)
        assertTrue(result.team2Resources < result.team1Resources)
    }

    @Test
    fun testDatabaseRepairAndRecalculation() = runBlocking {
        val matchId = "test_repair"
        val match = MatchEntity(
            id = matchId,
            teamAId = "csk",
            teamBId = "mi",
            teamAName = "CSK",
            teamBName = "MI",
            teamAShortName = "CSK",
            teamBShortName = "MI",
            status = "IN_PROGRESS"
        )
        val inn1 = InningsEntity("${matchId}_1", matchId, 1, "csk", "mi")
        val state = ActiveMatchStateEntity(matchId, "SCORING", "p_msd", "p_rutu", "p_bumrah")
        repository.startMatch(match, inn1, state)

        // Add 3 balls
        repository.recordBallEvent(matchId, 1, 4, "NONE", 0, false, "", "", "")
        repository.recordBallEvent(matchId, 1, 2, "NONE", 0, false, "", "", "")
        repository.recordBallEvent(matchId, 1, 6, "NONE", 0, false, "", "", "")

        // Simulate corrupted totals in Innings table
        val corruptedInn = db.inningsDao().getInningsSync(matchId, 1)!!.copy(totalRuns = 999)
        db.inningsDao().insertOrUpdateInnings(corruptedInn)

        // Verify repair detects and fixes discrepancy from ball-by-ball logs
        val repairSuccess = repository.verifyAndRepairIntegrity(matchId)
        assertTrue(repairSuccess)

        val repairedInn = db.inningsDao().getInningsSync(matchId, 1)!!
        assertEquals(12, repairedInn.totalRuns) // 4 + 2 + 6 = 12
    }

    @Test
    fun testBootReceiver_HandlesBootCompleted() {
        val receiver = BootReceiver().apply {
            databaseProvider = { db }
        }
        val intent = Intent(Intent.ACTION_BOOT_COMPLETED)
        receiver.onReceive(context, intent)
    }
}
