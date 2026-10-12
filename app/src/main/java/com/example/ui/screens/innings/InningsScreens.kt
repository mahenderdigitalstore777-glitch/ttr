package com.example.ui.screens.innings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.InningsEntity
import com.example.data.local.MatchEntity
import com.example.ui.MahiCricViewModel
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.LightPrimary
import com.example.ui.theme.LightPrimaryDark

/**
 * Full-screen Innings Break screen showing Innings 1 summary and Target
 */
@Composable
fun InningsBreakScreen(
    viewModel: MahiCricViewModel,
    onStartSecondInningsSetup: () -> Unit,
    modifier: Modifier = Modifier
) {
    val match by viewModel.currentMatch.collectAsState()
    val inningsList by viewModel.currentInningsList.collectAsState()
    val inn1 = inningsList.firstOrNull { it.inningsNumber == 1 }

    val totalRuns = inn1?.totalRuns ?: 0
    val wickets = inn1?.wickets ?: 0
    val balls = inn1?.legalBalls ?: 0
    val overs = "${balls / 6}.${balls % 6}"
    val target = totalRuns + 1
    val rrr = if (match?.totalOvers ?: 20 > 0) String.format("%.2f", target.toFloat() / (match?.totalOvers ?: 20)) else "0.00"

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                color = LightAccentGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = "INNINGS 1 COMPLETE",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = Color(0xFF8D6200),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
            Spacer(Modifier.height(12.dp))
            Text(
                text = "${match?.teamAName} Innings",
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = "$totalRuns / $wickets",
                fontWeight = FontWeight.Black,
                fontSize = 44.sp,
                color = LightPrimary
            )
            Text(
                text = "in $overs overs (RR: ${if (balls > 0) String.format("%.2f", totalRuns / (balls / 6f)) else "0.00"})",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // BIG TARGET CARD
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            elevation = CardDefaults.cardElevation(4.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "TARGET FOR ${match?.teamBName?.uppercase()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(6.dp))
                Text(
                    text = "$target RUNS",
                    fontWeight = FontWeight.Black,
                    fontSize = 38.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "in ${match?.totalOvers} overs • Required Run Rate: $rrr",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = LightPrimary
                )
            }
        }

        Button(
            onClick = onStartSecondInningsSetup,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
        ) {
            Icon(Icons.Default.PlayArrow, contentDescription = null)
            Spacer(Modifier.width(8.dp))
            Text("START SECOND INNINGS", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

/**
 * Second Innings Setup Screen: Batting & Bowling swap automatically, choose Striker, Non-striker & Opening Bowler
 */
@Composable
fun SecondInningsSetupScreen(
    viewModel: MahiCricViewModel,
    onChaseStarted: () -> Unit,
    modifier: Modifier = Modifier
) {
    val match by viewModel.currentMatch.collectAsState()

    // Batting and Bowling players (auto swapped!)
    val teamBBatsmen = remember {
        listOf(
            "p_b_1" to "Rohit Sharma",
            "p_b_2" to "Ishan Kishan",
            "p_b_3" to "Suryakumar Yadav",
            "p_b_4" to "Tilak Varma",
            "p_b_5" to "Hardik Pandya"
        )
    }

    val teamABowlers = remember {
        listOf(
            "p_a_1" to "Deepak Chahar",
            "p_a_2" to "Matheesha Pathirana",
            "p_a_3" to "Ravindra Jadeja",
            "p_a_4" to "Mustafizur Rahman"
        )
    }

    var selectedStriker by remember { mutableStateOf(teamBBatsmen[0].first) }
    var selectedNonStriker by remember { mutableStateOf(teamBBatsmen[1].first) }
    var selectedBowler by remember { mutableStateOf(teamABowlers[0].first) }

    val isValid = selectedStriker != selectedNonStriker && selectedBowler.isNotBlank()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            Text(
                text = "Second Innings Setup",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.onBackground
            )
            Text(
                text = "Batting: ${match?.teamBName} • Bowling: ${match?.teamAName}",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = LightPrimary
            )

            // Select Striker
            Text("Select Striker (*):", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                teamBBatsmen.take(3).forEach { (id, name) ->
                    FilterChip(
                        selected = selectedStriker == id,
                        onClick = { selectedStriker = id },
                        label = { Text(name, fontSize = 11.sp) }
                    )
                }
            }

            // Select Non-Striker
            Text("Select Non-Striker:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                teamBBatsmen.take(3).forEach { (id, name) ->
                    FilterChip(
                        selected = selectedNonStriker == id,
                        onClick = { selectedNonStriker = id },
                        label = { Text(name, fontSize = 11.sp) }
                    )
                }
            }

            if (selectedStriker == selectedNonStriker) {
                Text(
                    "⚠️ Striker and Non-Striker cannot be the same player!",
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Select Opening Bowler
            Text("Select Opening Bowler:", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                teamABowlers.forEach { (id, name) ->
                    FilterChip(
                        selected = selectedBowler == id,
                        onClick = { selectedBowler = id },
                        label = { Text(name, fontSize = 11.sp) }
                    )
                }
            }
        }

        Button(
            onClick = {
                viewModel.startSecondInnings(selectedStriker, selectedNonStriker, selectedBowler)
                onChaseStarted()
            },
            enabled = isValid,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("OK / START CHASE", fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

/**
 * Match Result Screen with Winner, Player of the Match, and Rematch buttons
 */
@Composable
fun MatchResultScreen(
    viewModel: MahiCricViewModel,
    onGoHome: () -> Unit,
    onRematch: () -> Unit,
    modifier: Modifier = Modifier
) {
    val match by viewModel.currentMatch.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(top = 20.dp)
        ) {
            Icon(
                Icons.Default.EmojiEvents,
                contentDescription = null,
                tint = LightAccentGold,
                modifier = Modifier.size(72.dp)
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "MATCH FINISHED",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = match?.resultSummary ?: "Match Completed",
                fontWeight = FontWeight.Black,
                fontSize = 24.sp,
                color = LightPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(20.dp))

            // Player of the match auto-suggest
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "PLAYER OF THE MATCH",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = LightAccentGold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "MS Dhoni",
                        fontWeight = FontWeight.Black,
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "52* (24) & 2 Catches • 82.5 MVP Points",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Button(
                onClick = onGoHome,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("GO TO HOME", fontWeight = FontWeight.Bold)
            }
            OutlinedButton(
                onClick = onRematch,
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text("REMATCH / NEW MATCH", fontWeight = FontWeight.Bold)
            }
        }
    }
}
