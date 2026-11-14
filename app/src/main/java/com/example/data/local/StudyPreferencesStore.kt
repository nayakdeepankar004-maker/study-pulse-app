package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.model.AlarmTone
import com.example.model.DateTimeHelper
import com.example.model.SUBJECT_ALL
import com.example.model.StudyPreferences
import com.example.model.StudyStream
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.studyDataStore: DataStore<Preferences> by preferencesDataStore(name = "study_pulse_prefs")

class StudyPreferencesStore(private val context: Context) {

    private object Keys {
        val SELECTED_STREAM = stringPreferencesKey("selected_stream")
        val SELECTED_SUBJECT_FILTER = stringPreferencesKey("selected_subject_filter")
        val DAILY_TARGET_LECTURES = intPreferencesKey("daily_target_lectures")
        val DAILY_TARGET_HOURS = intPreferencesKey("daily_target_hours")
        val LOGGED_HOURS_TODAY = floatPreferencesKey("logged_hours_today")
        val STREAK = intPreferencesKey("streak")
        val ALARM_TONE = stringPreferencesKey("alarm_tone")
        val LAST_LOGGED_DATE = stringPreferencesKey("last_logged_date")
        val IS_SEEDED = booleanPreferencesKey("is_seeded")
    }

    val preferencesFlow: Flow<StudyPreferences> = context.studyDataStore.data.map { prefs ->
        StudyPreferences(
            selectedStream = StudyStream.fromCode(prefs[Keys.SELECTED_STREAM] ?: StudyStream.NEET.code),
            selectedSubjectFilter = prefs[Keys.SELECTED_SUBJECT_FILTER] ?: SUBJECT_ALL,
            dailyTargetLectures = prefs[Keys.DAILY_TARGET_LECTURES] ?: 4,
            dailyTargetHours = prefs[Keys.DAILY_TARGET_HOURS] ?: 6,
            loggedHoursToday = prefs[Keys.LOGGED_HOURS_TODAY] ?: 2.5f,
            streak = prefs[Keys.STREAK] ?: 7,
            alarmTone = AlarmTone.fromCode(prefs[Keys.ALARM_TONE] ?: AlarmTone.NEON.code),
            lastLoggedDate = prefs[Keys.LAST_LOGGED_DATE] ?: DateTimeHelper.todayString(),
            isSeeded = prefs[Keys.IS_SEEDED] ?: false
        )
    }

    suspend fun setSelectedStream(stream: StudyStream) {
        context.studyDataStore.edit { prefs ->
            prefs[Keys.SELECTED_STREAM] = stream.code
            prefs[Keys.SELECTED_SUBJECT_FILTER] = SUBJECT_ALL
        }
    }

    suspend fun setSelectedSubjectFilter(subject: String) {
        context.studyDataStore.edit { prefs ->
            prefs[Keys.SELECTED_SUBJECT_FILTER] = subject
        }
    }

    suspend fun setDailyTargetLectures(count: Int) {
        context.studyDataStore.edit { prefs ->
            prefs[Keys.DAILY_TARGET_LECTURES] = count.coerceIn(1, 20)
        }
    }

    suspend fun setDailyTargetHours(hours: Int) {
        context.studyDataStore.edit { prefs ->
            prefs[Keys.DAILY_TARGET_HOURS] = hours.coerceIn(1, 18)
        }
    }

    suspend fun adjustLoggedHours(delta: Float) {
        context.studyDataStore.edit { prefs ->
            val current = prefs[Keys.LOGGED_HOURS_TODAY] ?: 2.5f
            val updated = ((current + delta).coerceAtLeast(0f) * 10f).toInt() / 10f
            prefs[Keys.LOGGED_HOURS_TODAY] = updated
            prefs[Keys.LAST_LOGGED_DATE] = DateTimeHelper.todayString()
        }
    }

    suspend fun setAlarmTone(tone: AlarmTone) {
        context.studyDataStore.edit { prefs ->
            prefs[Keys.ALARM_TONE] = tone.code
        }
    }

    suspend fun markSeeded() {
        context.studyDataStore.edit { prefs ->
            prefs[Keys.IS_SEEDED] = true
            if (prefs[Keys.LAST_LOGGED_DATE] == null) {
                prefs[Keys.LAST_LOGGED_DATE] = DateTimeHelper.todayString()
            }
        }
    }

    suspend fun rolloverDayIfNeeded(today: String) {
        context.studyDataStore.edit { prefs ->
            val last = prefs[Keys.LAST_LOGGED_DATE]
            if (last != null && last != today) {
                val yesterdayHours = prefs[Keys.LOGGED_HOURS_TODAY] ?: 0f
                val currentStreak = prefs[Keys.STREAK] ?: 7
                if (yesterdayHours > 0f) {
                    prefs[Keys.STREAK] = currentStreak + 1
                }
                prefs[Keys.LOGGED_HOURS_TODAY] = 0f
                prefs[Keys.LAST_LOGGED_DATE] = today
            }
        }
    }
}
