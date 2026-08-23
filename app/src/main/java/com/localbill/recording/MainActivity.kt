package com.localbill.recording

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.localbill.recording.ui.navigation.MainAppNavigation
import com.localbill.recording.ui.theme.BillRecordingTheme
import com.localbill.recording.ui.viewmodel.BackupViewModel
import com.localbill.recording.ui.viewmodel.CategoryViewModel
import com.localbill.recording.ui.viewmodel.HomeViewModel
import com.localbill.recording.ui.viewmodel.StatisticsViewModel

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as BillApplication

        val homeViewModel by viewModels<HomeViewModel> {
            HomeViewModel.Factory(app.recordRepository, app.categoryRepository)
        }

        val statisticsViewModel by viewModels<StatisticsViewModel> {
            StatisticsViewModel.Factory(app.recordRepository)
        }

        val categoryViewModel by viewModels<CategoryViewModel> {
            CategoryViewModel.Factory(app.categoryRepository)
        }

        val backupViewModel by viewModels<BackupViewModel> {
            BackupViewModel.Factory(app.backupRepository)
        }

        setContent {
            BillRecordingTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    MainAppNavigation(
                        homeViewModel = homeViewModel,
                        statisticsViewModel = statisticsViewModel,
                        categoryViewModel = categoryViewModel,
                        backupViewModel = backupViewModel
                    )
                }
            }
        }
    }
}
