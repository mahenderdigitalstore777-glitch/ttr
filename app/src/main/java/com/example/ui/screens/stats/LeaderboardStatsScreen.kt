package com.example.ui.screens.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Star
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
import com.example.ui.MahiCricViewModel
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.LightPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LeaderboardStatsScreen(
    viewModel: MahiCricViewModel,
    modifier: Modifier = Modifier
) {
    var selectedCategory by remember { mutableStateOf(0) } // 0: Most Runs, 1: Most Wickets, 2: MVP Points
    val topRuns by viewModel.topRunScorers.collectAsState()
    val topWickets by viewModel.topWicketTakers.collectAsState()
    val topMvp by viewModel.topMvpPlayers.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Statistics & Leaderboard", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            TabRow(selectedTabIndex = selectedCategory) {
                Tab(selected = selectedCategory == 0, onClick = { selectedCategory = 0 }, text = { Text("Most Runs", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedCategory == 1, onClick = { selectedCategory = 1 }, text = { Text("Wickets", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedCategory == 2, onClick = { selectedCategory = 2 }, text = { Text("MVP Points", fontWeight = FontWeight.Bold) })
            }

            val list = when (selectedCategory) {
                0 -> topRuns
                1 -> topWickets
                else -> topMvp
            }

            if (list.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No player statistics recorded yet. Score matches to build leaderboards!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    itemsIndexed(list) { index, player ->
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface,
                            modifier = Modifier.fillMaxWidth(),
                            tonalElevation = 2.dp
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(32.dp)
                                            .clip(CircleShape)
                                            .background(if (index == 0) LightAccentGold else MaterialTheme.colorScheme.surfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            "#${index + 1}",
                                            fontWeight = FontWeight.Black,
                                            fontSize = 12.sp,
                                            color = if (index == 0) Color(0xFF422E00) else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Spacer(Modifier.width(12.dp))
                                    Column {
                                        Text(player.name, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                                        Text("${player.role} • ${player.matches} matches", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }
                                }

                                Column(horizontalAlignment = Alignment.End) {
                                    when (selectedCategory) {
                                        0 -> {
                                            Text("${player.runs} Runs", fontWeight = FontWeight.Black, fontSize = 14.sp, color = LightPrimary)
                                            Text("HS: ${player.highestScore} | 50s: ${player.fifties}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        1 -> {
                                            Text("${player.wickets} Wkts", fontWeight = FontWeight.Black, fontSize = 14.sp, color = LightPrimary)
                                            Text("Maidens: ${player.maidens}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                        else -> {
                                            Text(String.format("%.1f pts", player.mvpPoints), fontWeight = FontWeight.Black, fontSize = 14.sp, color = LightAccentGold)
                                            Text("All-round MVP", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
