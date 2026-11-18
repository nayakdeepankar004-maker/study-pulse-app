package com.example.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

enum class ThemeMode(val label: String) {
    SYSTEM("System Default"),
    LIGHT("Light"),
    DARK("Dark")
}

private val Context.jeetDataStore: DataStore<Preferences> by preferencesDataStore(name = "jeetxtoppers_prefs")

class ThemePreferencesStore(private val context: Context) {
    private val themeModeKey = stringPreferencesKey("theme_mode")

    val themeModeFlow: Flow<ThemeMode> = context.jeetDataStore.data.map { prefs ->
        val raw = prefs[themeModeKey] ?: ThemeMode.SYSTEM.name
        enumValues<ThemeMode>().firstOrNull { it.name == raw } ?: ThemeMode.SYSTEM
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.jeetDataStore.edit { prefs ->
            prefs[themeModeKey] = mode.name
        }
    }
}
