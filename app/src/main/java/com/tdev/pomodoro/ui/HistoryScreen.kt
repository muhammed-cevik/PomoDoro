package com.tdev.pomodoro.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tdev.pomodoro.data.PomodoroSession
import com.tdev.pomodoro.ui.theme.CardBg
import com.tdev.pomodoro.ui.theme.TextDim
import com.tdev.pomodoro.ui.theme.TextPrimary
import com.tdev.pomodoro.ui.theme.TextSecondary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HistoryScreen(
    history: List<PomodoroSession>,
    onBack: () -> Unit,
    onClear: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Geri",
                color = TextSecondary,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onBack() }
            )
            Text(
                text = "Geçmiş",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "Temizle",
                color = TextDim,
                fontSize = 14.sp,
                modifier = Modifier.clickable { onClear() }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        if (history.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(text = "Henüz kayıt yok", color = TextDim, fontSize = 14.sp)
            }
        } else {
            val formatter = remember_(SimpleDateFormat("dd MMM, HH:mm", Locale("tr")))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(history) { session ->
                    HistoryRow(session = session, dateText = formatter.format(Date(session.completedAtEpochMillis)))
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(session: PomodoroSession, dateText: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(color = CardBg, shape = RoundedCornerShape(10.dp))
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = session.label, color = TextPrimary, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = dateText, color = TextDim, fontSize = 12.sp)
        }
        Text(text = "${session.durationMinutes} dk", color = TextSecondary, fontSize = 14.sp)
    }
}

// Basit yardımcı: Compose'un remember fonksiyonunu import çakışmasına
// girmeden kullanmak için ince bir sarmalayıcı.
@Composable
private fun <T> remember_(value: T): T {
    return androidx.compose.runtime.remember { value }
}
