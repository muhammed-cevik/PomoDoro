package com.tdev.pomodoro.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

val Context.dataStore by preferencesDataStore(name = "pomodoro_store")

/**
 * Tamamlanmış bir pomodoro / mola oturumu.
 */
data class PomodoroSession(
    val label: String,       // "Çalışma" veya "Mola"
    val durationMinutes: Int,
    val completedAtEpochMillis: Long
)

class PomodoroStore(private val context: Context) {

    private val HISTORY_KEY = stringPreferencesKey("history_json")
    private val WORK_MIN_KEY = intPreferencesKey("last_work_minutes")
    private val BREAK_MIN_KEY = intPreferencesKey("last_break_minutes")

    companion object {
        const val DEFAULT_WORK_MIN = 25
        const val DEFAULT_BREAK_MIN = 5
    }

    val lastWorkMinutes: Flow<Int> = context.dataStore.data.map {
        it[WORK_MIN_KEY] ?: DEFAULT_WORK_MIN
    }

    val lastBreakMinutes: Flow<Int> = context.dataStore.data.map {
        it[BREAK_MIN_KEY] ?: DEFAULT_BREAK_MIN
    }

    val history: Flow<List<PomodoroSession>> = context.dataStore.data.map { prefs ->
        val json = prefs[HISTORY_KEY] ?: "[]"
        parseHistory(json)
    }

    suspend fun saveLastDurations(workMin: Int, breakMin: Int) {
        context.dataStore.edit {
            it[WORK_MIN_KEY] = workMin
            it[BREAK_MIN_KEY] = breakMin
        }
    }

    suspend fun addSession(session: PomodoroSession) {
        context.dataStore.edit { prefs ->
            val current = parseHistory(prefs[HISTORY_KEY] ?: "[]").toMutableList()
            current.add(0, session) // en yeni en üstte
            val trimmed = current.take(200) // makul bir sınır
            prefs[HISTORY_KEY] = toJson(trimmed)
        }
    }

    suspend fun clearHistory() {
        context.dataStore.edit {
            it[HISTORY_KEY] = "[]"
        }
    }

    private fun parseHistory(json: String): List<PomodoroSession> {
        return try {
            val arr = JSONArray(json)
            (0 until arr.length()).map { i ->
                val o = arr.getJSONObject(i)
                PomodoroSession(
                    label = o.getString("label"),
                    durationMinutes = o.getInt("durationMinutes"),
                    completedAtEpochMillis = o.getLong("completedAt")
                )
            }
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun toJson(list: List<PomodoroSession>): String {
        val arr = JSONArray()
        list.forEach { s ->
            val o = JSONObject()
            o.put("label", s.label)
            o.put("durationMinutes", s.durationMinutes)
            o.put("completedAt", s.completedAtEpochMillis)
            arr.put(o)
        }
        return arr.toString()
    }
}
