package com.example.ui.screens.teams

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.PlayerEntity
import com.example.data.local.TeamEntity
import com.example.ui.MahiCricViewModel
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.LightPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeamsScreen(
    viewModel: MahiCricViewModel,
    modifier: Modifier = Modifier
) {
    val teams by viewModel.allTeams.collectAsState()
    var selectedTeam by remember { mutableStateOf<TeamEntity?>(null) }
    var showAddPlayerDialog by remember { mutableStateOf(false) }

    LaunchedEffect(teams) {
        if (selectedTeam == null && teams.isNotEmpty()) {
            selectedTeam = teams.first()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Teams & Squads", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = { showAddPlayerDialog = true },
                containerColor = LightPrimary,
                contentColor = Color.White
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Add Player", fontWeight = FontWeight.Bold)
            }
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Teams Horizontal Row
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                item {
                    Text("Select Team", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        teams.forEach { team ->
                            val isSelected = selectedTeam?.id == team.id
                            FilterChip(
                                selected = isSelected,
                                onClick = { selectedTeam = team },
                                label = { Text(team.shortName, fontWeight = FontWeight.Bold) },
                                leadingIcon = {
                                    Icon(
                                        Icons.Default.Shield,
                                        contentDescription = null,
                                        tint = if (isSelected) LightPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            )
                        }
                    }
                }

                selectedTeam?.let { team ->
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(team.name, fontWeight = FontWeight.Black, fontSize = 20.sp)
                                Text("City: ${team.city} • Code: ${team.shortName}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    item {
                        Text("Squad Members", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    // Squad players sample
                    val squad = listOf(
                        PlayerEntity("p1", "MS Dhoni", team.id, "Wicketkeeper", jerseyNumber = 7, isCaptain = true, runs = 5243, matches = 264),
                        PlayerEntity("p2", "Ruturaj Gaikwad", team.id, "Batsman", jerseyNumber = 31, runs = 2380, matches = 66),
                        PlayerEntity("p3", "Ravindra Jadeja", team.id, "All-Rounder", jerseyNumber = 8, runs = 2959, wickets = 160, matches = 240),
                        PlayerEntity("p4", "Shivam Dube", team.id, "All-Rounder", jerseyNumber = 25, runs = 1500, matches = 60),
                        PlayerEntity("p5", "Deepak Chahar", team.id, "Bowler", jerseyNumber = 90, wickets = 77, matches = 81),
                        PlayerEntity("p6", "Matheesha Pathirana", team.id, "Bowler", jerseyNumber = 99, wickets = 34, matches = 20)
                    )

                    items(squad) { player ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
                            tonalElevation = 1.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(36.dp)
                                            .clip(CircleShape)
                                            .background(LightPrimary.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "#${player.jerseyNumber}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp,
                                            color = LightPrimary
                                        )
                                    }
                                    Spacer(Modifier.width(10.dp))
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(player.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                            if (player.isCaptain) {
                                                Spacer(Modifier.width(4.dp))
                                                Text("(C)", fontSize = 10.sp, fontWeight = FontWeight.Bold, color = LightAccentGold)
                                            }
                                        }
                                        Text("${player.role} • ${player.battingStyle}", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    Text("${player.runs} Runs", fontWeight = FontWeight.Bold, fontSize = 12.sp, color = LightPrimary)
                                    if (player.wickets > 0) {
                                        Text("${player.wickets} Wkts", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddPlayerDialog) {
        var playerName by remember { mutableStateOf("") }
        var playerRole by remember { mutableStateOf("Batsman") }

        AlertDialog(
            onDismissRequest = { showAddPlayerDialog = false },
            title = { Text("Add Player to Squad") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = playerName,
                        onValueChange = { playerName = it },
                        label = { Text("Player Name") },
                        singleLine = true
                    )
                    Text("Role:", fontSize = 12.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf("Batsman", "Bowler", "All-Rounder", "Keeper").forEach { r ->
                            FilterChip(
                                selected = playerRole == r,
                                onClick = { playerRole = r },
                                label = { Text(r, fontSize = 10.sp) }
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    showAddPlayerDialog = false
                }) {
                    Text("Add")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddPlayerDialog = false }) { Text("Cancel") }
            }
        )
    }
}
