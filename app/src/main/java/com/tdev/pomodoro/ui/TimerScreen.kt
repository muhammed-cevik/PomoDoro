package com.tdev.pomodoro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tdev.pomodoro.timer.Phase
import com.tdev.pomodoro.timer.PomodoroUiState
import com.tdev.pomodoro.timer.RunState
import com.tdev.pomodoro.ui.theme.TextDim
import com.tdev.pomodoro.ui.theme.TextPrimary
import com.tdev.pomodoro.ui.theme.TextSecondary

@Composable
fun TimerScreen(
    state: PomodoroUiState,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onReset: () -> Unit,
    onSkip: () -> Unit,
    onWorkMinutesChange: (Int) -> Unit,
    onBreakMinutesChange: (Int) -> Unit,
    onOpenHistory: () -> Unit
) {
    val minutes = state.remainingSeconds / 60
    val seconds = state.remainingSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)
    val phaseLabel = if (state.phase == Phase.WORK) "ÇALIŞMA" else "MOLA"
    val editable = state.runState != RunState.RUNNING

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "PomoDoro",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Geçmiş",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onOpenHistory() }
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = phaseLabel,
            color = TextDim,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(12.dp))

        Box(
            modifier = Modifier
                .size(260.dp)
                .border(width = 1.dp, color = com.tdev.pomodoro.ui.theme.Divider, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = timeText,
                color = TextPrimary,
                fontSize = 56.sp,
                fontWeight = FontWeight.Light
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "Tamamlanan pomodoro: ${state.completedPomodoros}",
            color = TextDim,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Süre ayarlayıcılar
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DurationStepper(
                label = "Çalışma",
                minutes = state.workMinutes,
                enabled = editable,
                onChange = onWorkMinutesChange
            )
            DurationStepper(
                label = "Mola",
                minutes = state.breakMinutes,
                enabled = editable,
                onChange = onBreakMinutesChange
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButtonPD(
                text = "Sıfırla",
                modifier = Modifier.weight(1f),
                onClick = onReset
            )

            PrimaryButtonPD(
                text = when (state.runState) {
                    RunState.RUNNING -> "Duraklat"
                    else -> "Başlat"
                },
                modifier = Modifier.weight(1f),
                onClick = {
                    if (state.runState == RunState.RUNNING) onPause() else onStart()
                }
            )

            OutlinedButtonPD(
                text = "Geç",
                modifier = Modifier.weight(1f),
                onClick = onSkip
            )
        }
    }
}

@Composable
private fun DurationStepper(
    label: String,
    minutes: Int,
    enabled: Boolean,
    onChange: (Int) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, color = TextDim, fontSize = 12.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            StepperButton(symbol = "–", enabled = enabled && minutes > 1) {
                onChange((minutes - 1).coerceAtLeast(1))
            }
            Text(
                text = "$minutes dk",
                color = TextPrimary,
                fontSize = 15.sp,
                modifier = Modifier.padding(horizontal = 10.dp)
            )
            StepperButton(symbol = "+", enabled = enabled && minutes < 180) {
                onChange((minutes + 1).coerceAtMost(180))
            }
        }
    }
}

@Composable
private fun StepperButton(symbol: String, enabled: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(32.dp)
            .border(width = 1.dp, color = com.tdev.pomodoro.ui.theme.Divider, shape = CircleShape)
            .then(if (enabled) Modifier.clickable { onClick() } else Modifier),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = symbol,
            color = if (enabled) TextPrimary else TextDim,
            fontSize = 16.sp
        )
    }
}

@Composable
private fun PrimaryButtonPD(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(50.dp)
            .background(color = Color.White, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = Color.Black, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun OutlinedButtonPD(text: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Box(
        modifier = modifier
            .height(50.dp)
            .border(width = 1.dp, color = com.tdev.pomodoro.ui.theme.Divider, shape = RoundedCornerShape(8.dp))
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(text = text, color = TextSecondary, fontSize = 14.sp)
    }
}
