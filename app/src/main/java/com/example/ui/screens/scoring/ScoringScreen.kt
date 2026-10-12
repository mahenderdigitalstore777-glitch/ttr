package com.example.ui.screens.scoring

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.BallEventEntity
import com.example.data.local.InningsEntity
import com.example.data.local.MatchEntity
import com.example.data.local.PlayerEntity
import com.example.ui.MahiCricViewModel
import com.example.ui.components.BallBubble
import com.example.ui.components.Scoring3DButton
import com.example.ui.theme.*

@Composable
fun ScoringScreen(
    viewModel: MahiCricViewModel,
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val match by viewModel.currentMatch.collectAsState()
    val innings by viewModel.currentInnings.collectAsState()
    val state by viewModel.activeMatchState.collectAsState()
    val balls by viewModel.currentInningsBalls.collectAsState()
    val striker by viewModel.striker.collectAsState()
    val nonStriker by viewModel.nonStriker.collectAsState()
    val bowler by viewModel.bowler.collectAsState()

    val isLeftHandMode by viewModel.isLeftHandMode.collectAsState()
    val isBatterySaver by viewModel.isBatterySaver.collectAsState()

    // Dialog states from ViewModel
    val showLeaveConfirm by viewModel.showLeaveConfirmDialog.collectAsState()
    val showNextBatsmanPrompt = state?.currentScreen == "SELECT_NEXT_BATSMAN"
    val showNewBowlerPrompt = state?.currentScreen == "SELECT_NEW_BOWLER"

    if (match == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No active match. Start a new match from Home.", color = MaterialTheme.colorScheme.onBackground)
        }
        return
    }

    Scaffold(
        topBar = {
            // Very slim top bar with Match title, back button, undo/redo quick access
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.primaryContainer)
                        )
                    )
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.showLeaveConfirmDialog.value = true },
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                    Spacer(Modifier.width(6.dp))
                    Column {
                        Text(
                            text = "${match?.teamAShortName} vs ${match?.teamBShortName}",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "${match?.matchType ?: "T20"} • ${match?.venue ?: "Ground"}",
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 10.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { viewModel.undo() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Undo, contentDescription = "Undo", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.redo() },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Redo, contentDescription = "Redo", tint = Color.White)
                    }
                    IconButton(
                        onClick = { viewModel.showMatchSettingsSheet.value = true },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = Color.White)
                    }
                }
            }
        },
        containerColor = if (isBatterySaver) Color.Black else MaterialTheme.colorScheme.background
    ) { innerPadding ->
        // FIXED SINGLE SCREEN WITH ZERO SCROLLING (Section 4 Layout Rules)
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .navigationBarsPadding()
        ) {
            // ZONE 1: Compact Score Header (Weight 0.16f)
            Box(modifier = Modifier.weight(0.16f)) {
                ScoreHeaderZone(match = match!!, innings = innings, isFreeHit = state?.isFreeHit == true)
            }

            // ZONE 2: Players Card (Weight 0.18f)
            Box(modifier = Modifier.weight(0.18f)) {
                PlayersCardZone(
                    striker = striker,
                    nonStriker = nonStriker,
                    bowler = bowler,
                    innings = innings,
                    balls = balls
                )
            }

            // ZONE 3: This-Over Tracker (Weight 0.08f)
            Box(modifier = Modifier.weight(0.08f)) {
                ThisOverTrackerZone(balls = balls, ballsPerOver = match?.ballsPerOver ?: 6)
            }

            // ZONE 4: Two Columns: LEFT (~78%) SCORING PAD, RIGHT (~22%) SIDE PANEL (Weight 0.52f)
            Box(modifier = Modifier.weight(0.52f)) {
                Row(modifier = Modifier.fillMaxSize()) {
                    if (isLeftHandMode) {
                        // Side Panel on Left
                        SidePanelRail(
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(0.22f)
                                .fillMaxHeight()
                        )
                        ScoringPadGrid(
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(0.78f)
                                .fillMaxHeight()
                        )
                    } else {
                        // Standard: Pad on Left, Side Panel on Right
                        ScoringPadGrid(
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(0.78f)
                                .fillMaxHeight()
                        )
                        SidePanelRail(
                            viewModel = viewModel,
                            modifier = Modifier
                                .weight(0.22f)
                                .fillMaxHeight()
                        )
                    }
                }
            }

            // ZONE 5: Slim Bottom Strip (Weight 0.06f)
            Box(modifier = Modifier.weight(0.06f)) {
                SlimBottomStripZone(viewModel = viewModel)
            }
        }
    }

    // Leave Confirmation Dialog
    if (showLeaveConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.showLeaveConfirmDialog.value = false },
            title = { Text("Leave Scoring?") },
            text = { Text("Your match is saved automatically to local storage. You can resume at any time.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.showLeaveConfirmDialog.value = false
                    onNavigateBack()
                }) {
                    Text("Leave")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showLeaveConfirmDialog.value = false }) {
                    Text("Stay")
                }
            }
        )
    }

    // Bottom Sheets & Overlays
    ScoringBottomSheetsContainer(viewModel = viewModel)
}

/**
 * ZONE 1: Compact score header with CRR, Target, RRR, Win probability, and reserved Free Hit banner
 */
@Composable
fun ScoreHeaderZone(
    match: MatchEntity,
    innings: InningsEntity?,
    isFreeHit: Boolean
) {
    val totalRuns = innings?.totalRuns ?: 0
    val wickets = innings?.wickets ?: 0
    val legalBalls = innings?.legalBalls ?: 0
    val ballsPerOver = match.ballsPerOver.coerceAtLeast(1)

    val oversStr = "${legalBalls / ballsPerOver}.${legalBalls % ballsPerOver}"
    val oversFloat = (legalBalls / ballsPerOver) + ((legalBalls % ballsPerOver) / ballsPerOver.toFloat())

    val crr = if (oversFloat > 0f) String.format("%.2f", totalRuns / oversFloat) else "0.00"

    val isChase = match.currentInnings == 2 && match.targetRuns > 0
    val runsNeeded = (match.targetRuns - totalRuns).coerceAtLeast(0)
    val totalBallsInnings = match.totalOvers * ballsPerOver
    val ballsLeft = (totalBallsInnings - legalBalls).coerceAtLeast(0)
    val oversLeftFloat = ballsLeft / ballsPerOver.toFloat()
    val rrr = if (oversLeftFloat > 0f) String.format("%.2f", runsNeeded / oversLeftFloat) else "0.00"

    // Projected score
    val projected = if (oversFloat > 0f) ( (totalRuns / oversFloat) * match.totalOvers ).toInt() else 0

    // Win probability calculation approx
    val battingTeamWinProb = remember(totalRuns, wickets, ballsLeft, match.targetRuns) {
        if (!isChase) 0.5f
        else {
            if (runsNeeded <= 0) 1.0f
            else if (ballsLeft <= 0 || wickets >= match.playersPerSide - 1) 0.0f
            else {
                val reqRate = runsNeeded.toFloat() / (ballsLeft / 6f)
                val wktWeight = (match.playersPerSide - 1 - wickets) / 10f
                val prob = ( (12f - reqRate) / 12f * 0.6f + wktWeight * 0.4f ).coerceIn(0.05f, 0.95f)
                prob
            }
        }
    }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Main score line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "$totalRuns/$wickets",
                        fontWeight = FontWeight.Black,
                        fontSize = 24.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "($oversStr / ${match.totalOvers} ov)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = "CRR: $crr",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    if (isChase) {
                        Spacer(Modifier.width(4.dp))
                        Surface(
                            color = BallNoBall.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = "RRR: $rrr",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = BallNoBall,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
            }

            // Target or Projected line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (isChase) {
                    Text(
                        text = "Target: ${match.targetRuns} • Need $runsNeeded runs in $ballsLeft balls",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = LightPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Text(
                        text = "Projected: $projected • Extras: ${innings?.totalExtras ?: 0}",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                // Win Probability Bar
                if (isChase) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Win: ", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Box(
                            modifier = Modifier
                                .width(60.dp)
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(Color.Gray.copy(alpha = 0.3f))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(battingTeamWinProb)
                                    .background(LightAccentGold)
                            )
                        }
                    }
                }
            }

            // RESERVED Free Hit space (stays even when hidden)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(18.dp),
                contentAlignment = Alignment.Center
            ) {
                if (isFreeHit) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(FreeHitGlow, BoundaryGold, FreeHitGlow)
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "⚡ FREE HIT DELIVERY ⚡",
                            color = Color(0xFF2B1D00),
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * ZONE 2: Players Card (Striker*, Non-striker, Current Bowler, Partnership)
 */
@Composable
fun PlayersCardZone(
    striker: PlayerEntity?,
    nonStriker: PlayerEntity?,
    bowler: PlayerEntity?,
    innings: InningsEntity?,
    balls: List<BallEventEntity>
) {
    // Current batsman stats in this innings
    val strikerBalls = balls.filter { it.strikerId == striker?.id }
    val strikerRuns = strikerBalls.sumOf { it.runsOffBat }
    val strikerBallsFaced = strikerBalls.count { it.extrasType != "WIDE" }
    val strikerFours = strikerBalls.count { it.runsOffBat == 4 }
    val strikerSixes = strikerBalls.count { it.runsOffBat == 6 }
    val strikerSr = if (strikerBallsFaced > 0) String.format("%.1f", (strikerRuns.toFloat() / strikerBallsFaced) * 100) else "0.0"

    val nonStrikerBalls = balls.filter { it.strikerId == nonStriker?.id }
    val nonStrikerRuns = nonStrikerBalls.sumOf { it.runsOffBat }
    val nonStrikerBallsFaced = nonStrikerBalls.count { it.extrasType != "WIDE" }
    val nonStrikerFours = nonStrikerBalls.count { it.runsOffBat == 4 }
    val nonStrikerSixes = nonStrikerBalls.count { it.runsOffBat == 6 }
    val nonStrikerSr = if (nonStrikerBallsFaced > 0) String.format("%.1f", (nonStrikerRuns.toFloat() / nonStrikerBallsFaced) * 100) else "0.0"

    // Bowler figures in this innings
    val bowlerBalls = balls.filter { it.bowlerId == bowler?.id }
    val bowlerLegal = bowlerBalls.count { it.extrasType != "WIDE" && it.extrasType != "NO_BALL" }
    val bowlerOversStr = "${bowlerLegal / 6}.${bowlerLegal % 6}"
    val bowlerRunsConceded = bowlerBalls.sumOf { it.runsOffBat + (if (it.extrasType in listOf("WIDE", "NO_BALL")) it.extrasRuns else 0) }
    val bowlerWickets = bowlerBalls.count { it.isWicket && it.wicketType in listOf("BOWLED", "CAUGHT", "LBW", "STUMPED", "HIT_WICKET") }
    val bowlerOversFloat = (bowlerLegal / 6) + ((bowlerLegal % 6) / 6f)
    val bowlerEcon = if (bowlerOversFloat > 0f) String.format("%.2f", bowlerRunsConceded / bowlerOversFloat) else "0.00"

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            // Striker line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "🏏 ${striker?.name ?: "Striker"}*",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = LightPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$strikerRuns ($strikerBallsFaced)  4s:$strikerFours 6s:$strikerSixes SR:$strikerSr",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Non-Striker line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "   ${nonStriker?.name ?: "Non-Striker"}",
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$nonStrikerRuns ($nonStrikerBallsFaced)  4s:$nonStrikerFours 6s:$nonStrikerSixes SR:$nonStrikerSr",
                    fontWeight = FontWeight.Medium,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

            // Bowler line
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⚾ ${bowler?.name ?: "Bowler"}",
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Text(
                    text = "$bowlerOversStr-0-$bowlerRunsConceded-$bowlerWickets  Econ:$bowlerEcon",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

/**
 * ZONE 3: This-Over Tracker (Colored ball bubbles row)
 */
@Composable
fun ThisOverTrackerZone(
    balls: List<BallEventEntity>,
    ballsPerOver: Int
) {
    // Current over deliveries
    val legalBallsTotal = balls.count { it.extrasType != "WIDE" && it.extrasType != "NO_BALL" }
    val currentOverIndex = legalBallsTotal / ballsPerOver
    val overBalls = balls.filter { it.overNumber == currentOverIndex }

    Card(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
        shape = RoundedCornerShape(10.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "This Over: ",
                fontWeight = FontWeight.Bold,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (overBalls.isEmpty()) {
                Text(
                    text = "Ready to start over...",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                )
            } else {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(overBalls) { ball ->
                        val bubbleText = when {
                            ball.isWicket -> "W"
                            ball.extrasType == "WIDE" -> "Wd${if (ball.extrasRuns > 1) "+${ball.extrasRuns - 1}" else ""}"
                            ball.extrasType == "NO_BALL" -> "Nb${if (ball.runsOffBat > 0) "+${ball.runsOffBat}" else ""}"
                            ball.extrasType == "BYE" -> "B${ball.extrasRuns}"
                            ball.extrasType == "LEG_BYE" -> "Lb${ball.extrasRuns}"
                            ball.runsOffBat == 0 -> "•"
                            else -> ball.runsOffBat.toString()
                        }
                        BallBubble(
                            text = bubbleText,
                            runs = ball.runsOffBat,
                            isWicket = ball.isWicket,
                            extrasType = ball.extrasType
                        )
                    }
                }
            }
        }
    }
}

/**
 * ZONE 4: Left (~78%) SCORING PAD in a fixed 6-row grid
 * Row 1: 0, 1, 2
 * Row 2: 3, 4, 6
 * Row 3: 5, CUSTOM RUNS, WICKET (big red)
 * Row 4: WIDE, NO BALL, BYE
 * Row 5: LEG BYE, PENALTY, CHANGE STRIKE
 * Row 6: UNDO, REDO, SHORT RUN
 */
@Composable
fun ScoringPadGrid(
    viewModel: MahiCricViewModel,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(horizontal = 4.dp, vertical = 2.dp),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Row 1: 0, 1, 2
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Scoring3DButton("0", onClick = { viewModel.recordDelivery(runsOffBat = 0) }, modifier = Modifier.weight(1f), baseColor = BallDot, fontSize = 20)
            Scoring3DButton("1", onClick = { viewModel.recordDelivery(runsOffBat = 1) }, modifier = Modifier.weight(1f), baseColor = BallRuns123, fontSize = 20)
            Scoring3DButton("2", onClick = { viewModel.recordDelivery(runsOffBat = 2) }, modifier = Modifier.weight(1f), baseColor = BallRuns123, fontSize = 20)
        }

        // Row 2: 3, 4, 6
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Scoring3DButton("3", onClick = { viewModel.recordDelivery(runsOffBat = 3) }, modifier = Modifier.weight(1f), baseColor = BallRuns123, fontSize = 20)
            Scoring3DButton("4", onClick = { viewModel.recordDelivery(runsOffBat = 4) }, modifier = Modifier.weight(1f), baseColor = BallFour, fontSize = 20, subText = "FOUR")
            Scoring3DButton("6", onClick = { viewModel.recordDelivery(runsOffBat = 6) }, modifier = Modifier.weight(1f), baseColor = BallSix, fontSize = 20, subText = "SIX")
        }

        // Row 3: 5, CUSTOM, WICKET (Big Red)
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Scoring3DButton("5", onClick = { viewModel.recordDelivery(runsOffBat = 5) }, modifier = Modifier.weight(1f), baseColor = BallRuns123, fontSize = 18)
            Scoring3DButton("CUSTOM", onClick = { viewModel.showCustomRunsDialog.value = true }, modifier = Modifier.weight(1.1f), baseColor = Color(0xFF455A64), fontSize = 13, subText = "Runs/O.T.")
            Scoring3DButton("WICKET", onClick = { viewModel.showWicketSheet.value = true }, modifier = Modifier.weight(1.3f), baseColor = BallWicket, fontSize = 16, isDestructive = true, subText = "OUT")
        }

        // Row 4: WIDE, NO BALL, BYE
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Scoring3DButton("WIDE", onClick = { viewModel.showWideSheet.value = true }, modifier = Modifier.weight(1f), baseColor = BallWideExtras, fontSize = 13, subText = "Wd / Wd+")
            Scoring3DButton("NO BALL", onClick = { viewModel.showNoBallSheet.value = true }, modifier = Modifier.weight(1.1f), baseColor = BallNoBall, fontSize = 13, subText = "+Free Hit")
            Scoring3DButton("BYE", onClick = { viewModel.showByeLegByeSheet.value = true }, modifier = Modifier.weight(1f), baseColor = Color(0xFF00897B), fontSize = 13, subText = "Byes")
        }

        // Row 5: LEG BYE, PENALTY, STRIKE
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Scoring3DButton("LEG BYE", onClick = { viewModel.showByeLegByeSheet.value = true }, modifier = Modifier.weight(1f), baseColor = Color(0xFF00897B), fontSize = 12, subText = "Off pad")
            Scoring3DButton("PENALTY", onClick = { viewModel.showPenaltySheet.value = true }, modifier = Modifier.weight(1f), baseColor = Color(0xFF6D4C41), fontSize = 12, subText = "5 runs")
            Scoring3DButton("STRIKE", onClick = { viewModel.swapStrike() }, modifier = Modifier.weight(1f), baseColor = Color(0xFF3949AB), fontSize = 12, subText = "Swap")
        }

        // Row 6: UNDO, REDO, SHORT RUN
        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            Scoring3DButton("UNDO", onClick = { viewModel.undo() }, modifier = Modifier.weight(1f), baseColor = Color(0xFF546E7A), fontSize = 13)
            Scoring3DButton("REDO", onClick = { viewModel.redo() }, modifier = Modifier.weight(1f), baseColor = Color(0xFF546E7A), fontSize = 13)
            Scoring3DButton("SHORT RUN", onClick = { viewModel.recordDelivery(runsOffBat = 0, isShortRun = true) }, modifier = Modifier.weight(1.2f), baseColor = Color(0xFF78909C), fontSize = 11, subText = "Deduct")
        }
    }
}

/**
 * ZONE 4: Right (~22%) VERTICAL SIDE PANEL ("more tools" rail)
 * Scrolls ONLY inside itself
 */
@Composable
fun SidePanelRail(
    viewModel: MahiCricViewModel,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 2.dp, vertical = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            SideToolIcon(Icons.Default.Assessment, "Card") { viewModel.showScorecardSheet.value = true }
            SideToolIcon(Icons.Default.ChatBubble, "Commentary") { viewModel.showCommentarySheet.value = true }
            SideToolIcon(Icons.Default.Group, "Partner") { viewModel.showPartnershipsSheet.value = true }
            SideToolIcon(Icons.Default.DonutLarge, "Wagon") { viewModel.showWagonWheelSheet.value = true }
            SideToolIcon(Icons.Default.SportsBaseball, "Bowler") { viewModel.showBowlerChangeSheet.value = true }
            SideToolIcon(Icons.Default.PersonOff, "Retire") { viewModel.showRetireSheet.value = true }
            SideToolIcon(Icons.Default.SwapCalls, "Swap") { viewModel.swapStrike() }
            SideToolIcon(Icons.Default.Thunderstorm, "Rain/DLS") { viewModel.showDlsSheet.value = true }
            SideToolIcon(Icons.Default.Notes, "Notes") { viewModel.showScorerNotesSheet.value = true }
            SideToolIcon(Icons.Default.Settings, "Rules") { viewModel.showMatchSettingsSheet.value = true }
        }
    }
}

@Composable
fun SideToolIcon(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier
            .size(46.dp)
            .padding(2.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

/**
 * ZONE 5: Slim Bottom Strip: Scorecard | Commentary | Charts | Info
 */
@Composable
fun SlimBottomStripZone(viewModel: MahiCricViewModel) {
    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surface)
            .border(0.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextButton(
            onClick = { viewModel.showScorecardSheet.value = true },
            modifier = Modifier.weight(1f)
        ) {
            Text("Scorecard", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        TextButton(
            onClick = { viewModel.showCommentarySheet.value = true },
            modifier = Modifier.weight(1f)
        ) {
            Text("Commentary", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        TextButton(
            onClick = { viewModel.showPartnershipsSheet.value = true },
            modifier = Modifier.weight(1f)
        ) {
            Text("Partnership", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
        TextButton(
            onClick = { viewModel.showDlsSheet.value = true },
            modifier = Modifier.weight(1f)
        ) {
            Text("DLS / Rain", fontWeight = FontWeight.Bold, fontSize = 11.sp)
        }
    }
}
