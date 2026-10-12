package com.example.ui.screens.scoring

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlayerEntity
import com.example.engine.DlsCalculator
import com.example.ui.MahiCricViewModel
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScoringBottomSheetsContainer(viewModel: MahiCricViewModel) {
    val showWicket by viewModel.showWicketSheet.collectAsState()
    val showCustomRuns by viewModel.showCustomRunsDialog.collectAsState()
    val showWide by viewModel.showWideSheet.collectAsState()
    val showNoBall by viewModel.showNoBallSheet.collectAsState()
    val showByeLegBye by viewModel.showByeLegByeSheet.collectAsState()
    val showPenalty by viewModel.showPenaltySheet.collectAsState()
    val showBowlerChange by viewModel.showBowlerChangeSheet.collectAsState()
    val showRetire by viewModel.showRetireSheet.collectAsState()
    val showDls by viewModel.showDlsSheet.collectAsState()
    val showScorecard by viewModel.showScorecardSheet.collectAsState()
    val showCommentary by viewModel.showCommentarySheet.collectAsState()
    val showPartnerships by viewModel.showPartnershipsSheet.collectAsState()
    val showWagonWheel by viewModel.showWagonWheelSheet.collectAsState()
    val showNotes by viewModel.showScorerNotesSheet.collectAsState()
    val showMatchSettings by viewModel.showMatchSettingsSheet.collectAsState()

    val activeState by viewModel.activeMatchState.collectAsState()
    val isSelectNextBatsman = activeState?.currentScreen == "SELECT_NEXT_BATSMAN"
    val isSelectNewBowler = activeState?.currentScreen == "SELECT_NEW_BOWLER"

    val currentMatch by viewModel.currentMatch.collectAsState()
    val striker by viewModel.striker.collectAsState()
    val nonStriker by viewModel.nonStriker.collectAsState()
    val bowler by viewModel.bowler.collectAsState()

    // 1. Wicket Sheet
    if (showWicket) {
        ModalBottomSheet(onDismissRequest = { viewModel.showWicketSheet.value = false }) {
            WicketBottomSheetContent(
                isFreeHit = activeState?.isFreeHit == true,
                striker = striker,
                nonStriker = nonStriker,
                onDismissWicket = { wicketType, dismissedId, fielderId, runsCompleted, didCross ->
                    viewModel.showWicketSheet.value = false
                    viewModel.recordDelivery(
                        runsOffBat = runsCompleted,
                        isWicket = true,
                        wicketType = wicketType,
                        dismissedPlayerId = dismissedId,
                        fielderId = fielderId,
                        didCross = didCross
                    )
                }
            )
        }
    }

    // 2. Select Next Batsman Bottom Sheet (Forces selection after wicket!)
    if (isSelectNextBatsman) {
        ModalBottomSheet(
            onDismissRequest = { /* Cannot dismiss without choosing batsman */ }
        ) {
            SelectNextBatsmanContent(
                striker = striker,
                nonStriker = nonStriker,
                onSelectBatsman = { nextPlayerId, asStriker ->
                    viewModel.selectNextBatsman(nextPlayerId, asStriker)
                }
            )
        }
    }

    // 3. Select New Bowler Bottom Sheet (At over end or change)
    if (isSelectNewBowler || showBowlerChange) {
        ModalBottomSheet(
            onDismissRequest = {
                if (showBowlerChange) viewModel.showBowlerChangeSheet.value = false
            }
        ) {
            SelectBowlerContent(
                currentBowlerId = bowler?.id ?: "",
                previousBowlerId = activeState?.previousBowlerId ?: "",
                isMidOver = showBowlerChange,
                onSelectBowler = { newBowlerId, reason ->
                    viewModel.changeBowler(newBowlerId, showBowlerChange, reason)
                }
            )
        }
    }

    // 4. Custom Runs Dialog
    if (showCustomRuns) {
        CustomRunsDialog(
            onDismiss = { viewModel.showCustomRunsDialog.value = false },
            onConfirm = { runs, overthrows ->
                viewModel.showCustomRunsDialog.value = false
                viewModel.recordDelivery(runsOffBat = runs, overthrows = overthrows)
            }
        )
    }

    // 5. Wide Sheet
    if (showWide) {
        ModalBottomSheet(onDismissRequest = { viewModel.showWideSheet.value = false }) {
            WideBottomSheetContent(
                onConfirm = { extrasRuns ->
                    viewModel.showWideSheet.value = false
                    viewModel.recordDelivery(extrasType = "WIDE", extrasRuns = extrasRuns)
                }
            )
        }
    }

    // 6. No Ball Sheet
    if (showNoBall) {
        ModalBottomSheet(onDismissRequest = { viewModel.showNoBallSheet.value = false }) {
            NoBallBottomSheetContent(
                onConfirm = { batRuns, isBye, extrasRuns ->
                    viewModel.showNoBallSheet.value = false
                    viewModel.recordDelivery(
                        runsOffBat = batRuns,
                        extrasType = "NO_BALL",
                        extrasRuns = 1 + extrasRuns
                    )
                }
            )
        }
    }

    // 7. Bye / Leg Bye Sheet
    if (showByeLegBye) {
        ModalBottomSheet(onDismissRequest = { viewModel.showByeLegByeSheet.value = false }) {
            ByeLegByeBottomSheetContent(
                onConfirm = { type, runs ->
                    viewModel.showByeLegByeSheet.value = false
                    viewModel.recordDelivery(extrasType = type, extrasRuns = runs)
                }
            )
        }
    }

    // 8. Penalty Sheet
    if (showPenalty) {
        ModalBottomSheet(onDismissRequest = { viewModel.showPenaltySheet.value = false }) {
            PenaltyBottomSheetContent(
                onConfirm = { runs ->
                    viewModel.showPenaltySheet.value = false
                    viewModel.recordDelivery(extrasType = "PENALTY", extrasRuns = runs)
                }
            )
        }
    }

    // 9. Rain / DLS Sheet
    if (showDls) {
        ModalBottomSheet(onDismissRequest = { viewModel.showDlsSheet.value = false }) {
            DlsCalculatorSheetContent(
                currentMatch = currentMatch,
                onApply = { target, overs ->
                    viewModel.applyDls(target, overs)
                }
            )
        }
    }

    // 10. Scorecard Sheet
    if (showScorecard) {
        ModalBottomSheet(onDismissRequest = { viewModel.showScorecardSheet.value = false }) {
            ScorecardSheetContent(viewModel = viewModel)
        }
    }

    // 11. Commentary Sheet
    if (showCommentary) {
        ModalBottomSheet(onDismissRequest = { viewModel.showCommentarySheet.value = false }) {
            CommentarySheetContent(viewModel = viewModel)
        }
    }

    // 12. Partnerships Sheet
    if (showPartnerships) {
        ModalBottomSheet(onDismissRequest = { viewModel.showPartnershipsSheet.value = false }) {
            PartnershipsSheetContent(viewModel = viewModel)
        }
    }

    // 13. Scorer Notes Sheet
    if (showNotes) {
        ModalBottomSheet(onDismissRequest = { viewModel.showScorerNotesSheet.value = false }) {
            ScorerNotesSheetContent(
                onSave = { note -> viewModel.saveScorerNote(note) }
            )
        }
    }

    // 14. Match Settings Sheet
    if (showMatchSettings) {
        ModalBottomSheet(onDismissRequest = { viewModel.showMatchSettingsSheet.value = false }) {
            MatchSettingsSheetContent(viewModel = viewModel)
        }
    }
}

/**
 * Wicket Dismissal Sheet with engine validations
 */
@Composable
fun WicketBottomSheetContent(
    isFreeHit: Boolean,
    striker: PlayerEntity?,
    nonStriker: PlayerEntity?,
    onDismissWicket: (wicketType: String, dismissedId: String, fielderId: String, runsCompleted: Int, didCross: Boolean) -> Unit
) {
    var selectedType by remember { mutableStateOf("BOWLED") }
    var dismissedPlayerId by remember { mutableStateOf(striker?.id ?: "") }
    var runsCompleted by remember { mutableStateOf(0) }
    var didCross by remember { mutableStateOf(false) }
    var fielderName by remember { mutableStateOf("") }

    val validTypes = remember(isFreeHit) {
        if (isFreeHit) {
            listOf("RUN_OUT")
        } else {
            listOf(
                "BOWLED", "CAUGHT", "LBW", "RUN_OUT", "STUMPED", "HIT_WICKET",
                "RETIRED_HURT", "RETIRED_OUT", "HANDLED_BALL", "OBSTRUCTING"
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Wicket / Dismissal", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BallWicket)

        if (isFreeHit) {
            Surface(color = FreeHitGlow.copy(alpha = 0.2f), shape = RoundedCornerShape(6.dp)) {
                Text(
                    "⚡ Free Hit Delivery: Only RUN OUT is allowed!",
                    color = Color(0xFF6B4700),
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }

        // Dismissal Types Grid
        Text("Dismissal Type:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        LazyColumn(modifier = Modifier.heightIn(max = 160.dp)) {
            items(validTypes.chunked(3)) { rowTypes ->
                Row(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    rowTypes.forEach { type ->
                        val isSelected = selectedType == type
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedType = type },
                            label = { Text(type.replace("_", " "), fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (rowTypes.size < 3) {
                        repeat(3 - rowTypes.size) { Spacer(Modifier.weight(1f)) }
                    }
                }
            }
        }

        // Run out options
        if (selectedType == "RUN_OUT") {
            Text("Who was dismissed?", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = dismissedPlayerId == (striker?.id ?: ""),
                    onClick = { dismissedPlayerId = striker?.id ?: "" },
                    label = { Text("Striker (${striker?.name ?: "Striker"})") }
                )
                FilterChip(
                    selected = dismissedPlayerId == (nonStriker?.id ?: ""),
                    onClick = { dismissedPlayerId = nonStriker?.id ?: "" },
                    label = { Text("Non-Striker (${nonStriker?.name ?: "Non-Striker"})") }
                )
            }

            Text("Runs completed before run-out: $runsCompleted", fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(0, 1, 2, 3).forEach { r ->
                    FilterChip(
                        selected = runsCompleted == r,
                        onClick = { runsCompleted = r },
                        label = { Text("$r") }
                    )
                }
            }
        }

        if (selectedType in listOf("CAUGHT", "RUN_OUT", "STUMPED")) {
            OutlinedTextField(
                value = fielderName,
                onValueChange = { fielderName = it },
                label = { Text("Fielder Name (Optional)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        // Did batsmen cross check
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { didCross = !didCross }
        ) {
            Checkbox(checked = didCross, onCheckedChange = { didCross = it })
            Text("Batsmen crossed before dismissal", fontSize = 13.sp)
        }

        Button(
            onClick = {
                onDismissWicket(selectedType, dismissedPlayerId, fielderName, runsCompleted, didCross)
            },
            colors = ButtonDefaults.buttonColors(containerColor = BallWicket),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CONFIRM WICKET", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/**
 * Next Batsman Selection
 */
@Composable
fun SelectNextBatsmanContent(
    striker: PlayerEntity?,
    nonStriker: PlayerEntity?,
    onSelectBatsman: (nextPlayerId: String, asStriker: Boolean) -> Unit
) {
    // Generate squad list of available batsmen
    val squad = remember {
        listOf(
            "p_next_1" to "Virat Kohli",
            "p_next_2" to "Suryakumar Yadav",
            "p_next_3" to "Hardik Pandya",
            "p_next_4" to "Ravindra Jadeja",
            "p_next_5" to "Axar Patel",
            "p_next_6" to "Rishabh Pant",
            "p_next_7" to "Sanju Samson"
        )
    }

    var selectedPlayerId by remember { mutableStateOf(squad.first().first) }
    var makeStriker by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Select Next Batsman", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LightPrimary)
        Text("A wicket has fallen. Please select the batsman entering the crease.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            items(squad) { (id, name) ->
                Surface(
                    onClick = { selectedPlayerId = id },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedPlayerId == id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        RadioButton(selected = selectedPlayerId == id, onClick = { selectedPlayerId = id })
                        Spacer(Modifier.width(8.dp))
                        Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    }
                }
            }
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { makeStriker = !makeStriker }
        ) {
            Checkbox(checked = makeStriker, onCheckedChange = { makeStriker = it })
            Text("Take strike (Facing next ball)", fontSize = 13.sp, fontWeight = FontWeight.Medium)
        }

        Button(
            onClick = { onSelectBatsman(selectedPlayerId, makeStriker) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CONFIRM BATSMAN", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Bowler Selection Sheet
 */
@Composable
fun SelectBowlerContent(
    currentBowlerId: String,
    previousBowlerId: String,
    isMidOver: Boolean,
    onSelectBowler: (newBowlerId: String, reason: String) -> Unit
) {
    val bowlingSquad = remember {
        listOf(
            "p_bowl_1" to "Jasprit Bumrah",
            "p_bowl_2" to "Mohammed Shami",
            "p_bowl_3" to "Mohammed Siraj",
            "p_bowl_4" to "Kuldeep Yadav",
            "p_bowl_5" to "Arshdeep Singh"
        )
    }

    var selectedId by remember {
        mutableStateOf(bowlingSquad.firstOrNull { it.first != previousBowlerId && it.first != currentBowlerId }?.first ?: bowlingSquad.first().first)
    }
    var midOverReason by remember { mutableStateOf("Injured") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(
            if (isMidOver) "Mid-Over Bowler Change" else "Select New Bowler",
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.primary
        )

        if (isMidOver) {
            Text("Reason for mid-over change:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf("Injured", "Suspended", "Sent off", "Other").forEach { r ->
                    FilterChip(
                        selected = midOverReason == r,
                        onClick = { midOverReason = r },
                        label = { Text(r, fontSize = 11.sp) }
                    )
                }
            }
        }

        Text("Select Bowler:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
        LazyColumn(modifier = Modifier.heightIn(max = 200.dp)) {
            items(bowlingSquad) { (id, name) ->
                val isConsecutive = id == previousBowlerId
                Surface(
                    onClick = { if (!isConsecutive) selectedId = id },
                    shape = RoundedCornerShape(8.dp),
                    color = if (selectedId == id) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    tonalElevation = 1.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(
                                selected = selectedId == id,
                                onClick = { if (!isConsecutive) selectedId = id },
                                enabled = !isConsecutive
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(name, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        }
                        if (isConsecutive) {
                            Text("Bowled prev over", fontSize = 10.sp, color = BallWicket, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { onSelectBowler(selectedId, midOverReason) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CONFIRM BOWLER", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Custom Runs Dialog
 */
@Composable
fun CustomRunsDialog(
    onDismiss: () -> Unit,
    onConfirm: (runs: Int, overthrows: Int) -> Unit
) {
    var runsText by remember { mutableStateOf("7") }
    var overthrowsText by remember { mutableStateOf("0") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Custom Runs / Overthrows", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = runsText,
                    onValueChange = { runsText = it },
                    label = { Text("Runs off bat") },
                    singleLine = true
                )
                OutlinedTextField(
                    value = overthrowsText,
                    onValueChange = { overthrowsText = it },
                    label = { Text("Overthrows (optional)") },
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                val r = runsText.toIntOrNull() ?: 0
                val ot = overthrowsText.toIntOrNull() ?: 0
                onConfirm(r, ot)
            }) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

/**
 * Wide Options Sheet
 */
@Composable
fun WideBottomSheetContent(onConfirm: (extrasRuns: Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Wide Delivery", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BallWideExtras)
        Text("Standard wide adds 1 penalty run to the team and re-bowls the ball.", fontSize = 12.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(onClick = { onConfirm(1) }, modifier = Modifier.weight(1f)) { Text("1 Wd") }
            Button(onClick = { onConfirm(2) }, modifier = Modifier.weight(1f)) { Text("1+1 (2)") }
            Button(onClick = { onConfirm(3) }, modifier = Modifier.weight(1f)) { Text("1+2 (3)") }
            Button(onClick = { onConfirm(5) }, modifier = Modifier.weight(1f)) { Text("1+4 (5)") }
        }
    }
}

/**
 * No Ball Options Sheet
 */
@Composable
fun NoBallBottomSheetContent(onConfirm: (batRuns: Int, isBye: Boolean, extrasRuns: Int) -> Unit) {
    var runsOffBat by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("No Ball Delivery", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = BallNoBall)
        Text("1 penalty run charged + next delivery becomes FREE HIT. Enter runs off the bat:", fontSize = 12.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(0, 1, 2, 3, 4, 6).forEach { r ->
                FilterChip(
                    selected = runsOffBat == r,
                    onClick = { runsOffBat = r },
                    label = { Text("$r runs") },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Button(
            onClick = { onConfirm(runsOffBat, false, 0) },
            colors = ButtonDefaults.buttonColors(containerColor = BallNoBall),
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CONFIRM NO BALL (FREE HIT NEXT)", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Bye / Leg Bye Sheet
 */
@Composable
fun ByeLegByeBottomSheetContent(onConfirm: (type: String, runs: Int) -> Unit) {
    var selectedType by remember { mutableStateOf("BYE") }
    var selectedRuns by remember { mutableStateOf(1) }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Byes & Leg Byes", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF00897B))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(
                selected = selectedType == "BYE",
                onClick = { selectedType = "BYE" },
                label = { Text("BYE (Missed bat/body)") }
            )
            FilterChip(
                selected = selectedType == "LEG_BYE",
                onClick = { selectedType = "LEG_BYE" },
                label = { Text("LEG BYE (Deflected off pad)") }
            )
        }

        Text("Runs completed:", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(1, 2, 3, 4).forEach { r ->
                FilterChip(
                    selected = selectedRuns == r,
                    onClick = { selectedRuns = r },
                    label = { Text("$r") }
                )
            }
        }

        Button(
            onClick = { onConfirm(selectedType, selectedRuns) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("CONFIRM EXTRAS", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Penalty Sheet
 */
@Composable
fun PenaltyBottomSheetContent(onConfirm: (runs: Int) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Penalty Runs", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Color(0xFF6D4C41))
        Text("Award penalty runs under MCC cricket laws (e.g. ball hits helmet, unfair play):", fontSize = 12.sp)

        Button(
            onClick = { onConfirm(5) },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("AWARD 5 PENALTY RUNS", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * DLS / Rain Interruption Calculator Sheet
 */
@Composable
fun DlsCalculatorSheetContent(
    currentMatch: com.example.data.local.MatchEntity?,
    onApply: (revisedTarget: Int, revisedOvers: Float) -> Unit
) {
    var team1Score by remember { mutableStateOf("160") }
    var team1Overs by remember { mutableStateOf("20") }
    var team2RevisedOvers by remember { mutableStateOf("15") }

    val dlsResult = remember(team1Score, team1Overs, team2RevisedOvers) {
        val s1 = team1Score.toIntOrNull() ?: 160
        val o1 = team1Overs.toFloatOrNull() ?: 20f
        val o2 = team2RevisedOvers.toFloatOrNull() ?: 15f
        DlsCalculator.calculateDlsTarget(s1, o1, o2)
    }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Rain / DLS Target Calculator", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = LightPrimary)
        Text("ICC Standard Edition DLS algorithm (G50 = ${DlsCalculator.g50}):", fontSize = 12.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = team1Score,
                onValueChange = { team1Score = it },
                label = { Text("Team 1 Runs") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            OutlinedTextField(
                value = team2RevisedOvers,
                onValueChange = { team2RevisedOvers = it },
                label = { Text("Team 2 New Overs") },
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "Revised Target: ${dlsResult.revisedTarget} runs",
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "Par Score: ${dlsResult.parScore} runs",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    dlsResult.explanation,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                )
            }
        }

        Button(
            onClick = {
                val o2 = team2RevisedOvers.toFloatOrNull() ?: 20f
                onApply(dlsResult.revisedTarget, o2)
            },
            modifier = Modifier.fillMaxWidth().height(48.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("APPLY DLS TARGET TO MATCH", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Scorecard Sheet Content
 */
@Composable
fun ScorecardSheetContent(viewModel: MahiCricViewModel) {
    val match by viewModel.currentMatch.collectAsState()
    val inningsList by viewModel.currentInningsList.collectAsState()
    val balls by viewModel.currentInningsBalls.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Text("Detailed Scorecard", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Text("${match?.teamAName} vs ${match?.teamBName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)

        LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
            items(inningsList) { inn ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            "Innings ${inn.inningsNumber}: ${inn.totalRuns}/${inn.wickets} (${inn.legalBalls / 6}.${inn.legalBalls % 6} ov)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            "Extras: ${inn.totalExtras} (w ${inn.wideRuns}, nb ${inn.noBallRuns}, b ${inn.byeRuns}, lb ${inn.legByeRuns})",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

/**
 * Commentary Sheet Content
 */
@Composable
fun CommentarySheetContent(viewModel: MahiCricViewModel) {
    val balls by viewModel.currentInningsBalls.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Ball by Ball Commentary", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        if (balls.isEmpty()) {
            Text("No balls bowled yet in this innings.", fontSize = 12.sp)
        } else {
            LazyColumn(modifier = Modifier.heightIn(max = 350.dp)) {
                items(balls.reversed()) { ball ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = if (ball.isWicket) BallWicket else LightPrimary,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    "${ball.overNumber}.${ball.ballInOver}",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                        }
                        Spacer(Modifier.width(8.dp))
                        Text(
                            ball.commentary.ifBlank { "${ball.runsOffBat} runs scored." },
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Partnerships Sheet Content
 */
@Composable
fun PartnershipsSheetContent(viewModel: MahiCricViewModel) {
    val striker by viewModel.striker.collectAsState()
    val nonStriker by viewModel.nonStriker.collectAsState()
    val balls by viewModel.currentInningsBalls.collectAsState()

    val currentPartnershipRuns = balls.filter {
        (it.strikerId == striker?.id || it.strikerId == nonStriker?.id)
    }.sumOf { it.runsOffBat + it.extrasRuns }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Partnership Details", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Current Partnership", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Text(
                    "$currentPartnershipRuns Runs",
                    fontWeight = FontWeight.Black,
                    fontSize = 22.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    "${striker?.name ?: "Striker"} & ${nonStriker?.name ?: "Non-Striker"}",
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }
    }
}

/**
 * Scorer Notes Sheet
 */
@Composable
fun ScorerNotesSheetContent(onSave: (note: String) -> Unit) {
    var noteText by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Scorer Notes", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        OutlinedTextField(
            value = noteText,
            onValueChange = { noteText = it },
            label = { Text("Add match memo / umpire note") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3
        )
        Button(
            onClick = { onSave(noteText) },
            modifier = Modifier.fillMaxWidth().height(48.dp)
        ) {
            Text("SAVE NOTE", fontWeight = FontWeight.Bold)
        }
    }
}

/**
 * Match Settings Sheet
 */
@Composable
fun MatchSettingsSheetContent(viewModel: MahiCricViewModel) {
    val isLeftHand by viewModel.isLeftHandMode.collectAsState()
    val isBatterySaver by viewModel.isBatterySaver.collectAsState()
    val isDarkMode by viewModel.isDarkMode.collectAsState()

    Column(
        modifier = Modifier.fillMaxWidth().padding(16.dp).navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Scoring Settings", fontWeight = FontWeight.Bold, fontSize = 18.sp)

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Left-Handed / One-Hand Mode (Side Rail on Left)")
            Switch(checked = isLeftHand, onCheckedChange = { viewModel.isLeftHandMode.value = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Battery Saver Mode (Dim UI)")
            Switch(checked = isBatterySaver, onCheckedChange = { viewModel.isBatterySaver.value = it })
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Dark Theme")
            Switch(checked = isDarkMode, onCheckedChange = { viewModel.isDarkMode.value = it })
        }
    }
}
