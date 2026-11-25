package com.gameturbo.ai.data.prefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "game_turbo_prefs")

data class AppPreferencesState(
    val activeProfileId: Long = 1L,
    val samplingIntervalMs: Long = 3000L,
    val focusModeEnabled: Boolean = false,
    val keepScreenOn: Boolean = false,
    val hapticFeedback: Boolean = true,
    val brightnessPercent: Int = -1,
    val mediaVolumePercent: Int = -1,
    val preferredRefreshHz: Int = 0,
    val sustainedPerformanceMode: Boolean = false,
    val selectedAdvancedTier: String = "NORMAL"
)

class PreferencesRepository(private val context: Context) {

    private object Keys {
        val ACTIVE_PROFILE_ID = longPreferencesKey("active_profile_id")
        val SAMPLING_INTERVAL_MS = longPreferencesKey("sampling_interval_ms")
        val FOCUS_MODE_ENABLED = booleanPreferencesKey("focus_mode_enabled")
        val KEEP_SCREEN_ON = booleanPreferencesKey("keep_screen_on")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val BRIGHTNESS_PERCENT = intPreferencesKey("brightness_percent")
        val MEDIA_VOLUME_PERCENT = intPreferencesKey("media_volume_percent")
        val PREFERRED_REFRESH_HZ = intPreferencesKey("preferred_refresh_hz")
        val SUSTAINED_PERF_MODE = booleanPreferencesKey("sustained_perf_mode")
        val SELECTED_ADVANCED_TIER = stringPreferencesKey("selected_advanced_tier")
    }

    val preferencesFlow: Flow<AppPreferencesState> = context.dataStore.data.map { prefs ->
        AppPreferencesState(
            activeProfileId = prefs[Keys.ACTIVE_PROFILE_ID] ?: 1L,
            samplingIntervalMs = prefs[Keys.SAMPLING_INTERVAL_MS] ?: 3000L,
            focusModeEnabled = prefs[Keys.FOCUS_MODE_ENABLED] ?: false,
            keepScreenOn = prefs[Keys.KEEP_SCREEN_ON] ?: false,
            hapticFeedback = prefs[Keys.HAPTIC_FEEDBACK] ?: true,
            brightnessPercent = prefs[Keys.BRIGHTNESS_PERCENT] ?: -1,
            mediaVolumePercent = prefs[Keys.MEDIA_VOLUME_PERCENT] ?: -1,
            preferredRefreshHz = prefs[Keys.PREFERRED_REFRESH_HZ] ?: 0,
            sustainedPerformanceMode = prefs[Keys.SUSTAINED_PERF_MODE] ?: false,
            selectedAdvancedTier = prefs[Keys.SELECTED_ADVANCED_TIER] ?: "NORMAL"
        )
    }

    suspend fun applyProfileSettings(
        profileId: Long,
        samplingIntervalMs: Long,
        focusMode: Boolean,
        keepScreenOn: Boolean,
        hapticFeedback: Boolean,
        brightnessPercent: Int,
        mediaVolumePercent: Int,
        preferredRefreshHz: Int,
        sustainedPerformanceMode: Boolean
    ) {
        context.dataStore.edit { prefs ->
            prefs[Keys.ACTIVE_PROFILE_ID] = profileId
            prefs[Keys.SAMPLING_INTERVAL_MS] = samplingIntervalMs
            prefs[Keys.FOCUS_MODE_ENABLED] = focusMode
            prefs[Keys.KEEP_SCREEN_ON] = keepScreenOn
            prefs[Keys.HAPTIC_FEEDBACK] = hapticFeedback
            prefs[Keys.BRIGHTNESS_PERCENT] = brightnessPercent
            prefs[Keys.MEDIA_VOLUME_PERCENT] = mediaVolumePercent
            prefs[Keys.PREFERRED_REFRESH_HZ] = preferredRefreshHz
            prefs[Keys.SUSTAINED_PERF_MODE] = sustainedPerformanceMode
        }
    }

    suspend fun setFocusMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.FOCUS_MODE_ENABLED] = enabled
        }
    }

    suspend fun setSustainedPerformanceMode(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SUSTAINED_PERF_MODE] = enabled
        }
    }

    suspend fun setSelectedAdvancedTier(tier: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SELECTED_ADVANCED_TIER] = tier
        }
    }
}
