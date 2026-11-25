package com.gameturbo.ai.advanced

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gameturbo.ai.data.AppContainer
import com.gameturbo.ai.data.db.SecurityAuditEntity
import com.gameturbo.ai.data.repository.AdvancedCapabilitiesSnapshot
import com.gameturbo.ai.data.repository.AdvancedTier
import com.gameturbo.ai.performance.DeviceSnapshot
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AdvancedUiState(
    val selectedTier: AdvancedTier = AdvancedTier.NORMAL,
    val capabilities: AdvancedCapabilitiesSnapshot = AdvancedCapabilitiesSnapshot(
        canWriteSystemSettings = false,
        hasDndPolicyAccess = false,
        ignoringBatteryOptimizations = false,
        isShizukuInstalled = false,
        isAdbEnabledInSystem = false,
        suBinaryPresent = false
    ),
    val snapshot: DeviceSnapshot? = null,
    val auditLogs: List<SecurityAuditEntity> = emptyList()
)

class AdvancedViewModel(private val container: AppContainer) : ViewModel() {

    private val capabilitiesState = MutableStateFlow(AdvancedUiState().capabilities)
    private val snapshotState = MutableStateFlow<DeviceSnapshot?>(null)

    val uiState: StateFlow<AdvancedUiState> = combine(
        container.preferencesRepository.preferencesFlow,
        capabilitiesState,
        snapshotState,
        container.advancedRepository.auditLogs
    ) { prefs, caps, snapshot, logs ->
        val tier = AdvancedTier.entries.firstOrNull { it.name == prefs.selectedAdvancedTier }
            ?: AdvancedTier.NORMAL
        AdvancedUiState(
            selectedTier = tier,
            capabilities = caps,
            snapshot = snapshot,
            auditLogs = logs
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = AdvancedUiState()
    )

    init {
        refreshCapabilities()
    }

    fun refreshCapabilities() {
        viewModelScope.launch {
            capabilitiesState.value = container.advancedRepository.inspectCapabilities()
            snapshotState.value = container.telemetryRepository.captureImmediate()
        }
    }

    fun selectTier(tier: AdvancedTier) {
        viewModelScope.launch {
            container.preferencesRepository.setSelectedAdvancedTier(tier.name)
            container.advancedRepository.logAuditEvent(
                category = "ADVANCED_MODE",
                action = "Selección de nivel",
                detail = "Modo inspeccionado: ${tier.title} (${tier.badge})",
                mode = tier.title
            )
            refreshCapabilities()
        }
    }

    fun logSystemShortcutAccess(toolName: String) {
        viewModelScope.launch {
            container.advancedRepository.logAuditEvent(
                category = "TOOLS",
                action = "Acceso a herramienta",
                detail = toolName,
                mode = uiState.value.selectedTier.title
            )
        }
    }

    fun clearAuditLogs() {
        viewModelScope.launch {
            container.advancedRepository.clearAuditLogs()
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return AdvancedViewModel(container) as T
        }
    }
}
