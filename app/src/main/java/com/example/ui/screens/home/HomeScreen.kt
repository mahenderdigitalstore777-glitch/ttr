package com.example.ui.screens.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.MatchEntity
import com.example.ui.MahiCricViewModel
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.LightPrimary
import com.example.ui.theme.LightPrimaryDark

@Composable
fun HomeScreen(
    viewModel: MahiCricViewModel,
    onNavigateToScoring: (matchId: String) -> Unit,
    onNavigateToNewMatch: () -> Unit,
    onNavigateToTournaments: () -> Unit,
    onNavigateToTeams: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeMatch by viewModel.activeMatch.collectAsState()
    val allMatches by viewModel.allMatches.collectAsState()
    var searchQuery by remember { mutableStateOf("") }

    val filteredMatches = remember(allMatches, searchQuery) {
        if (searchQuery.isBlank()) allMatches
        else allMatches.filter {
            it.teamAName.contains(searchQuery, ignoreCase = true) ||
            it.teamBName.contains(searchQuery, ignoreCase = true) ||
            it.venue.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Search Bar
        item {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search matches, teams, tournaments…", fontSize = 13.sp) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                shape = RoundedCornerShape(24.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                    focusedContainerColor = MaterialTheme.colorScheme.surface
                ),
                singleLine = true
            )
        }

        // Active Match "RESUME SCORING" Hero Banner (Section 5 Auto-Resume)
        if (activeMatch != null) {
            item {
                ActiveMatchResumeCard(
                    match = activeMatch!!,
                    onResume = {
                        viewModel.selectMatch(activeMatch!!.id)
                        onNavigateToScoring(activeMatch!!.id)
                    }
                )
            }
        }

        // Quick Actions Row (New Match, New Tournament, New Team, Quick Score)
        item {
            QuickActionsRow(
                onNewMatch = onNavigateToNewMatch,
                onNewTournament = onNavigateToTournaments,
                onNewTeam = onNavigateToTeams
            )
        }

        // Match of the Day Featured Card
        item {
            MatchOfTheDayCard()
        }

        // Recent Matches Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Recent Matches",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = "${filteredMatches.size} Total",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        if (filteredMatches.isEmpty()) {
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("No matches found.", fontWeight = FontWeight.SemiBold)
                        Text("Tap + or New Match to score a match ball by ball!", fontSize = 12.sp)
                    }
                }
            }
        } else {
            items(filteredMatches) { match ->
                MatchCardItem(
                    match = match,
                    onClick = {
                        viewModel.selectMatch(match.id)
                        onNavigateToScoring(match.id)
                    }
                )
            }
        }

        item {
            Spacer(Modifier.height(80.dp)) // Extra padding for bottom bar
        }
    }
}

/**
 * Prominent Resume Match card for unfinished matches
 */
@Composable
fun ActiveMatchResumeCard(
    match: MatchEntity,
    onResume: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onResume() },
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Box(
            modifier = Modifier
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(LightPrimary, LightPrimaryDark)
                    )
                )
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = LightAccentGold,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "⚡ MATCH IN PROGRESS",
                            fontWeight = FontWeight.Black,
                            fontSize = 10.sp,
                            color = Color(0xFF382300),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "Auto-saved",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "${match.teamAName} vs ${match.teamBName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.White
                )

                Text(
                    text = "${match.matchType} • ${match.venue} • Innings ${match.currentInnings}",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 12.sp
                )

                Button(
                    onClick = onResume,
                    colors = ButtonDefaults.buttonColors(containerColor = LightAccentGold),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth().height(44.dp)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF2B1D00))
                    Spacer(Modifier.width(6.dp))
                    Text(
                        text = "RESUME SCORING",
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF2B1D00),
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

/**
 * Quick Actions: New Match, New Tournament, Teams
 */
@Composable
fun QuickActionsRow(
    onNewMatch: () -> Unit,
    onNewTournament: () -> Unit,
    onNewTeam: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        QuickActionButton(
            title = "New Match",
            subtitle = "Ball by Ball",
            icon = Icons.Default.AddCircle,
            color = LightPrimary,
            onClick = onNewMatch,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "Tournament",
            subtitle = "Points & Fixtures",
            icon = Icons.Default.EmojiEvents,
            color = Color(0xFF1E88E5),
            onClick = onNewTournament,
            modifier = Modifier.weight(1f)
        )
        QuickActionButton(
            title = "Teams",
            subtitle = "Squad & Players",
            icon = Icons.Default.Groups,
            color = Color(0xFF8E24AA),
            onClick = onNewTeam,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun QuickActionButton(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surface,
        modifier = modifier.height(84.dp),
        tonalElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = color, modifier = Modifier.size(24.dp))
            Column {
                Text(title, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                Text(subtitle, fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

/**
 * Match of the day card
 */
@Composable
fun MatchOfTheDayCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("🔥 MATCH OF THE DAY", fontWeight = FontWeight.Bold, fontSize = 11.sp, color = LightAccentGold)
                Text("Pro Premier League", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("CSK vs MI", fontWeight = FontWeight.Black, fontSize = 16.sp)
                    Text("Wankhede Stadium, Mumbai", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Surface(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Starts Today",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

/**
 * Match Card Item for match list
 */
@Composable
fun MatchCardItem(
    match: MatchEntity,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${match.matchType} • ${match.venue}",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = match.status.replace("_", " "),
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    color = if (match.status == "IN_PROGRESS") LightPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${match.teamAName} vs ${match.teamBName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )
            }

            if (match.resultSummary.isNotBlank()) {
                Text(
                    text = match.resultSummary,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = LightPrimary
                )
            }
        }
    }
}
