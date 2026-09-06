package com.localbill.recording.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
import com.localbill.recording.ui.theme.TextSecondary
import com.localbill.recording.ui.theme.WarmBorder
import com.localbill.recording.ui.theme.WarmBone
import com.localbill.recording.ui.viewmodel.BackupViewModel
import com.localbill.recording.ui.viewmodel.CategoryViewModel
import com.localbill.recording.ui.viewmodel.HomeViewModel
import com.localbill.recording.ui.viewmodel.StatisticsViewModel

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Home : Screen("home", "账单", Icons.AutoMirrored.Filled.MenuBook)
    object Statistics : Screen("statistics", "统计", Icons.Default.AutoGraph)
    object Categories : Screen("categories", "分类", Icons.Default.CollectionsBookmark)
    object Backup : Screen("backup", "设置", Icons.Default.Settings)
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
        bottomBar = {
            val haptic = LocalHapticFeedback.current
            Column {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline, thickness = 0.8.dp)
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.background,
                    tonalElevation = 0.dp,
                    modifier = Modifier
                ) {
                    bottomNavScreens.forEach { screen ->
                        val isSelected = currentRoute == screen.route
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    imageVector = screen.icon,
                                    contentDescription = screen.title,
                                    modifier = Modifier.size(22.dp)
                                )
                            },
                            label = {
                                Text(
                                    text = screen.title,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            },
                            selected = isSelected,
                            onClick = {
                                if (currentRoute != screen.route) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    navController.navigate(screen.route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = DeepGreen,
                                selectedTextColor = DeepGreen,
                                indicatorColor = DeepGreen.copy(alpha = 0.12f),
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                disabledIconColor = Color.Transparent,
                                disabledTextColor = Color.Transparent
                            )
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            enterTransition = { fadeIn(animationSpec = tween(220)) },
            exitTransition = { fadeOut(animationSpec = tween(160)) }
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

