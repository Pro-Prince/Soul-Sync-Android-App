package com.example

import android.os.Bundle
import android.content.Intent
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import compose.icons.TablerIcons
import compose.icons.tablericons.Moon
import compose.icons.tablericons.Sun
import compose.icons.tablericons.Home
import compose.icons.tablericons.Pencil
import compose.icons.tablericons.Heart
import compose.icons.tablericons.Droplet
import compose.icons.tablericons.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.theme.SoulSyncTheme
import com.example.data.SettingsRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.runBlocking
import androidx.compose.material.icons.Icons
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.graphics.vector.ImageVector
import com.example.di.AppContainer
import com.example.ui.screens.home.HomeScreen
import com.example.ui.components.CelebrationDialog
import com.example.ui.components.getCelebrationInfo
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope
import com.example.data.supabase.SupabaseClient

sealed class BottomTab(
    val route: String,
    val title: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    object Home : BottomTab("tab_home", "Home", TablerIcons.Home, TablerIcons.Home)
    object Diary : BottomTab("tab_diary", "Diary", TablerIcons.Pencil, TablerIcons.Pencil)
    object Moods : BottomTab("tab_moods", "Moods", TablerIcons.Heart, TablerIcons.Heart)
    object Cycle : BottomTab("tab_cycle", "Cycle", TablerIcons.Droplet, TablerIcons.Droplet)
    object Settings : BottomTab("tab_settings", "Settings", TablerIcons.Settings, TablerIcons.Settings)
}

val bottomTabsList = listOf(
    BottomTab.Home,
    BottomTab.Diary,
    BottomTab.Moods,
    BottomTab.Cycle,
    BottomTab.Settings
)

class MainActivity : ComponentActivity() {

    private val intentFlow = MutableSharedFlow<Intent>(extraBufferCapacity = 1)

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        intentFlow.tryEmit(intent)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val appContainer = (application as SoulSyncApplication).container
        
        lifecycleScope.launch {
            appContainer.diaryRepository.cleanUpDuplicates()
            appContainer.moodRepository.cleanUpDuplicates()
            appContainer.achievementRepository.initDefaultAchievements()
            appContainer.notificationPreferencesRepository.updateLastActiveTimestamp(System.currentTimeMillis())
            try {
                val session = SupabaseClient.auth.currentSessionOrNull()
                if (session != null) {
                    Log.d("AUTH", "Restored existing Supabase session: ${session.user?.email}")
                } else {
                    Log.d("AUTH", "No active Supabase session found")
                }
            } catch (e: Exception) {
                Log.e("AUTH", "Failed to check Supabase session", e)
            }
        }

        setContent {
            val isDarkModePref by appContainer.settingsRepository.darkMode.collectAsState(initial = null)
            val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
            val isDarkMode = isDarkModePref ?: isSystemDark

            val themeName by appContainer.settingsRepository.colorTheme.collectAsState(initial = "SOUL_PINK")
            
            SoulSyncTheme(
                themeName = themeName,
                forceDark = isDarkMode
            ) {
                MainScreen(appContainer, intentFlow) { signOut(appContainer) }
            }
        }
    }

    private fun signOut(appContainer: AppContainer) {
        lifecycleScope.launch {
            try {
                SupabaseClient.auth.signOut()
            } catch (e: Exception) {
                Log.e("AUTH", "Failed to sign out from Supabase", e)
            }

            try {
                val credentialManager = androidx.credentials.CredentialManager.create(this@MainActivity)
                credentialManager.clearCredentialState(androidx.credentials.ClearCredentialStateRequest())
            } catch (e: Exception) {
                Log.e("AUTH", "Failed to clear Credential Manager state", e)
            }

            appContainer.settingsRepository.clearUserCredentials()
            (application as SoulSyncApplication).resetContainer()
            val intent = Intent(this@MainActivity, AuthActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            startActivity(intent)
            finish()
        }
    }
}

@Composable
fun MainTopBar(appContainer: AppContainer) {
    val coroutineScope = rememberCoroutineScope()
    val isDarkModePref by appContainer.settingsRepository.darkMode.collectAsState(initial = null)
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val isDarkMode = isDarkModePref ?: isSystemDark
    
    val bgColor = MaterialTheme.colorScheme.background
    val textColor = MaterialTheme.colorScheme.onBackground

    Surface(
        color = bgColor,
        tonalElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .heightIn(min = 64.dp)
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_transparent),
                    contentDescription = "Soul Sync Logo Icon",
                    modifier = Modifier.size(36.dp),
                    contentScale = ContentScale.Fit
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Soul Sync",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = textColor
                )
            }
            
            IconButton(
                onClick = {
                    coroutineScope.launch {
                        appContainer.settingsRepository.setDarkMode(!isDarkMode)
                    }
                }
            ) {
                Icon(
                    imageVector = if (isDarkMode) TablerIcons.Moon else TablerIcons.Sun,
                    contentDescription = "Toggle Dark Mode",
                    tint = textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun MainScreen(
    appContainer: AppContainer,
    intentFlow: SharedFlow<Intent>,
    onSignOutClick: () -> Unit
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isCycleEnabled by appContainer.settingsRepository.periodTrackerEnabled.collectAsState(initial = false)
    val snackbarHostState = remember { SnackbarHostState() }
    var activeCelebration by remember { mutableStateOf<com.example.ui.components.CelebrationInfo?>(null) }
    
    LaunchedEffect(navController) {
        // Cold start intent handling
        val activity = context as? MainActivity
        activity?.intent?.let {
            com.example.notification.NotificationDeepLinkHandler.handleIntent(it, navController)
        }

        // Hot start intent handling dynamically
        intentFlow.collect { intent ->
            com.example.notification.NotificationDeepLinkHandler.handleIntent(intent, navController)
        }
    }
    
    LaunchedEffect(isCycleEnabled) {
        if (!isCycleEnabled) {
            val currentRoute = navController.currentDestination?.route
            if (currentRoute == BottomTab.Cycle.route) {
                navController.navigate(BottomTab.Home.route) {
                    popUpTo(navController.graph.findStartDestination().id) {
                        saveState = true
                    }
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        appContainer.achievementRepository.newlyUnlocked.collect { badgeId ->
            val info = getCelebrationInfo(badgeId)
            activeCelebration = info
            
            val name = com.example.ui.screens.settings.getNameForBadge(badgeId)
            val result = snackbarHostState.showSnackbar(
                message = "Achievement unlocked: $name!",
                actionLabel = "View",
                duration = SnackbarDuration.Short
            )
            if (result == SnackbarResult.ActionPerformed) {
                navController.navigate("tab_settings") { // navigate to Settings tab
                    launchSingleTop = true
                    restoreState = true
                }
            }
        }
    }

    val visibleTabs = remember(isCycleEnabled) {
        if (isCycleEnabled) bottomTabsList else bottomTabsList.filter { it != BottomTab.Cycle }
    }

    val navBackStackEntryState by navController.currentBackStackEntryAsState()
    val currentRouteName = navBackStackEntryState?.destination?.route
    val isRootTabScreen = currentRouteName in visibleTabs.map { it.route }
    val isNotHomeScreen = currentRouteName != BottomTab.Home.route

    BackHandler(enabled = isRootTabScreen && isNotHomeScreen) {
        navController.navigate(BottomTab.Home.route) {
            popUpTo(navController.graph.findStartDestination().id) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { MainTopBar(appContainer) },
        bottomBar = {
            val navBackStackEntry by navController.currentBackStackEntryAsState()
            val currentDestination = navBackStackEntry?.destination
            val currentRoute = currentDestination?.route
            
            if (currentRoute in visibleTabs.map { it.route }) {
                Surface(
                    color = MaterialTheme.colorScheme.surface,
                    shadowElevation = 8.dp,
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .navigationBarsPadding()
                            .height(80.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        visibleTabs.forEach { tab ->
                            val isSelected = currentDestination?.hierarchy?.any { it.route == tab.route } == true
                            val activeColor = MaterialTheme.colorScheme.primary
                            val inactiveColor = MaterialTheme.colorScheme.onSurfaceVariant
                            
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight()
                                    .clickable(
                                        interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() },
                                        indication = androidx.compose.material3.ripple()
                                    ) {
                                        navController.navigate(tab.route) {
                                            popUpTo(navController.graph.findStartDestination().id) {
                                                saveState = true
                                            }
                                            launchSingleTop = true
                                            restoreState = true
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent)
                                            .padding(horizontal = 20.dp, vertical = 6.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                            contentDescription = tab.title,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (isSelected) activeColor else inactiveColor
                                        )
                                    }
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Medium,
                                        letterSpacing = 0.5.sp,
                                        color = if (isSelected) activeColor else inactiveColor
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = BottomTab.Home.route,
            modifier = Modifier.padding(innerPadding).fillMaxSize(),
            enterTransition = {
                val isTabSwitch = initialState.destination.route in visibleTabs.map { it.route } && targetState.destination.route in visibleTabs.map { it.route }
                if (isTabSwitch) {
                    fadeIn(animationSpec = tween(200)) + scaleIn(initialScale = 0.92f, animationSpec = tween(200))
                } else {
                    slideIntoContainer(
                        AnimatedContentTransitionScope.SlideDirection.Up,
                        animationSpec = tween(240, easing = FastOutSlowInEasing)
                    ) + fadeIn(animationSpec = tween(240))
                }
            },
            exitTransition = {
                val isTabSwitch = initialState.destination.route in visibleTabs.map { it.route } && targetState.destination.route in visibleTabs.map { it.route }
                if (isTabSwitch) {
                    fadeOut(animationSpec = tween(160))
                } else {
                    fadeOut(animationSpec = tween(160))
                }
            },
            popEnterTransition = {
                fadeIn(animationSpec = tween(200))
            },
            popExitTransition = {
                slideOutOfContainer(
                    AnimatedContentTransitionScope.SlideDirection.Down,
                    animationSpec = tween(200, easing = LinearOutSlowInEasing)
                ) + fadeOut(animationSpec = tween(200))
            }
        ) {
            composable(BottomTab.Home.route) {
                HomeScreen(
                    appContainer = appContainer,
                    onNavigateToNewEntry = {
                        navController.navigate("new_entry")
                    },
                    onNavigateToDiary = {
                        navController.navigate(BottomTab.Diary.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMoods = {
                        navController.navigate(BottomTab.Moods.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToSettings = {
                        navController.navigate(BottomTab.Settings.route) {
                            popUpTo(navController.graph.findStartDestination().id) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToEntryDetail = { entryId ->
                        navController.navigate("entry_detail/$entryId")
                    }
                )
            }
            composable(BottomTab.Diary.route) {
                com.example.ui.screens.diary.DiaryScreen(
                    appContainer = appContainer,
                    onNavigateToNewEntry = { navController.navigate("new_entry") },
                    onNavigateToEntryDetail = { id -> navController.navigate("entry_detail/$id") }
                )
            }
            composable(BottomTab.Moods.route) {
                com.example.ui.screens.moods.MoodsScreen(
                    appContainer = appContainer,
                    onNavigateToEntryDetail = { id -> navController.navigate("entry_detail/$id") },
                    onNavigateToHome = {
                        navController.navigate(BottomTab.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomTab.Cycle.route) {
                com.example.ui.screens.cycle.CycleScreen(
                    appContainer = appContainer,
                    onNavigateToHome = {
                        navController.navigate(BottomTab.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
            composable(BottomTab.Settings.route) {
                com.example.ui.screens.settings.SettingsScreen(
                    appContainer = appContainer,
                    onNavigateToHome = {
                        navController.navigate(BottomTab.Home.route) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSignOut = onSignOutClick
                )
            }
            composable(
                route = "new_entry?entryId={entryId}",
                arguments = listOf(androidx.navigation.navArgument("entryId") {
                    type = androidx.navigation.NavType.StringType
                    nullable = true
                    defaultValue = null
                })
            ) { navBackEntry ->
                val entryId = navBackEntry.arguments?.getString("entryId")
                com.example.ui.screens.diary.NewEntryScreen(
                    appContainer = appContainer,
                    entryId = entryId,
                    onBack = { navController.popBackStack() },
                    onEntrySaved = { id -> 
                        navController.popBackStack()
                        if (entryId == null) {
                            navController.navigate("entry_detail/$id")
                        }
                    }
                )
            }
            composable("entry_detail/{id}") { navBackEntry ->
                val id = navBackEntry.arguments?.getString("id") ?: ""
                com.example.ui.screens.diary.DiaryEntryDetailScreen(
                    entryId = id,
                    appContainer = appContainer,
                    onBack = { navController.popBackStack() },
                    onEdit = { entryId -> 
                        navController.navigate("new_entry?entryId=$entryId")
                    }
                )
            }
        }
    }

    activeCelebration?.let { celebration ->
        CelebrationDialog(
            info = celebration,
            onDismiss = { activeCelebration = null }
        )
    }
}
