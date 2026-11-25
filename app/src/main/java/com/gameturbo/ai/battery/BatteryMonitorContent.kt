package com.gameturbo.ai.battery

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gameturbo.ai.data.db.TelemetrySampleEntity
import com.gameturbo.ai.performance.BatteryTelemetry
import com.gameturbo.ai.security.SecurityValidator
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.MetricKeyValueRow
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.RadialTelemetryGauge
import com.gameturbo.ai.ui.theme.SectionHeader
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill
import com.gameturbo.ai.ui.theme.TelemetryLineChart

@Composable
fun BatteryMonitorContent(
    battery: BatteryTelemetry?,
    history: List<TelemetrySampleEntity>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (battery == null) {
            item { CyberCard { Text("Leyendo BatteryManager...") } }
            return@LazyColumn
        }

        val batteryColor = when {
            battery.levelPercent <= 15 -> StatusElevated
            else -> StatusOptimal
        }

        item {
            CyberCard(accentColor = batteryColor) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    SectionHeader(
                        title = "Monitor de Batería",
                        subtitle = "Telemetría directa BatteryManager",
                        icon = Icons.Filled.BatteryChargingFull,
                        accentColor = batteryColor,
                        modifier = Modifier.weight(1f)
                    )
                    StatusPill(label = battery.statusLabel, color = batteryColor)
                }
                Spacer(modifier = Modifier.height(14.dp))
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    RadialTelemetryGauge(
                        progress = battery.levelPercent / 100f,
                        centerLabel = "${battery.levelPercent}%",
                        centerSubtitle = battery.pluggedLabel,
                        accentColor = batteryColor,
                        size = 132.dp,
                        strokeWidth = 12.dp
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
                MetricKeyValueRow("Estado de salud", battery.healthLabel, valueColor = batteryColor)
                MetricKeyValueRow(
                    "Temperatura",
                    battery.temperatureC?.let { "${"%.1f".format(it)} °C" } ?: "No disponible"
                )
                MetricKeyValueRow(
                    "Tecnología",
                    battery.technology ?: "Android no la expone en este dispositivo"
                )
                MetricKeyValueRow(
                    "Voltaje",
                    battery.voltageMv?.let { "$it mV" } ?: "No disponible"
                )
                MetricKeyValueRow(
                    "Corriente instantánea",
                    battery.currentNowMa?.let { "$it mA" } ?: "No expuesta por el controlador"
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = {
                        SecurityValidator.safeStartActivity(
                            context,
                            Intent(Intent.ACTION_POWER_USAGE_SUMMARY),
                            fallbackIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Filled.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir configuración de batería del sistema")
                }
            }
        }

        item {
            CyberCard(accentColor = NeonCyan) {
                Text("Historial de nivel de batería (%)", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                TelemetryLineChart(
                    values = history.map { it.batteryLevel.toFloat() },
                    maxValue = 100f,
                    minValue = 0f,
                    lineColor = StatusOptimal,
                    unitSuffix = "%"
                )
            }
        }
    }
}
