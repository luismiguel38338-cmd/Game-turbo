package com.gameturbo.ai.performance

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.gameturbo.ai.data.AppContainer
import com.gameturbo.ai.data.db.TelemetrySampleEntity
import com.gameturbo.ai.data.repository.AppStorageInspection
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class PerformanceUiState(
    val snapshot: DeviceSnapshot? = null,
    val history: List<TelemetrySampleEntity> = emptyList(),
    val storageInspection: AppStorageInspection = AppStorageInspection(emptyList(), 0L, 0L),
    val selectedFilePaths: Set<String> = emptySet(),
    val lastStorageMessage: String? = null
)

class PerformanceViewModel(private val container: AppContainer) : ViewModel() {

    private val snapshotState = MutableStateFlow<DeviceSnapshot?>(null)
    private val storageInspectionState = MutableStateFlow(AppStorageInspection(emptyList(), 0L, 0L))
    private val selectedFilesState = MutableStateFlow<Set<String>>(emptySet())
    private val storageMsgState = MutableStateFlow<String?>(null)

    val uiState: StateFlow<PerformanceUiState> = combine(
        snapshotState,
        container.telemetryRepository.recentSamples,
        storageInspectionState,
        selectedFilesState,
        storageMsgState
    ) { snapshot, samples, storageInspection, selected, msg ->
        PerformanceUiState(
            snapshot = snapshot,
            history = samples.reversed(),
            storageInspection = storageInspection,
            selectedFilePaths = selected,
            lastStorageMessage = msg
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PerformanceUiState()
    )

    init {
        viewModelScope.launch {
            refreshStorageInspection()
            while (true) {
                runCatching {
                    snapshotState.value = container.telemetryRepository.captureImmediate()
                }
                delay(2500L)
            }
        }
    }

    fun refreshNow() {
        viewModelScope.launch {
            runCatching {
                snapshotState.value = container.telemetryRepository.captureAndPersist()
            }
            refreshStorageInspection()
        }
    }

    fun refreshStorageInspection() {
        viewModelScope.launch {
            storageInspectionState.value = container.storageRepository.inspectAppOwnedStorage()
        }
    }

    fun toggleFileSelection(path: String) {
        val current = selectedFilesState.value.toMutableSet()
        if (!current.add(path)) current.remove(path)
        selectedFilesState.value = current
    }

    fun selectAllTempFiles() {
        val tempPaths = storageInspectionState.value.files
            .filter { it.isTemporary }
            .map { it.path }
            .toSet()
        selectedFilesState.value = tempPaths
    }

    fun clearSelection() {
        selectedFilesState.value = emptySet()
    }

    fun deleteConfirmedSelectedFiles() {
        viewModelScope.launch {
            val paths = selectedFilesState.value
            val deleted = container.storageRepository.deleteSelectedFiles(paths)
            selectedFilesState.value = emptySet()
            storageMsgState.value = if (deleted > 0) {
                "Se eliminaron $deleted archivos temporales propios de la app."
            } else {
                "No se eliminó ningún archivo."
            }
            refreshStorageInspection()
        }
    }

    fun clearTelemetryHistory() {
        viewModelScope.launch {
            container.telemetryRepository.clearHistory()
        }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return PerformanceViewModel(container) as T
        }
    }
}
