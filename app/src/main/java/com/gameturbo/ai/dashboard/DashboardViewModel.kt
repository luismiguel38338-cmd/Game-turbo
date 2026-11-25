package com.gameturbo.ai.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gameturbo.ai.data.AppContainer
import com.gameturbo.ai.data.db.GamingProfileEntity
import com.gameturbo.ai.data.db.TelemetrySampleEntity
import com.gameturbo.ai.data.prefs.AppPreferencesState
import com.gameturbo.ai.games.OptimizationResult
import com.gameturbo.ai.performance.DeviceSnapshot
import com.gameturbo.ai.performance.PingResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class DashboardUiState(
    val snapshot: DeviceSnapshot? = null,
    val recentSamples: List<TelemetrySampleEntity> = emptyList(),
    val registeredGamesCount: Int = 0,
    val activeProfile: GamingProfileEntity? = null,
    val preferences: AppPreferencesState = AppPreferencesState(),
    val latestPing: PingResult? = null,
    val lastOptimization: OptimizationResult? = null,
    val isOptimizing: Boolean = false,
    val isTestingPing: Boolean = false
)

class DashboardViewModel(private val container: AppContainer) : ViewModel() {

    private val snapshotState = MutableStateFlow<DeviceSnapshot?>(null)
    private val pingState = MutableStateFlow<PingResult?>(null)
    private val lastOptState = MutableStateFlow<OptimizationResult?>(null)
    private val optimizingState = MutableStateFlow(false)
    private val testingPingState = MutableStateFlow(false)

    private val liveExtrasFlow = combine(
        pingState,
        lastOptState,
        optimizingState,
        testingPingState
    ) { ping, opt, isOpt, isPing ->
        QuadExtras(ping, opt, isOpt, isPing)
    }

    private data class QuadExtras(
        val ping: PingResult?,
        val opt: OptimizationResult?,
        val isOpt: Boolean,
        val isPing: Boolean
    )

    val uiState: StateFlow<DashboardUiState> = combine(
        combine(
            snapshotState,
            container.telemetryRepository.recentSamples,
            container.gameRepository.registeredGameCount,
            container.profileRepository.profiles,
            container.preferencesRepository.preferencesFlow
        ) { snapshot, samples, gameCount, profiles, prefs ->
            val activeProfile = profiles.firstOrNull { it.id == prefs.activeProfileId } ?: profiles.firstOrNull()
            DashboardUiState(
                snapshot = snapshot,
                recentSamples = samples.reversed(),
                registeredGamesCount = gameCount,
                activeProfile = activeProfile,
                preferences = prefs
            )
        },
        liveExtrasFlow
    ) { base, extras ->
        base.copy(
            latestPing = extras.ping,
            lastOptimization = extras.opt,
            isOptimizing = extras.isOpt,
            isTestingPing = extras.isPing
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = DashboardUiState()
    )

    init {
        viewModelScope.launch {
            container.profileRepository.ensureDefaultProfiles()
        }
        startPeriodicPolling()
    }

    private fun startPeriodicPolling() {
        viewModelScope.launch {
            var currentIntervalMs = 3000L
            launch {
                container.preferencesRepository.preferencesFlow
                    .map { it.samplingIntervalMs }
                    .distinctUntilChanged()
                    .collect { currentIntervalMs = it.coerceIn(1000L, 10000L) }
            }
            while (true) {
                runCatching {
                    snapshotState.value = container.telemetryRepository.captureAndPersist()
                }
                delay(currentIntervalMs)
            }
        }
    }

    fun refreshNow() {
        viewModelScope.launch {
            runCatching {
                snapshotState.value = container.telemetryRepository.captureAndPersist()
            }
        }
    }

    fun runQuickOptimize() {
        viewModelScope.launch {
            optimizingState.value = true
            runCatching {
                val activeProfile = uiState.value.activeProfile
                val result = container.optimizerEngine.runOptimizationPipeline(
                    profile = activeProfile,
                    enableFocusMode = uiState.value.preferences.focusModeEnabled,
                    measurePing = true
                )
                lastOptState.value = result
                pingState.value = result.pingResult ?: pingState.value
                snapshotState.value = container.telemetryRepository.captureAndPersist()
            }
            optimizingState.value = false
        }
    }

    fun dismissOptimizationResult() {
        lastOptState.value = null
    }

    fun runPingTest() {
        viewModelScope.launch {
            testingPingState.value = true
            runCatching {
                val p1 = container.telemetryReader.measureNetworkLatency("1.1.1.1", 443)
                val p2 = container.telemetryReader.measureNetworkLatency("8.8.8.8", 443)
                pingState.value = listOfNotNull(p1, p2).minByOrNull { it.avgMs }
            }
            testingPingState.value = false
        }
    }

    fun toggleFocusMode() {
        viewModelScope.launch {
            val next = !uiState.value.preferences.focusModeEnabled
            container.preferencesRepository.setFocusMode(next)
            container.optimizerEngine.applySystemDnd(next)
            container.advancedRepository.logAuditEvent(
                category = "DASHBOARD",
                action = if (next) "Modo concentración activado" else "Modo concentración desactivado",
                detail = "Cambio desde Panel principal.",
                mode = "Normal"
            )
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return DashboardViewModel(container) as T
        }
    }
}
