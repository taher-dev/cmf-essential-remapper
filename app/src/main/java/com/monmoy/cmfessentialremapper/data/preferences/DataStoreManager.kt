package com.monmoy.cmfessentialremapper.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "cmf_essential_settings")

/**
 * Manages persisted user preferences and learned hardware key identifiers via Jetpack DataStore.
 */
class DataStoreManager(private val context: Context) {

    object PreferenceKeys {
        val KEY_HAPTIC_FEEDBACK_ENABLED = booleanPreferencesKey("haptic_feedback_enabled")
        val KEY_LEARNED_KEY_CODE = intPreferencesKey("learned_key_code")
        val KEY_LEARNED_SCAN_CODE = intPreferencesKey("learned_scan_code")
        val KEY_IS_KEY_CONFIGURED = booleanPreferencesKey("is_key_configured")
        val KEY_DOUBLE_PRESS_WINDOW_MS = longPreferencesKey("double_press_window_ms")
        val KEY_TRIPLE_PRESS_WINDOW_MS = longPreferencesKey("triple_press_window_ms")
        val KEY_LONG_PRESS_THRESHOLD_MS = longPreferencesKey("long_press_threshold_ms")
    }

    private val preferencesFlow: Flow<Preferences> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    val isHapticFeedbackEnabled: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[PreferenceKeys.KEY_HAPTIC_FEEDBACK_ENABLED] ?: true
    }

    val learnedKeyCode: Flow<Int> = preferencesFlow.map { preferences ->
        preferences[PreferenceKeys.KEY_LEARNED_KEY_CODE] ?: 0
    }

    val learnedScanCode: Flow<Int> = preferencesFlow.map { preferences ->
        preferences[PreferenceKeys.KEY_LEARNED_SCAN_CODE] ?: 250
    }

    val isKeyConfigured: Flow<Boolean> = preferencesFlow.map { preferences ->
        preferences[PreferenceKeys.KEY_IS_KEY_CONFIGURED] ?: false
    }

    val doublePressWindowMs: Flow<Long> = preferencesFlow.map { preferences ->
        preferences[PreferenceKeys.KEY_DOUBLE_PRESS_WINDOW_MS] ?: 300L
    }

    val triplePressWindowMs: Flow<Long> = preferencesFlow.map { preferences ->
        preferences[PreferenceKeys.KEY_TRIPLE_PRESS_WINDOW_MS] ?: 300L
    }

    val longPressThresholdMs: Flow<Long> = preferencesFlow.map { preferences ->
        preferences[PreferenceKeys.KEY_LONG_PRESS_THRESHOLD_MS] ?: 600L
    }

    suspend fun saveLearnedKey(keyCode: Int, scanCode: Int) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.KEY_LEARNED_KEY_CODE] = keyCode
            preferences[PreferenceKeys.KEY_LEARNED_SCAN_CODE] = scanCode
            preferences[PreferenceKeys.KEY_IS_KEY_CONFIGURED] = true
        }
    }

    suspend fun resetLearnedKey() {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.KEY_LEARNED_KEY_CODE] = 0
            preferences[PreferenceKeys.KEY_LEARNED_SCAN_CODE] = 250
            preferences[PreferenceKeys.KEY_IS_KEY_CONFIGURED] = false
        }
    }

    suspend fun setHapticFeedbackEnabled(enabled: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferenceKeys.KEY_HAPTIC_FEEDBACK_ENABLED] = enabled
        }
    }
}
