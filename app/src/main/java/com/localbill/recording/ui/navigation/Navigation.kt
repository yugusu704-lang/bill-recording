package com.localbill.recording.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.localbill.recording.ui.screens.BackupScreen
import com.localbill.recording.ui.screens.CategoryManagementScreen
import com.localbill.recording.ui.screens.HomeScreen
import com.localbill.recording.ui.screens.StatisticsScreen
import com.localbill.recording.ui.theme.DeepGreen
import com.localbill.recording.ui.theme.DeepGreenLight
import com.localbill.recording.ui.theme.DeepGreenSoft
import com.localbill.recording.ui.theme.WarmBone
import com.localbill.recording.ui.viewmodel.BackupViewModel
import com.localbill.recording.ui.viewmodel.CategoryViewModel
import com.localbill.recording.ui.viewmodel.HomeViewModel
import com.localbill.recording.ui.viewmodel.StatisticsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    data object Home : Screen("home", "账单", Icons.AutoMirrored.Filled.MenuBook)
    data object Statistics : Screen("statistics", "统计", Icons.Default.AutoGraph)
    data object Categories : Screen("categories", "分类", Icons.Default.CollectionsBookmark)
    data object Backup : Screen("backup", "备份", Icons.Default.Settings)
}

val bottomNavScreens = listOf(
    Screen.Home,
    Screen.Statistics,
    Screen.Categories,
    Screen.Backup
)

@Composable
fun MainAppNavigation(
    homeViewModel: HomeViewModel,
    statisticsViewModel: StatisticsViewModel,
    categoryViewModel: CategoryViewModel,
    backupViewModel: BackupViewModel,
    initialAddRecord: Boolean = false,
    navController: NavHostController = rememberNavController()
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    Scaffold(
        containerColor = WarmBone,
        bottomBar = {
            FloatingNavigationCapsule(
                currentRoute = currentRoute,
                onNavigate = { route ->
                    navController.navigate(route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .background(WarmBone)
                .padding(innerPadding)
                .consumeWindowInsets(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(180)) },
            exitTransition = { fadeOut(animationSpec = tween(180)) }
        ) {
            composable(Screen.Home.route) {
                HomeScreen(viewModel = homeViewModel, initialAddRecord = initialAddRecord)
            }
            composable(Screen.Statistics.route) {
                StatisticsScreen(viewModel = statisticsViewModel)
            }
            composable(Screen.Categories.route) {
                CategoryManagementScreen(viewModel = categoryViewModel)
            }
            composable(Screen.Backup.route) {
                BackupScreen(viewModel = backupViewModel)
            }
        }
    }
}

@Composable
private fun FloatingNavigationCapsule(
    currentRoute: String?,
    onNavigate: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current

    Box(
        modifier = modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(bottom = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .padding(horizontal = 20.dp)
                .widthIn(max = 440.dp)
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(32.dp),
            color = DeepGreenLight,
            border = BorderStroke(1.dp, DeepGreenSoft),
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 6.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                bottomNavScreens.forEach { screen ->
                    val isSelected = currentRoute == screen.route
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(24.dp))
                            .background(if (isSelected) Color.White else Color.Transparent)
                            .then(
                                if (isSelected) Modifier.border(
                                    1.dp,
                                    Color(0xFFE2EBE2),
                                    RoundedCornerShape(24.dp)
                                ) else Modifier
                            )
                            .clickable {
                                if (!isSelected) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onNavigate(screen.route)
                                }
                            }
                            .padding(vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = screen.title,
                                tint = if (isSelected) DeepGreen else Color(0xFF6E8272),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                ),
                                color = if (isSelected) DeepGreen else Color(0xFF6E8272)
                            )
                        }
                    }
                }
            }
        }
    }
}
