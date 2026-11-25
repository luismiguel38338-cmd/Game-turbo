package com.gameturbo.ai.temperature

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.gameturbo.ai.data.db.TelemetrySampleEntity
import com.gameturbo.ai.performance.TemperatureTelemetry
import com.gameturbo.ai.performance.ThermalLevel
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.JetBrainsMonoFamily
import com.gameturbo.ai.ui.theme.MetricKeyValueRow
import com.gameturbo.ai.ui.theme.SectionHeader
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusHigh
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill
import com.gameturbo.ai.ui.theme.TelemetryLineChart

@Composable
fun TemperatureMonitorContent(
    temperature: TemperatureTelemetry?,
    history: List<TelemetrySampleEntity>,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (temperature == null) {
            item { CyberCard { Text("Leyendo sensores térmicos disponibles...") } }
            return@LazyColumn
        }

        val levelColor = when (temperature.level) {
            ThermalLevel.NORMAL -> StatusOptimal
            ThermalLevel.ELEVATED -> StatusElevated
            ThermalLevel.HIGH -> StatusHigh
            ThermalLevel.UNAVAILABLE -> StatusInfo
        }

        item {
            CyberCard(accentColor = levelColor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Monitor Térmico",
                        subtitle = "Sensores oficiales de batería y estado PowerManager",
                        icon = Icons.Filled.Thermostat,
                        accentColor = levelColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatusPill(label = temperature.level.label, color = levelColor)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = temperature.batteryTempC?.let { "${"%.1f".format(it)} °C" }
                        ?: "No disponible",
                    style = MaterialTheme.typography.displaySmall.copy(fontFamily = JetBrainsMonoFamily),
                    color = levelColor
                )
                Text(
                    text = "Temperatura de batería reportada por Android",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(12.dp))
                MetricKeyValueRow("Estado térmico del sistema", temperature.systemThermalStatus, valueColor = levelColor)
                MetricKeyValueRow(
                    "Sensor ambiente",
                    temperature.ambientTempC?.let { "${"%.1f".format(it)} °C" }
                        ?: "No disponible en este hardware"
                )
                MetricKeyValueRow("Temperatura CPU / GPU", "Restringida por Android (sin acceso sin privilegios)")
            }
        }

        item {
            CyberCard(accentColor = levelColor) {
                Text("Historial térmico de batería (°C)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                val validTemps = history.mapNotNull { it.batteryTempC.takeIf { t -> t >= 0f } }
                TelemetryLineChart(
                    values = validTemps,
                    maxValue = 55f,
                    minValue = 15f,
                    lineColor = levelColor,
                    unitSuffix = "°C"
                )
            }
        }

        item {
            CyberCard(accentColor = StatusInfo) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = StatusInfo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = temperature.sensorNote,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}
