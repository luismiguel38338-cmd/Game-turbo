package com.gameturbo.ai.performance

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.gameturbo.ai.battery.BatteryMonitorContent
import com.gameturbo.ai.cpu.CpuMonitorContent
import com.gameturbo.ai.memory.RamMonitorContent
import com.gameturbo.ai.storage.StorageManagerContent
import com.gameturbo.ai.temperature.TemperatureMonitorContent
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.ObsidianDeep

@Composable
fun PerformanceHubScreen(
    state: PerformanceUiState,
    selectedSubTab: Int,
    onSelectSubTab: (Int) -> Unit,
    onRefreshNow: () -> Unit,
    onClearTelemetryHistory: () -> Unit,
    onRefreshStorage: () -> Unit,
    onToggleStorageFile: (String) -> Unit,
    onSelectTempStorageFiles: () -> Unit,
    onClearStorageSelection: () -> Unit,
    onDeleteConfirmedStorageFiles: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        "RAM" to Icons.Filled.Memory,
        "CPU" to Icons.Filled.DeveloperBoard,
        "Temperatura" to Icons.Filled.Thermostat,
        "Batería" to Icons.Filled.BatteryChargingFull,
        "Almacenamiento" to Icons.Filled.SdStorage
    )

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier
                        .weight(1f)
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    tabs.forEachIndexed { index, (label, icon) ->
                        FilterChip(
                            selected = selectedSubTab == index,
                            onClick = { onSelectSubTab(index) },
                            modifier = Modifier.testTag("monitor_tab_$index"),
                            label = { Text(label) },
                            leadingIcon = { Icon(icon, contentDescription = null) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = ObsidianDeep,
                                selectedLeadingIconColor = ObsidianDeep
                            )
                        )
                    }
                }
                IconButton(onClick = onRefreshNow) {
                    Icon(
                        Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.cd_refresh_telemetry),
                        tint = NeonCyan
                    )
                }
            }

            when (selectedSubTab) {
                0 -> RamMonitorContent(
                    ram = state.snapshot?.ram,
                    history = state.history,
                    onClearHistory = onClearTelemetryHistory
                )
                1 -> CpuMonitorContent(cpu = state.snapshot?.cpu)
                2 -> TemperatureMonitorContent(
                    temperature = state.snapshot?.temperature,
                    history = state.history
                )
                3 -> BatteryMonitorContent(
                    battery = state.snapshot?.battery,
                    history = state.history
                )
                else -> StorageManagerContent(
                    storage = state.snapshot?.storage,
                    inspection = state.storageInspection,
                    selectedPaths = state.selectedFilePaths,
                    lastMessage = state.lastStorageMessage,
                    onRefresh = onRefreshStorage,
                    onToggleSelect = onToggleStorageFile,
                    onSelectTempFiles = onSelectTempStorageFiles,
                    onClearSelection = onClearStorageSelection,
                    onDeleteConfirmed = onDeleteConfirmedStorageFiles
                )
            }
        }
    }
}
