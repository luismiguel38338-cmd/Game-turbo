package com.gameturbo.ai.memory

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gameturbo.ai.data.db.TelemetrySampleEntity
import com.gameturbo.ai.performance.RamTelemetry
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.ElectricRoyalBlue
import com.gameturbo.ai.ui.theme.MetricKeyValueRow
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.RadialTelemetryGauge
import com.gameturbo.ai.ui.theme.SectionHeader
import com.gameturbo.ai.ui.theme.StatusHigh
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill
import com.gameturbo.ai.ui.theme.TelemetryLineChart

@Composable
fun RamMonitorContent(
    ram: RamTelemetry?,
    history: List<TelemetrySampleEntity>,
    onClearHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (ram == null) {
            item {
                CyberCard { Text("Leyendo ActivityManager.MemoryInfo...") }
            }
            return@LazyColumn
        }

        item {
            CyberCard(accentColor = NeonCyan) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Monitor de Memoria RAM",
                        subtitle = "Lectura oficial ActivityManager.MemoryInfo",
                        icon = Icons.Filled.Memory,
                        modifier = Modifier.weight(1f)
                    )
                    StatusPill(
                        label = if (ram.lowMemory) "Memoria baja" else "Estable",
                        color = if (ram.lowMemory) StatusHigh else StatusOptimal
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RadialTelemetryGauge(
                        progress = ram.usedPercent / 100f,
                        centerLabel = "${"%.1f".format(ram.usedPercent)}%",
                        centerSubtitle = "RAM en uso",
                        size = 136.dp,
                        strokeWidth = 12.dp
                    )
                }
                Spacer(modifier = Modifier.height(14.dp))
                MetricKeyValueRow("RAM total del sistema", "${ram.totalMb} MB")
                MetricKeyValueRow("RAM disponible", "${ram.availableMb} MB", valueColor = StatusOptimal)
                MetricKeyValueRow("RAM utilizada", "${ram.usedMb} MB", valueColor = NeonCyan)
                MetricKeyValueRow("Umbral de memoria baja", "${ram.thresholdMb} MB")
                MetricKeyValueRow("Heap JVM de Game Turbo AI", "${ram.appHeapUsedMb} / ${ram.appHeapMaxMb} MB")
            }
        }

        item {
            CyberCard(accentColor = ElectricRoyalBlue) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Historial de mediciones (%)", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onClearHistory) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TelemetryLineChart(
                    values = history.map { it.ramUsedPercent },
                    maxValue = 100f,
                    minValue = 0f,
                    lineColor = NeonCyan,
                    unitSuffix = "%"
                )
            }
        }

        item {
            CyberCard(accentColor = StatusInfo) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = StatusInfo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Política segura de Android: Game Turbo AI consulta el estado de memoria mediante las APIs oficiales sin cerrar aplicaciones ni eliminar procesos protegidos del sistema.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
