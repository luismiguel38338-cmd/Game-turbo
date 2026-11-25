package com.gameturbo.ai.cpu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gameturbo.ai.performance.CpuTelemetry
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.ElectricRoyalBlue
import com.gameturbo.ai.ui.theme.JetBrainsMonoFamily
import com.gameturbo.ai.ui.theme.MetricKeyValueRow
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.SectionHeader
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill

@Composable
fun CpuMonitorContent(
    cpu: CpuTelemetry?,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (cpu == null) {
            item { CyberCard { Text("Consultando arquitectura de procesador...") } }
            return@LazyColumn
        }

        item {
            CyberCard(accentColor = ElectricRoyalBlue) {
                SectionHeader(
                    title = "Monitor de Procesador (CPU)",
                    subtitle = "Metadatos del sistema y lectura segura de núcleos",
                    icon = Icons.Filled.DeveloperBoard,
                    accentColor = ElectricRoyalBlue
                )
                Spacer(modifier = Modifier.height(12.dp))
                MetricKeyValueRow("Procesador / SoC", cpu.processorLabel, valueColor = NeonCyan)
                MetricKeyValueRow("Arquitectura principal", cpu.architecture)
                MetricKeyValueRow("ABIs compatibles", cpu.supportedAbis.joinToString(", ").ifBlank { "No disponible" })
                MetricKeyValueRow("Núcleos detectados", "${cpu.coreCount} núcleos lógicos", valueColor = StatusOptimal)
                MetricKeyValueRow(
                    "Gobernador CPU0",
                    cpu.governor ?: "No expuesto por Android (SELinux)"
                )
            }
        }

        item {
            CyberCard(accentColor = StatusElevated) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Lock, contentDescription = null, tint = StatusElevated)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = cpu.loadRestrictedNote + " Tampoco se modifican frecuencias de CPU sin permisos de fabricante o root autorizado.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(cpu.coreFrequencies, key = { it.coreIndex }) { core ->
            CyberCard(accentColor = StatusInfo) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Núcleo CPU #${core.coreIndex}",
                            style = MaterialTheme.typography.titleMedium
                        )
                        val rangeText = if (core.minMhz != null && core.maxMhz != null) {
                            "Rango: ${core.minMhz} – ${core.maxMhz} MHz"
                        } else {
                            "Rango MHz restringido por Android"
                        }
                        Text(
                            text = rangeText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    if (core.currentMhz != null) {
                        Text(
                            text = "${core.currentMhz} MHz",
                            style = MaterialTheme.typography.titleLarge.copy(fontFamily = JetBrainsMonoFamily),
                            color = NeonCyan
                        )
                    } else {
                        StatusPill(label = "Bloqueado", color = StatusElevated)
                    }
                }
            }
        }
    }
}
