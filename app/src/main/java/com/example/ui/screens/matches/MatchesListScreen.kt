package com.example.ui.screens.matches

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.MahiCricViewModel
import com.example.ui.screens.home.MatchCardItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchesListScreen(
    viewModel: MahiCricViewModel,
    onNavigateToScoring: (matchId: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val allMatches by viewModel.allMatches.collectAsState()
    var selectedTab by remember { mutableStateOf(0) } // 0: All, 1: Live, 2: Completed

    val filtered = remember(allMatches, selectedTab) {
        when (selectedTab) {
            1 -> allMatches.filter { it.status == "IN_PROGRESS" || it.status == "INNINGS_BREAK" }
            2 -> allMatches.filter { it.status == "COMPLETED" }
            else -> allMatches
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Match History & Fixtures", fontWeight = FontWeight.Bold, fontSize = 18.sp) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            TabRow(selectedTabIndex = selectedTab) {
                Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("All", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("Live", fontWeight = FontWeight.Bold) })
                Tab(selected = selectedTab == 2, onClick = { selectedTab = 2 }, text = { Text("Completed", fontWeight = FontWeight.Bold) })
            }

            if (filtered.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = androidx.compose.ui.Alignment.Center) {
                    Text("No matches in this category.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtered) { match ->
                        MatchCardItem(
                            match = match,
                            onClick = {
                                viewModel.selectMatch(match.id)
                                onNavigateToScoring(match.id)
                            }
                        )
                    }
                }
            }
        }
    }
}
