package com.tdev.pomodoro.timer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tdev.pomodoro.data.PomodoroSession
import com.tdev.pomodoro.data.PomodoroStore
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class Phase { WORK, BREAK }
enum class RunState { IDLE, RUNNING, PAUSED, FINISHED }

data class PomodoroUiState(
    val phase: Phase = Phase.WORK,
    val runState: RunState = RunState.IDLE,
    val workMinutes: Int = PomodoroStore.DEFAULT_WORK_MIN,
    val breakMinutes: Int = PomodoroStore.DEFAULT_BREAK_MIN,
    val remainingSeconds: Int = PomodoroStore.DEFAULT_WORK_MIN * 60,
    val completedPomodoros: Int = 0,
    val history: List<PomodoroSession> = emptyList()
)

class PomodoroViewModel(application: Application) : AndroidViewModel(application) {

    private val store = PomodoroStore(application)

    private val _uiState = MutableStateFlow(PomodoroUiState())
    val uiState: StateFlow<PomodoroUiState> = _uiState

    private var tickJob: Job? = null

    init {
        viewModelScope.launch {
            val work = store.lastWorkMinutes.first()
            val brk = store.lastBreakMinutes.first()
            _uiState.value = _uiState.value.copy(
                workMinutes = work,
                breakMinutes = brk,
                remainingSeconds = work * 60
            )
        }
        viewModelScope.launch {
            store.history.collect { list ->
                _uiState.value = _uiState.value.copy(history = list)
            }
        }
    }

    /** Kullanıcı süreyi her seferinde değiştirebilir; sadece IDLE durumdayken etkili olur. */
    fun setWorkMinutes(minutes: Int) {
        if (_uiState.value.runState != RunState.RUNNING) {
            _uiState.value = _uiState.value.copy(
                workMinutes = minutes,
                remainingSeconds = if (_uiState.value.phase == Phase.WORK) minutes * 60 else _uiState.value.remainingSeconds
            )
        }
    }

    fun setBreakMinutes(minutes: Int) {
        if (_uiState.value.runState != RunState.RUNNING) {
            _uiState.value = _uiState.value.copy(
                breakMinutes = minutes,
                remainingSeconds = if (_uiState.value.phase == Phase.BREAK) minutes * 60 else _uiState.value.remainingSeconds
            )
        }
    }

    fun start() {
        val state = _uiState.value
        if (state.runState == RunState.RUNNING) return

        if (state.runState == RunState.IDLE || state.runState == RunState.FINISHED) {
            viewModelScope.launch {
                store.saveLastDurations(state.workMinutes, state.breakMinutes)
            }
        }

        _uiState.value = state.copy(runState = RunState.RUNNING)
        tickJob?.cancel()
        tickJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0 && _uiState.value.runState == RunState.RUNNING) {
                delay(1000)
                if (_uiState.value.runState != RunState.RUNNING) break
                val newRemaining = _uiState.value.remainingSeconds - 1
                _uiState.value = _uiState.value.copy(remainingSeconds = newRemaining)
                if (newRemaining <= 0) {
                    onPhaseComplete()
                }
            }
        }
    }

    fun pause() {
        if (_uiState.value.runState == RunState.RUNNING) {
            _uiState.value = _uiState.value.copy(runState = RunState.PAUSED)
            tickJob?.cancel()
        }
    }

    fun reset() {
        tickJob?.cancel()
        val state = _uiState.value
        val seconds = if (state.phase == Phase.WORK) state.workMinutes * 60 else state.breakMinutes * 60
        _uiState.value = state.copy(runState = RunState.IDLE, remainingSeconds = seconds)
    }

    /** Mevcut fazı bitmiş sayıp bir sonrakine geç (elle atlamak için). */
    fun skipPhase() {
        tickJob?.cancel()
        onPhaseComplete()
    }

    private fun onPhaseComplete() {
        val state = _uiState.value
        val finishedLabel = if (state.phase == Phase.WORK) "Çalışma" else "Mola"
        val finishedDuration = if (state.phase == Phase.WORK) state.workMinutes else state.breakMinutes

        viewModelScope.launch {
            store.addSession(
                PomodoroSession(
                    label = finishedLabel,
                    durationMinutes = finishedDuration,
                    completedAtEpochMillis = System.currentTimeMillis()
                )
            )
        }

        val nextPhase = if (state.phase == Phase.WORK) Phase.BREAK else Phase.WORK
        val nextSeconds = if (nextPhase == Phase.WORK) state.workMinutes * 60 else state.breakMinutes * 60
        val newCompleted = if (state.phase == Phase.WORK) state.completedPomodoros + 1 else state.completedPomodoros

        _uiState.value = state.copy(
            phase = nextPhase,
            runState = RunState.IDLE,
            remainingSeconds = nextSeconds,
            completedPomodoros = newCompleted
        )
    }

    fun clearHistory() {
        viewModelScope.launch { store.clearHistory() }
    }

    override fun onCleared() {
        super.onCleared()
        tickJob?.cancel()
    }
}
