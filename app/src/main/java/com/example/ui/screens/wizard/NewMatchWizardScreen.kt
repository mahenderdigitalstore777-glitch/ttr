package com.example.ui.screens.wizard

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Flip
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MahiCricViewModel
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.LightPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewMatchWizardScreen(
    viewModel: MahiCricViewModel,
    onMatchStarted: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    var step by remember { mutableStateOf(1) } // Steps 1 to 7

    // Step 1: Teams
    var teamAName by remember { mutableStateOf("Chennai Super Kings") }
    var teamAShort by remember { mutableStateOf("CSK") }
    var teamBName by remember { mutableStateOf("Mumbai Indians") }
    var teamBShort by remember { mutableStateOf("MI") }

    // Step 2 & 3: Format & Overs
    var selectedFormat by remember { mutableStateOf("T20") }
    var overs by remember { mutableStateOf(20) }
    var ballsPerOver by remember { mutableStateOf(6) }
    var playersPerSide by remember { mutableStateOf(11) }

    // Step 4 & 5: Match Type & Venue
    var matchType by remember { mutableStateOf("Friendly") }
    var venue by remember { mutableStateOf("Wankhede Stadium") }
    var umpires by remember { mutableStateOf("Anil Chaudhary, Nitin Menon") }

    // Step 6: Toss
    var tossWinner by remember { mutableStateOf("Team A") }
    var tossDecision by remember { mutableStateOf("BAT") }
    var coinFlipped by remember { mutableStateOf(false) }
    val coinRotation by animateFloatAsState(
        targetValue = if (coinFlipped) 720f else 0f,
        label = "coin_toss"
    )

    // Step 7: Openers
    var strikerName by remember { mutableStateOf("Ruturaj Gaikwad") }
    var nonStrikerName by remember { mutableStateOf("MS Dhoni") }
    var bowlerName by remember { mutableStateOf("Jasprit Bumrah") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Match Setup (Step $step of 7)", fontWeight = FontWeight.Bold, fontSize = 16.sp) },
                navigationIcon = {
                    IconButton(onClick = { if (step > 1) step-- else onCancel() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Step Progress Indicator
                LinearProgressIndicator(
                    progress = { step / 7f },
                    modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
                )

                when (step) {
                    1 -> {
                        Text("Step 1: Select or Enter Teams", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        OutlinedTextField(
                            value = teamAName,
                            onValueChange = {
                                teamAName = it
                                teamAShort = it.take(4).uppercase()
                            },
                            label = { Text("Team A Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = teamAShort,
                            onValueChange = { teamAShort = it },
                            label = { Text("Team A Short Code (e.g. CSK)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = teamBName,
                            onValueChange = {
                                teamBName = it
                                teamBShort = it.take(4).uppercase()
                            },
                            label = { Text("Team B Name") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = teamBShort,
                            onValueChange = { teamBShort = it },
                            label = { Text("Team B Short Code (e.g. MI)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    2 -> {
                        Text("Step 2: Squad & Playing XI", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Text("Players per side: $playersPerSide", fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(6, 8, 11, 15).forEach { p ->
                                FilterChip(
                                    selected = playersPerSide == p,
                                    onClick = { playersPerSide = p },
                                    label = { Text("$p a side") }
                                )
                            }
                        }
                    }

                    3 -> {
                        Text("Step 3: Match Format & Overs", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            listOf("T5" to 5, "T10" to 10, "T20" to 20, "50-Over" to 50, "Custom" to 15).forEach { (f, o) ->
                                FilterChip(
                                    selected = selectedFormat == f,
                                    onClick = {
                                        selectedFormat = f
                                        overs = o
                                    },
                                    label = { Text(f) }
                                )
                            }
                        }
                        OutlinedTextField(
                            value = overs.toString(),
                            onValueChange = { overs = it.toIntOrNull() ?: 20 },
                            label = { Text("Overs per innings (1 - 100)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = ballsPerOver.toString(),
                            onValueChange = { ballsPerOver = it.toIntOrNull() ?: 6 },
                            label = { Text("Balls per over (default: 6)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    4 -> {
                        Text("Step 4: Match Type", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Friendly", "Tournament", "Practice / Nets").forEach { t ->
                                FilterChip(
                                    selected = matchType == t,
                                    onClick = { matchType = t },
                                    label = { Text(t) }
                                )
                            }
                        }
                    }

                    5 -> {
                        Text("Step 5: Ground & Officials", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        OutlinedTextField(
                            value = venue,
                            onValueChange = { venue = it },
                            label = { Text("Venue / Ground") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = umpires,
                            onValueChange = { umpires = it },
                            label = { Text("Umpires") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    6 -> {
                        Text("Step 6: Coin Toss", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(90.dp)
                                    .graphicsLayer { rotationY = coinRotation }
                                    .clip(CircleShape)
                                    .background(LightAccentGold)
                                    .border(2.dp, Color(0xFFC79200), CircleShape)
                                    .clickable { coinFlipped = !coinFlipped },
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    if (coinRotation.toInt() % 360 > 180) "TAILS" else "HEADS",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 15.sp,
                                    color = Color(0xFF4A3400)
                                )
                            }
                            TextButton(onClick = { coinFlipped = !coinFlipped }) {
                                Icon(Icons.Default.Flip, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text("Flip Coin")
                            }
                        }

                        Text("Toss Winner:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = tossWinner == "Team A",
                                onClick = { tossWinner = "Team A" },
                                label = { Text(teamAName) }
                            )
                            FilterChip(
                                selected = tossWinner == "Team B",
                                onClick = { tossWinner = "Team B" },
                                label = { Text(teamBName) }
                            )
                        }

                        Text("Elected To:", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            FilterChip(
                                selected = tossDecision == "BAT",
                                onClick = { tossDecision = "BAT" },
                                label = { Text("BAT FIRST") }
                            )
                            FilterChip(
                                selected = tossDecision == "BOWL",
                                onClick = { tossDecision = "BOWL FIRST" },
                                label = { Text("BOWL FIRST") }
                            )
                        }
                    }

                    7 -> {
                        Text("Step 7: Opening Players", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        OutlinedTextField(
                            value = strikerName,
                            onValueChange = { strikerName = it },
                            label = { Text("Opening Striker (*)") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = nonStrikerName,
                            onValueChange = { nonStrikerName = it },
                            label = { Text("Opening Non-Striker") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = bowlerName,
                            onValueChange = { bowlerName = it },
                            label = { Text("Opening Bowler") },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }

            // Navigation buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (step > 1) {
                    OutlinedButton(onClick = { step-- }) {
                        Text("Back")
                    }
                } else {
                    Spacer(Modifier.width(8.dp))
                }

                Button(
                    onClick = {
                        if (step < 7) {
                            step++
                        } else {
                            viewModel.createMatch(
                                teamAId = "team_a_${System.currentTimeMillis()}",
                                teamBId = "team_b_${System.currentTimeMillis()}",
                                teamAName = teamAName,
                                teamBName = teamBName,
                                teamAShort = teamAShort,
                                teamBShort = teamBShort,
                                overs = overs,
                                ballsPerOver = ballsPerOver,
                                matchType = selectedFormat,
                                venue = venue,
                                tossWinnerId = if (tossWinner == "Team A") "team_a" else "team_b",
                                tossDecision = tossDecision,
                                strikerId = "p_str_$strikerName",
                                nonStrikerId = "p_non_$nonStrikerName",
                                bowlerId = "p_bowl_$bowlerName"
                            )
                            onMatchStarted()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
                ) {
                    Text(if (step == 7) "START MATCH" else "Next")
                }
            }
        }
    }
}
