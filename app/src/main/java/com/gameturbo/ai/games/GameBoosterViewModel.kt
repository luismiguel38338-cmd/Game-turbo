package com.gameturbo.ai.games

import android.content.Context
import android.content.Intent
import android.graphics.drawable.Drawable
import android.widget.Toast
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gameturbo.ai.data.AppContainer
import com.gameturbo.ai.data.db.GameSessionEntity
import com.gameturbo.ai.data.db.GamingProfileEntity
import com.gameturbo.ai.data.db.RegisteredGameEntity
import com.gameturbo.ai.data.repository.LaunchableAppItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class GameCardModel(
    val entity: RegisteredGameEntity,
    val icon: Drawable?,
    val isInstalled: Boolean,
    val assignedProfileName: String
)

data class GameBoosterUiState(
    val registeredGames: List<GameCardModel> = emptyList(),
    val installedLauncherApps: List<LaunchableAppItem> = emptyList(),
    val sessions: List<GameSessionEntity> = emptyList(),
    val profiles: List<GamingProfileEntity> = emptyList(),
    val focusModeEnabled: Boolean = false,
    val isScanning: Boolean = false,
    val lastScanMessage: String? = null
)

class GameBoosterViewModel(private val container: AppContainer) : ViewModel() {

    private val installedAppsState = MutableStateFlow<List<LaunchableAppItem>>(emptyList())
    private val scanningState = MutableStateFlow(false)
    private val scanMessageState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<GameBoosterUiState> = combine(
        container.gameRepository.registeredGames,
        container.gameRepository.sessions,
        container.profileRepository.profiles,
        container.preferencesRepository.preferencesFlow,
        installedAppsState
    ) { games, sessions, profiles, prefs, installedApps ->
        val profileMap = profiles.associateBy { it.id }
        val cards = games.map { game ->
            val launchable = container.gameRepository.getLaunchIntent(game.packageName) != null
            GameCardModel(
                entity = game,
                icon = container.gameRepository.loadAppIcon(game.packageName),
                isInstalled = launchable,
                assignedProfileName = game.assignedProfileId?.let { profileMap[it]?.name } ?: "Perfil activo"
            )
        }
        GameBoosterUiState(
            registeredGames = cards,
            installedLauncherApps = installedApps,
            sessions = sessions,
            profiles = profiles,
            focusModeEnabled = prefs.focusModeEnabled,
            isScanning = scanningState.value,
            lastScanMessage = scanMessageState.value
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = GameBoosterUiState()
    )

    init {
        refreshInstalledAppsAndDetectGames()
    }

    fun refreshInstalledAppsAndDetectGames() {
        viewModelScope.launch {
            scanningState.value = true
            runCatching {
                val detected = container.gameRepository.syncDetectedGames()
                val apps = container.gameRepository.queryInstalledLauncherApps()
                installedAppsState.value = apps
                scanMessageState.value = if (detected > 0) {
                    "Detectados $detected videojuegos con categoría GAME."
                } else {
                    "No se detectaron apps con CATEGORY_GAME; puedes añadir juegos manualmente desde tus apps instaladas."
                }
            }
            scanningState.value = false
        }
    }

    fun registerAppAsGame(
        app: LaunchableAppItem,
        profileId: Long?,
        focusMode: Boolean,
        keepScreenOn: Boolean
    ) {
        viewModelScope.launch {
            container.gameRepository.registerManualGame(
                packageName = app.packageName,
                displayName = app.label,
                profileId = profileId,
                focusMode = focusMode,
                keepScreenOn = keepScreenOn
            )
        }
    }

    fun updateGame(game: RegisteredGameEntity) {
        viewModelScope.launch {
            container.gameRepository.updateGameConfig(game)
        }
    }

    fun removeGame(packageName: String) {
        viewModelScope.launch {
            container.gameRepository.removeGame(packageName)
        }
    }

    fun toggleFocusMode() {
        viewModelScope.launch {
            val next = !uiState.value.focusModeEnabled
            container.preferencesRepository.setFocusMode(next)
        }
    }

    fun clearSessionHistory() {
        viewModelScope.launch {
            container.gameRepository.clearSessions()
        }
    }

    fun launchGame(context: Context, model: GameCardModel) {
        viewModelScope.launch {
            val snapshot = container.telemetryRepository.captureImmediate()
            val assignedProfile = model.entity.assignedProfileId?.let { id ->
                uiState.value.profiles.firstOrNull { it.id == id }
            }
            if (assignedProfile != null) {
                container.profileRepository.activateProfile(assignedProfile)
            } else if (model.entity.focusModeEnabled) {
                container.preferencesRepository.setFocusMode(true)
            }

            container.gameRepository.recordSessionLaunch(
                game = model.entity,
                profileName = assignedProfile?.name ?: "Perfil actual",
                ramPercent = snapshot.ram.usedPercent.toInt(),
                batteryPercent = snapshot.battery.levelPercent,
                tempC = snapshot.battery.temperatureC ?: -1f
            )

            val launchIntent = container.gameRepository.getLaunchIntent(model.entity.packageName)
            if (launchIntent != null) {
                launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                try {
                    context.startActivity(launchIntent)
                } catch (_: Exception) {
                    Toast.makeText(
                        context,
                        "No se pudo abrir ${model.entity.displayName} en este dispositivo.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(
                    context,
                    "${model.entity.displayName} no está instalado o no tiene actividad lanzable.",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return GameBoosterViewModel(container) as T
        }
    }
}
