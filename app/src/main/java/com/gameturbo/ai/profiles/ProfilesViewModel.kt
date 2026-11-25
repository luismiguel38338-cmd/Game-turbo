package com.gameturbo.ai.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gameturbo.ai.data.AppContainer
import com.gameturbo.ai.data.db.GamingProfileEntity
import com.gameturbo.ai.data.prefs.AppPreferencesState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class ProfilesUiState(
    val profiles: List<GamingProfileEntity> = emptyList(),
    val preferences: AppPreferencesState = AppPreferencesState()
)

class ProfilesViewModel(private val container: AppContainer) : ViewModel() {

    val uiState: StateFlow<ProfilesUiState> = combine(
        container.profileRepository.profiles,
        container.preferencesRepository.preferencesFlow
    ) { profiles, prefs ->
        ProfilesUiState(
            profiles = profiles,
            preferences = prefs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ProfilesUiState()
    )

    init {
        viewModelScope.launch {
            container.profileRepository.ensureDefaultProfiles()
        }
    }

    fun activateProfile(profile: GamingProfileEntity) {
        viewModelScope.launch {
            container.profileRepository.activateProfile(profile)
            container.optimizerEngine.runOptimizationPipeline(
                profile = profile,
                enableFocusMode = profile.focusMode,
                measurePing = false
            )
        }
    }

    fun saveCustomProfile(
        existingId: Long? = null,
        name: String,
        samplingIntervalMs: Long,
        focusMode: Boolean,
        keepScreenOn: Boolean,
        hapticFeedback: Boolean,
        brightnessPercent: Int,
        mediaVolumePercent: Int,
        preferredRefreshHz: Int,
        description: String
    ) {
        viewModelScope.launch {
            container.profileRepository.saveCustomProfile(
                existingId = existingId,
                name = name,
                samplingIntervalMs = samplingIntervalMs,
                focusMode = focusMode,
                keepScreenOn = keepScreenOn,
                hapticFeedback = hapticFeedback,
                brightnessPercent = brightnessPercent,
                mediaVolumePercent = mediaVolumePercent,
                preferredRefreshHz = preferredRefreshHz,
                description = description
            )
        }
    }

    fun deleteProfile(profile: GamingProfileEntity) {
        viewModelScope.launch {
            container.profileRepository.deleteCustomProfile(profile)
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ProfilesViewModel(container) as T
        }
    }
}
