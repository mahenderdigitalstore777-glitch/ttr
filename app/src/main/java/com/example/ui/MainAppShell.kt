package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.screens.home.HomeScreen
import com.example.ui.screens.innings.InningsBreakScreen
import com.example.ui.screens.innings.MatchResultScreen
import com.example.ui.screens.innings.SecondInningsSetupScreen
import com.example.ui.screens.matches.MatchesListScreen
import com.example.ui.screens.more.BackupRestoreScreen
import com.example.ui.screens.more.CricketRulesGuideScreen
import com.example.ui.screens.more.MoreScreen
import com.example.ui.screens.more.QuickScoreScreen
import com.example.ui.screens.scoring.ScoringScreen
import com.example.ui.screens.stats.LeaderboardStatsScreen
import com.example.ui.screens.teams.TeamsScreen
import com.example.ui.screens.tournaments.TournamentsScreen
import com.example.ui.screens.welcome.SplashScreen
import com.example.ui.screens.welcome.WelcomeSlidesScreen
import com.example.ui.screens.wizard.NewMatchWizardScreen
import com.example.ui.theme.LightAccentGold
import com.example.ui.theme.LightPrimary
import com.example.ui.theme.LightPrimaryDark
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppShell(
    viewModel: MahiCricViewModel,
    modifier: Modifier = Modifier
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    var currentScreen by remember { mutableStateOf("SPLASH") } // SPLASH, WELCOME, HOME, MATCHES, TOURNAMENTS, TEAMS, MORE, SCORING, WIZARD, INNINGS_BREAK, SECOND_INN_SETUP, RESULT, RULES, BACKUP, QUICK_SCORE, STATS
    var hasSeenWelcome by remember { mutableStateOf(false) }

    val activeMatch by viewModel.activeMatch.collectAsState()
    val activeState by viewModel.activeMatchState.collectAsState()
    val notifications by viewModel.notifications.collectAsState()
    val unreadNotifs = notifications.count { !it.isRead }
    var showNotifsSheet by remember { mutableStateOf(false) }

    val showResumePrompt by viewModel.showResumePrompt.collectAsState()

    // Navigation Back Handling
    BackHandler(enabled = currentScreen != "HOME") {
        when (currentScreen) {
            "SCORING" -> viewModel.showLeaveConfirmDialog.value = true
            "WIZARD", "INNINGS_BREAK", "SECOND_INN_SETUP", "RESULT", "RULES", "BACKUP", "QUICK_SCORE", "STATS", "MATCHES", "TOURNAMENTS", "TEAMS", "MORE" -> {
                currentScreen = "HOME"
            }
            else -> {}
        }
    }

    if (currentScreen == "SPLASH") {
        SplashScreen(
            onSplashFinished = {
                currentScreen = if (!hasSeenWelcome) "WELCOME" else "HOME"
            }
        )
        return
    }

    if (currentScreen == "WELCOME") {
        WelcomeSlidesScreen(
            onFinishWelcome = {
                hasSeenWelcome = true
                currentScreen = "HOME"
            }
        )
        return
    }

    // Modal Navigation Drawer
    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                drawerContainerColor = MaterialTheme.colorScheme.surface,
                modifier = Modifier.width(300.dp)
            ) {
                // Drawer Header
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            Brush.verticalGradient(
                                listOf(LightPrimary, LightPrimaryDark)
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Icon(Icons.Default.SportsCricket, contentDescription = null, tint = LightAccentGold, modifier = Modifier.size(40.dp))
                        Spacer(Modifier.height(8.dp))
                        Text("Mahi Cric Pro", fontWeight = FontWeight.Black, fontSize = 20.sp, color = Color.White)
                        Text("Offline Pro Cricket Scorer", fontSize = 12.sp, color = Color.White.copy(alpha = 0.8f))
                    }
                }

                Spacer(Modifier.height(8.dp))

                DrawerMenuItem(Icons.Default.Home, "Home") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "HOME"
                }
                DrawerMenuItem(Icons.Default.SportsBaseball, "My Matches") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "MATCHES"
                }
                DrawerMenuItem(Icons.Default.EmojiEvents, "Tournaments") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "TOURNAMENTS"
                }
                DrawerMenuItem(Icons.Default.Groups, "Teams & Squads") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "TEAMS"
                }
                DrawerMenuItem(Icons.Default.Leaderboard, "Leaderboards & Stats") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "STATS"
                }
                DrawerMenuItem(Icons.Default.Speed, "Quick Score Mode") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "QUICK_SCORE"
                }
                DrawerMenuItem(Icons.Default.MenuBook, "Cricket Rules Guide") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "RULES"
                }
                DrawerMenuItem(Icons.Default.Backup, "Backup & Restore") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "BACKUP"
                }
                DrawerMenuItem(Icons.Default.Settings, "Settings") {
                    coroutineScope.launch { drawerState.close() }
                    currentScreen = "MORE"
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                // Only show top bar on main tabs (Home, Matches, Tournaments, Teams, More)
                if (currentScreen in listOf("HOME", "MATCHES", "TOURNAMENTS", "TEAMS", "MORE")) {
                    TopAppBar(
                        title = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.SportsCricket, contentDescription = null, tint = LightAccentGold, modifier = Modifier.size(24.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(
                                    text = "Mahi Cric Pro",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 18.sp,
                                    color = Color.White
                                )
                            }
                        },
                        navigationIcon = {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menu", tint = Color.White)
                            }
                        },
                        actions = {
                            IconButton(onClick = { showNotifsSheet = true }) {
                                BadgedBox(
                                    badge = {
                                        if (unreadNotifs > 0) {
                                            Badge { Text("$unreadNotifs") }
                                        }
                                    }
                                ) {
                                    Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = Color.White)
                                }
                            }
                        },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = LightPrimary
                        )
                    )
                }
            },
            bottomBar = {
                // Bottom bar on main tabs with big center "+" button
                if (currentScreen in listOf("HOME", "MATCHES", "TOURNAMENTS", "TEAMS", "MORE")) {
                    NavigationBar(
                        containerColor = MaterialTheme.colorScheme.surface,
                        tonalElevation = 6.dp
                    ) {
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Home, contentDescription = null) },
                            label = { Text("Home", fontSize = 10.sp) },
                            selected = currentScreen == "HOME",
                            onClick = { currentScreen = "HOME" }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.SportsBaseball, contentDescription = null) },
                            label = { Text("Matches", fontSize = 10.sp) },
                            selected = currentScreen == "MATCHES",
                            onClick = { currentScreen = "MATCHES" }
                        )

                        // Big center "+" New Match Button
                        IconButton(
                            onClick = { currentScreen = "WIZARD" },
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(LightPrimary)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "New Match", tint = Color.White, modifier = Modifier.size(28.dp))
                        }

                        NavigationBarItem(
                            icon = { Icon(Icons.Default.EmojiEvents, contentDescription = null) },
                            label = { Text("Tournaments", fontSize = 10.sp) },
                            selected = currentScreen == "TOURNAMENTS",
                            onClick = { currentScreen = "TOURNAMENTS" }
                        )
                        NavigationBarItem(
                            icon = { Icon(Icons.Default.Groups, contentDescription = null) },
                            label = { Text("Teams", fontSize = 10.sp) },
                            selected = currentScreen == "TEAMS",
                            onClick = { currentScreen = "TEAMS" }
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentScreen) {
                    "HOME" -> HomeScreen(
                        viewModel = viewModel,
                        onNavigateToScoring = { currentScreen = "SCORING" },
                        onNavigateToNewMatch = { currentScreen = "WIZARD" },
                        onNavigateToTournaments = { currentScreen = "TOURNAMENTS" },
                        onNavigateToTeams = { currentScreen = "TEAMS" }
                    )
                    "MATCHES" -> MatchesListScreen(
                        viewModel = viewModel,
                        onNavigateToScoring = { currentScreen = "SCORING" }
                    )
                    "TOURNAMENTS" -> TournamentsScreen(viewModel = viewModel)
                    "TEAMS" -> TeamsScreen(viewModel = viewModel)
                    "MORE" -> MoreScreen(
                        viewModel = viewModel,
                        onNavigateToRules = { currentScreen = "RULES" },
                        onNavigateToBackup = { currentScreen = "BACKUP" },
                        onNavigateToQuickScore = { currentScreen = "QUICK_SCORE" }
                    )
                    "SCORING" -> {
                        // Check if state is INNINGS_BREAK or RESULT
                        if (activeState?.currentScreen == "INNINGS_BREAK") {
                            InningsBreakScreen(
                                viewModel = viewModel,
                                onStartSecondInningsSetup = { currentScreen = "SECOND_INN_SETUP" }
                            )
                        } else if (activeState?.currentScreen == "RESULT") {
                            MatchResultScreen(
                                viewModel = viewModel,
                                onGoHome = { currentScreen = "HOME" },
                                onRematch = { currentScreen = "WIZARD" }
                            )
                        } else {
                            ScoringScreen(
                                viewModel = viewModel,
                                onNavigateBack = { currentScreen = "HOME" }
                            )
                        }
                    }
                    "SECOND_INN_SETUP" -> SecondInningsSetupScreen(
                        viewModel = viewModel,
                        onChaseStarted = { currentScreen = "SCORING" }
                    )
                    "INNINGS_BREAK" -> InningsBreakScreen(
                        viewModel = viewModel,
                        onStartSecondInningsSetup = { currentScreen = "SECOND_INN_SETUP" }
                    )
                    "RESULT" -> MatchResultScreen(
                        viewModel = viewModel,
                        onGoHome = { currentScreen = "HOME" },
                        onRematch = { currentScreen = "WIZARD" }
                    )
                    "WIZARD" -> NewMatchWizardScreen(
                        viewModel = viewModel,
                        onMatchStarted = { currentScreen = "SCORING" },
                        onCancel = { currentScreen = "HOME" }
                    )
                    "RULES" -> CricketRulesGuideScreen(onBack = { currentScreen = "HOME" })
                    "BACKUP" -> BackupRestoreScreen(viewModel = viewModel, onBack = { currentScreen = "HOME" })
                    "QUICK_SCORE" -> QuickScoreScreen(viewModel = viewModel, onBack = { currentScreen = "HOME" })
                    "STATS" -> LeaderboardStatsScreen(viewModel = viewModel)
                }
            }
        }
    }

    // Auto-Resume Friendly Prompt (Section 5)
    if (showResumePrompt && activeMatch != null) {
        AlertDialog(
            onDismissRequest = { viewModel.showResumePrompt.value = false },
            title = {
                Text(
                    text = "Resume Unfinished Match",
                    fontWeight = FontWeight.Bold,
                    color = LightPrimary
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(
                        text = "${activeMatch?.teamAName} vs ${activeMatch?.teamBName}",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    )
                    Text(
                        text = "Your match was in progress and safely preserved in local storage.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.showResumePrompt.value = false
                        viewModel.selectMatch(activeMatch!!.id)
                        currentScreen = "SCORING"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = LightPrimary)
                ) {
                    Text("RESUME SCORING", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.showResumePrompt.value = false }) {
                    Text("Open Home")
                }
            }
        )
    }

    // Notifications Bottom Sheet
    if (showNotifsSheet) {
        ModalBottomSheet(onDismissRequest = { showNotifsSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Notifications", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                if (notifications.isEmpty()) {
                    Text("No notifications yet.", fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    notifications.forEach { notif ->
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(notif.title, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text(notif.message, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerMenuItem(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = LightPrimary, modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(14.dp))
            Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
        }
    }
}
