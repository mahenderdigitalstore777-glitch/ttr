package com.example.ui.screens.tournaments

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.TournamentEntity
import com.example.data.local.TournamentTeamEntity
import com.example.ui.MahiCricViewModel
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.LightPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TournamentsScreen(
    viewModel: MahiCricViewModel,
    modifier: Modifier = Modifier
) {
    val tournaments by viewModel.allTournaments.collectAsState()
    var selectedTournamentId by remember { mutableStateOf<String?>(null) }
    var selectedTab by remember { mutableStateOf(0) } // 0: Points Table, 1: Fixtures

    val currentTournament = remember(tournaments, selectedTournamentId) {
        tournaments.firstOrNull { it.id == selectedTournamentId } ?: tournaments.firstOrNull()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tournaments & Leagues", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
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
            if (tournaments.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No tournaments yet. Create your first league!", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                // Tournament Selector Chip Row
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tournaments.forEach { t ->
                        val isSelected = currentTournament?.id == t.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedTournamentId = t.id },
                            label = { Text(t.name, fontWeight = FontWeight.SemiBold) }
                        )
                    }
                }

                // Tab Row: Points Table vs Fixtures
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text("Points Table", fontWeight = FontWeight.Bold) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text("Fixtures", fontWeight = FontWeight.Bold) }
                    )
                }

                if (selectedTab == 0) {
                    // Points Table with NRR
                    PointsTableContent()
                } else {
                    // Fixtures List
                    FixturesContent()
                }
            }
        }
    }
}

@Composable
fun PointsTableContent() {
    val standings = remember {
        listOf(
            StandingsRow("CSK", "Chennai Super Kings", 4, 3, 1, 0, 6, "+0.850"),
            StandingsRow("MI", "Mumbai Indians", 4, 2, 2, 0, 4, "+0.220"),
            StandingsRow("RCB", "Royal Challengers", 4, 2, 2, 0, 4, "-0.150"),
            StandingsRow("KKR", "Kolkata Knight Riders", 4, 1, 3, 0, 2, "-0.920")
        )
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            // Header Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(8.dp))
                    .padding(vertical = 8.dp, horizontal = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Pos", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(28.dp))
                Text("Team", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.weight(1f))
                Text("P", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(22.dp))
                Text("W", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(22.dp))
                Text("L", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(22.dp))
                Text("Pts", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(28.dp), color = LightPrimary)
                Text("NRR", fontWeight = FontWeight.Bold, fontSize = 11.sp, modifier = Modifier.width(48.dp))
            }

            Spacer(Modifier.height(4.dp))

            // Rows
            standings.forEachIndexed { index, row ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp, horizontal = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("${index + 1}", fontWeight = FontWeight.Bold, fontSize = 12.sp, modifier = Modifier.width(28.dp))
                    Text(row.shortName, fontWeight = FontWeight.SemiBold, fontSize = 12.sp, modifier = Modifier.weight(1f))
                    Text("${row.played}", fontSize = 12.sp, modifier = Modifier.width(22.dp))
                    Text("${row.won}", fontSize = 12.sp, modifier = Modifier.width(22.dp))
                    Text("${row.lost}", fontSize = 12.sp, modifier = Modifier.width(22.dp))
                    Text("${row.points}", fontWeight = FontWeight.Black, fontSize = 13.sp, modifier = Modifier.width(28.dp), color = LightPrimary)
                    Text(row.nrr, fontSize = 11.sp, modifier = Modifier.width(48.dp), color = if (row.nrr.startsWith("+")) LightPrimary else MaterialTheme.colorScheme.error)
                }
                if (index < standings.size - 1) {
                    HorizontalDivider(thickness = 0.5.dp, color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f))
                }
            }
        }
    }
}

data class StandingsRow(
    val shortName: String,
    val fullName: String,
    val played: Int,
    val won: Int,
    val lost: Int,
    val tied: Int,
    val points: Int,
    val nrr: String
)

@Composable
fun FixturesContent() {
    val fixtures = remember {
        listOf(
            "Match 1" to "CSK vs MI (Wankhede, Today)",
            "Match 2" to "RCB vs KKR (Chinnaswamy, Tomorrow)",
            "Match 3" to "CSK vs RCB (Chepauk, Oct 15)",
            "Match 4" to "MI vs KKR (Wankhede, Oct 17)"
        )
    }

    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(fixtures) { (m, details) ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(m, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text(details, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = LightAccentGold)
                }
            }
        }
    }
}
