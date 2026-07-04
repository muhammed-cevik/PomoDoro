package com.tdev.pomodoro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tdev.pomodoro.timer.PomodoroViewModel
import com.tdev.pomodoro.ui.HistoryScreen
import com.tdev.pomodoro.ui.TimerScreen
import com.tdev.pomodoro.ui.theme.Bg
import com.tdev.pomodoro.ui.theme.PomoDoroTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PomoDoroTheme {
                Surface(
                    modifier = Modifier.fillMaxSize().background(Bg)
                ) {
                    PomoDoroApp()
                }
            }
        }
    }
}

private enum class Screen { TIMER, HISTORY }

@Composable
private fun PomoDoroApp() {
    val viewModel: PomodoroViewModel = viewModel()
    val state by viewModel.uiState.collectAsState()
    var screen by remember { mutableStateOf(Screen.TIMER) }

    when (screen) {
        Screen.TIMER -> TimerScreen(
            state = state,
            onStart = viewModel::start,
            onPause = viewModel::pause,
            onReset = viewModel::reset,
            onSkip = viewModel::skipPhase,
            onWorkMinutesChange = viewModel::setWorkMinutes,
            onBreakMinutesChange = viewModel::setBreakMinutes,
            onOpenHistory = { screen = Screen.HISTORY }
        )
        Screen.HISTORY -> HistoryScreen(
            history = state.history,
            onBack = { screen = Screen.TIMER },
            onClear = viewModel::clearHistory
        )
    }
}
