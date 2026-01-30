package com.krishana.onedot.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repository for managing app settings using DataStore.
 */
class SettingsRepository(private val context: Context) {

    companion object {
        private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")
        
        val PAST_COLOR_KEY = intPreferencesKey("past_color")
        val TODAY_COLOR_KEY = intPreferencesKey("today_color")
        val FUTURE_COLOR_KEY = intPreferencesKey("future_color")
        val BACKGROUND_COLOR_KEY = intPreferencesKey("background_color")
        val LAST_UPDATE_KEY = longPreferencesKey("last_update_timestamp")

        // Default colors
        const val DEFAULT_PAST_COLOR = 0xFF1976D2.toInt()      // Blue[700]
        const val DEFAULT_TODAY_COLOR = 0xFFFF9800.toInt()     // Orange
        const val DEFAULT_FUTURE_COLOR = 0xFF424242.toInt()    // Grey[800]
        const val DEFAULT_BACKGROUND_COLOR = 0xFF000000.toInt() // Black
    }

    val pastColorFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[PAST_COLOR_KEY] ?: DEFAULT_PAST_COLOR
    }

    val todayColorFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[TODAY_COLOR_KEY] ?: DEFAULT_TODAY_COLOR
    }

    val futureColorFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[FUTURE_COLOR_KEY] ?: DEFAULT_FUTURE_COLOR
    }

    val backgroundColorFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[BACKGROUND_COLOR_KEY] ?: DEFAULT_BACKGROUND_COLOR
    }

    val lastUpdateFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_UPDATE_KEY] ?: 0L
    }

    suspend fun updatePastColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[PAST_COLOR_KEY] = color
        }
    }

    suspend fun updateTodayColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[TODAY_COLOR_KEY] = color
        }
    }

    suspend fun updateFutureColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[FUTURE_COLOR_KEY] = color
        }
    }

    suspend fun updateBackgroundColor(color: Int) {
        context.dataStore.edit { preferences ->
            preferences[BACKGROUND_COLOR_KEY] = color
        }
    }

    suspend fun updateLastUpdateTimestamp(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[LAST_UPDATE_KEY] = timestamp
        }
    }

    suspend fun getAllColors(): Map<String, Int> {
        val preferences = context.dataStore.data
        return mapOf(
            "past" to (preferences.map { it[PAST_COLOR_KEY] ?: DEFAULT_PAST_COLOR }.first()),
            "today" to (preferences.map { it[TODAY_COLOR_KEY] ?: DEFAULT_TODAY_COLOR }.first()),
            "future" to (preferences.map { it[FUTURE_COLOR_KEY] ?: DEFAULT_FUTURE_COLOR }.first()),
            "background" to (preferences.map { it[BACKGROUND_COLOR_KEY] ?: DEFAULT_BACKGROUND_COLOR }.first())
        )
    }
}
