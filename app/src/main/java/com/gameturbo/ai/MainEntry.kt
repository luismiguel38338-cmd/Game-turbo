package com.gameturbo.ai

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SportsEsports
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.R
import com.gameturbo.ai.advanced.AdvancedToolsScreen
import com.gameturbo.ai.advanced.AdvancedViewModel
import com.gameturbo.ai.dashboard.DashboardScreen
import com.gameturbo.ai.dashboard.DashboardViewModel
import com.gameturbo.ai.data.AppContainer
import com.gameturbo.ai.games.GameBoosterScreen
import com.gameturbo.ai.games.GameBoosterViewModel
import com.gameturbo.ai.performance.PerformanceHubScreen
import com.gameturbo.ai.performance.PerformanceViewModel
import com.gameturbo.ai.profiles.ProfilesScreen
import com.gameturbo.ai.profiles.ProfilesViewModel
import com.gameturbo.ai.ui.theme.ElectricRoyalBlue
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.ObsidianDeep
import com.gameturbo.ai.ui.theme.RoyalMidnight
import com.gameturbo.ai.ui.theme.RoyalSurface
import kotlinx.coroutines.delay

object GameTurboRoutes {
    const val DASHBOARD = "dashboard"
    const val BOOSTER = "booster"
    const val MONITOR = "monitor"
    const val PROFILES = "profiles"
    const val ADVANCED = "advanced"
}

private data class NavDestinationItem(
    val route: String,
    val labelRes: Int,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
)

@Composable
fun MainEntry(
    showSplashInitially: Boolean = true
) {
    val context = LocalContext.current
    val container = remember(context) { AppContainer(context) }

    val dashboardVm: DashboardViewModel = viewModel(factory = DashboardViewModel.Factory(container))
    val boosterVm: GameBoosterViewModel = viewModel(factory = GameBoosterViewModel.Factory(container))
    val performanceVm: PerformanceViewModel = viewModel(factory = PerformanceViewModel.Factory(container))
    val profilesVm: ProfilesViewModel = viewModel(factory = ProfilesViewModel.Factory(container))
    val advancedVm: AdvancedViewModel = viewModel(factory = AdvancedViewModel.Factory(container))

    val dashboardState by dashboardVm.uiState.collectAsStateWithLifecycle()
    val boosterState by boosterVm.uiState.collectAsStateWithLifecycle()
    val performanceState by performanceVm.uiState.collectAsStateWithLifecycle()
    val profilesState by profilesVm.uiState.collectAsStateWithLifecycle()
    val advancedState by advancedVm.uiState.collectAsStateWithLifecycle()

    // Apply window-level keepScreenOn and optional brightness preference safely
    val activity = context as? Activity
    val keepScreenOn = dashboardState.preferences.keepScreenOn
    val brightnessPref = dashboardState.preferences.brightnessPercent

    DisposableEffect(activity, keepScreenOn, brightnessPref) {
        val window = activity?.window
        if (window != null) {
            if (keepScreenOn) {
                window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            } else {
                window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
            val attrs = window.attributes
            attrs.screenBrightness = if (brightnessPref in 10..100) {
                brightnessPref / 100f
            } else {
                WindowManager.LayoutParams.BRIGHTNESS_OVERRIDE_NONE
            }
            window.attributes = attrs
        }
        onDispose { }
    }

    var splashVisible by remember { mutableStateOf(showSplashInitially) }
    LaunchedEffect(showSplashInitially) {
        if (showSplashInitially) {
            delay(1250L)
            splashVisible = false
        }
    }

    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: GameTurboRoutes.DASHBOARD

    var selectedMonitorSubTab by remember { mutableIntStateOf(0) }
    var selectedAdvancedSubTab by remember { mutableIntStateOf(0) }

    val navItems = remember {
        listOf(
            NavDestinationItem(
                route = GameTurboRoutes.DASHBOARD,
                labelRes = R.string.nav_dashboard,
                selectedIcon = Icons.Filled.Dashboard,
                unselectedIcon = Icons.Outlined.Dashboard,
                testTag = "nav_dashboard"
            ),
            NavDestinationItem(
                route = GameTurboRoutes.BOOSTER,
                labelRes = R.string.nav_booster,
                selectedIcon = Icons.Filled.SportsEsports,
                unselectedIcon = Icons.Outlined.SportsEsports,
                testTag = "nav_booster"
            ),
            NavDestinationItem(
                route = GameTurboRoutes.MONITOR,
                labelRes = R.string.nav_monitor,
                selectedIcon = Icons.Filled.Speed,
                unselectedIcon = Icons.Outlined.Speed,
                testTag = "nav_monitor"
            ),
            NavDestinationItem(
                route = GameTurboRoutes.PROFILES,
                labelRes = R.string.nav_profiles,
                selectedIcon = Icons.Filled.Tune,
                unselectedIcon = Icons.Outlined.Tune,
                testTag = "nav_profiles"
            ),
            NavDestinationItem(
                route = GameTurboRoutes.ADVANCED,
                labelRes = R.string.nav_advanced,
                selectedIcon = Icons.Filled.AdminPanelSettings,
                unselectedIcon = Icons.Outlined.AdminPanelSettings,
                testTag = "nav_advanced"
            )
        )
    }

    fun navigateToRoute(route: String) {
        navController.navigate(route) {
            popUpTo(navController.graph.startDestinationId) {
                saveState = true
            }
            launchSingleTop = true
            restoreState = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(ObsidianDeep)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val useNavigationRail = maxWidth >= 600.dp

            Scaffold(
                containerColor = ObsidianDeep,
                contentWindowInsets = WindowInsets.safeDrawing,
                bottomBar = {
                    if (!useNavigationRail) {
                        NavigationBar(
                            containerColor = RoyalMidnight,
                            windowInsets = WindowInsets.navigationBars
                        ) {
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                val label = stringResource(item.labelRes)
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(item.route) },
                                    modifier = Modifier.testTag(item.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label, maxLines = 1) },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = ObsidianDeep,
                                        selectedTextColor = NeonCyan,
                                        indicatorColor = NeonCyan,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (useNavigationRail) {
                        NavigationRail(containerColor = RoyalMidnight) {
                            Spacer(modifier = Modifier.height(12.dp))
                            navItems.forEach { item ->
                                val selected = currentRoute == item.route
                                val label = stringResource(item.labelRes)
                                NavigationRailItem(
                                    selected = selected,
                                    onClick = { navigateToRoute(item.route) },
                                    modifier = Modifier.testTag(item.testTag),
                                    icon = {
                                        Icon(
                                            imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                                            contentDescription = label
                                        )
                                    },
                                    label = { Text(label) },
                                    colors = NavigationRailItemDefaults.colors(
                                        selectedIconColor = ObsidianDeep,
                                        selectedTextColor = NeonCyan,
                                        indicatorColor = NeonCyan,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                )
                            }
                        }
                    }

                    NavHost(
                        navController = navController,
                        startDestination = GameTurboRoutes.DASHBOARD,
                        modifier = Modifier.weight(1f)
                    ) {
                        composable(GameTurboRoutes.DASHBOARD) {
                            DashboardScreen(
                                state = dashboardState,
                                onRefresh = { dashboardVm.refreshNow() },
                                onToggleFocusMode = { dashboardVm.toggleFocusMode() },
                                onOpenMonitorTab = { tab ->
                                    selectedMonitorSubTab = tab
                                    navigateToRoute(GameTurboRoutes.MONITOR)
                                },
                                onOpenGames = { navigateToRoute(GameTurboRoutes.BOOSTER) },
                                onOpenProfiles = { navigateToRoute(GameTurboRoutes.PROFILES) },
                                onOpenTools = {
                                    selectedAdvancedSubTab = 0
                                    navigateToRoute(GameTurboRoutes.ADVANCED)
                                }
                            )
                        }

                        composable(GameTurboRoutes.BOOSTER) {
                            GameBoosterScreen(
                                state = boosterState,
                                onScanGames = { boosterVm.refreshInstalledAppsAndDetectGames() },
                                onRegisterApp = { app, profileId, focus, keepOn ->
                                    boosterVm.registerAppAsGame(app, profileId, focus, keepOn)
                                },
                                onUpdateGame = { boosterVm.updateGame(it) },
                                onRemoveGame = { boosterVm.removeGame(it) },
                                onLaunchGame = { ctx, model -> boosterVm.launchGame(ctx, model) },
                                onToggleFocusMode = { boosterVm.toggleFocusMode() },
                                onClearSessions = { boosterVm.clearSessionHistory() }
                            )
                        }

                        composable(GameTurboRoutes.MONITOR) {
                            PerformanceHubScreen(
                                state = performanceState,
                                selectedSubTab = selectedMonitorSubTab,
                                onSelectSubTab = { selectedMonitorSubTab = it },
                                onRefreshNow = { performanceVm.refreshNow() },
                                onClearTelemetryHistory = { performanceVm.clearTelemetryHistory() },
                                onRefreshStorage = { performanceVm.refreshStorageInspection() },
                                onToggleStorageFile = { performanceVm.toggleFileSelection(it) },
                                onSelectTempStorageFiles = { performanceVm.selectAllTempFiles() },
                                onClearStorageSelection = { performanceVm.clearSelection() },
                                onDeleteConfirmedStorageFiles = { performanceVm.deleteConfirmedSelectedFiles() }
                            )
                        }

                        composable(GameTurboRoutes.PROFILES) {
                            ProfilesScreen(
                                state = profilesState,
                                onActivateProfile = { profilesVm.activateProfile(it) },
                                onSaveProfile = { id, name, interval, focus, keepOn, haptic, brightness, desc ->
                                    profilesVm.saveCustomProfile(
                                        id, name, interval, focus, keepOn, haptic, brightness, desc
                                    )
                                },
                                onDeleteProfile = { profilesVm.deleteProfile(it) }
                            )
                        }

                        composable(GameTurboRoutes.ADVANCED) {
                            AdvancedToolsScreen(
                                state = advancedState,
                                initialSubTab = selectedAdvancedSubTab,
                                onSelectTier = { advancedVm.selectTier(it) },
                                onRefreshCapabilities = { advancedVm.refreshCapabilities() },
                                onLogShortcut = { advancedVm.logSystemShortcutAccess(it) },
                                onClearAuditLogs = { advancedVm.clearAuditLogs() },
                                onOpenPerformanceHistory = {
                                    selectedMonitorSubTab = 0
                                    navigateToRoute(GameTurboRoutes.MONITOR)
                                }
                            )
                        }
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = splashVisible,
            enter = fadeIn(tween(150)),
            exit = fadeOut(tween(400))
        ) {
            AnimatedSplashOverlay()
        }
    }
}

@Composable
private fun AnimatedSplashOverlay() {
    val scale = remember { Animatable(0.76f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        alpha.animateTo(1f, animationSpec = tween(420, easing = FastOutSlowInEasing))
        scale.animateTo(1.0f, animationSpec = tween(550, easing = FastOutSlowInEasing))
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.radialGradient(
                    colors = listOf(RoyalSurface, ObsidianDeep)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape)
                    .border(2.dp, NeonCyan.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_launcher_art),
                    contentDescription = stringResource(R.string.cd_app_logo),
                    modifier = Modifier.fillMaxSize()
                )
            }
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "GAME TURBO AI",
                style = MaterialTheme.typography.displaySmall,
                color = NeonCyan
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "CENTRO DE CONTROL GAMING NATIVO",
                style = MaterialTheme.typography.labelLarge,
                color = ElectricRoyalBlue
            )
        }
    }
}
