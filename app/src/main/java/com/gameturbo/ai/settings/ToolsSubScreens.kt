package com.gameturbo.ai.settings

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.SettingsApplications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.gameturbo.ai.data.repository.AdvancedCapabilitiesSnapshot
import com.gameturbo.ai.performance.DeviceSnapshot
import com.gameturbo.ai.security.SecurityValidator
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.ElectricRoyalBlue
import com.gameturbo.ai.ui.theme.MetricKeyValueRow
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.SectionHeader
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal

@Composable
fun GamingToolsContent(
    snapshot: DeviceSnapshot?,
    capabilities: AdvancedCapabilitiesSnapshot,
    onLogShortcut: (String) -> Unit,
    onOpenPerformanceHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (snapshot != null) {
            item {
                CyberCard(accentColor = NeonCyan) {
                    SectionHeader(
                        title = "Información del Dispositivo",
                        subtitle = "Identidad oficial Build.*",
                        icon = Icons.Filled.PhoneAndroid
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MetricKeyValueRow("Fabricante / Marca", "${snapshot.manufacturer} (${snapshot.brand})")
                    MetricKeyValueRow("Modelo", snapshot.model, valueColor = NeonCyan)
                    MetricKeyValueRow("Versión Android", "${snapshot.androidVersion} (API ${snapshot.sdkInt})")
                    MetricKeyValueRow("Parche de seguridad", snapshot.securityPatch)
                }
            }

            item {
                CyberCard(accentColor = ElectricRoyalBlue) {
                    SectionHeader(
                        title = "Información de Pantalla",
                        subtitle = "Métricas reales de resolución y refresco",
                        icon = Icons.Filled.DisplaySettings,
                        accentColor = ElectricRoyalBlue
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MetricKeyValueRow(
                        "Resolución física",
                        "${snapshot.display.widthPx} × ${snapshot.display.heightPx} px",
                        valueColor = NeonCyan
                    )
                    MetricKeyValueRow("Densidad", "${snapshot.display.densityDpi} dpi")
                    MetricKeyValueRow("Tasa de refresco", "${snapshot.display.refreshRateHz.toInt()} Hz", valueColor = StatusOptimal)
                    MetricKeyValueRow("Soporte HDR", if (snapshot.display.hdrSupported) "Compatible" else "No reportado")
                }
            }

            item {
                CyberCard(accentColor = StatusInfo) {
                    SectionHeader(
                        title = "Información de Conectividad",
                        subtitle = "Estado de red actual via ConnectivityManager",
                        icon = Icons.Filled.NetworkCheck,
                        accentColor = StatusInfo
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    MetricKeyValueRow("Transporte activo", snapshot.connectivity.transportLabel, valueColor = NeonCyan)
                    MetricKeyValueRow(
                        "Acceso a Internet",
                        if (snapshot.connectivity.isConnected) "Conectado" else "Sin acceso",
                        valueColor = if (snapshot.connectivity.isConnected) StatusOptimal else StatusElevated
                    )
                    MetricKeyValueRow("Red medida (coste)", if (snapshot.connectivity.isMetered) "Sí" else "No")
                    MetricKeyValueRow(
                        "Ancho de banda estimado",
                        snapshot.connectivity.downstreamKbps?.let { "${it / 1000} Mbps bajada" }
                            ?: "No reportado"
                    )
                }
            }
        }

        item {
            CyberCard(accentColor = StatusOptimal) {
                SectionHeader(
                    title = "Estado de Servicios y Permisos",
                    subtitle = "Capacidades autorizadas en este dispositivo",
                    icon = Icons.Filled.SettingsApplications,
                    accentColor = StatusOptimal
                )
                Spacer(modifier = Modifier.height(10.dp))
                MetricKeyValueRow(
                    "Escritura ajustes sistema",
                    if (capabilities.canWriteSystemSettings) "Autorizado" else "Requiere permiso",
                    valueColor = if (capabilities.canWriteSystemSettings) StatusOptimal else StatusElevated
                )
                MetricKeyValueRow(
                    "Acceso No Molestar (DND)",
                    if (capabilities.hasDndPolicyAccess) "Autorizado" else "Requiere permiso",
                    valueColor = if (capabilities.hasDndPolicyAccess) StatusOptimal else StatusElevated
                )
                MetricKeyValueRow(
                    "Exclusión ahorro batería",
                    if (capabilities.ignoringBatteryOptimizations) "Sin restricción" else "Optimizado por Android"
                )
                MetricKeyValueRow(
                    "Depuración ADB global",
                    if (capabilities.isAdbEnabledInSystem) "Habilitada" else "Deshabilitada"
                )
            }
        }

        item {
            CyberCard(accentColor = NeonCyan) {
                SectionHeader(
                    title = "Accesos a Ajustes del Sistema",
                    subtitle = "Abre las pantallas oficiales cuando Android restringe cambios directos",
                    icon = Icons.Filled.Brightness6
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                onLogShortcut("Ajustes de pantalla y brillo")
                                SecurityValidator.safeStartActivity(context, Intent(Settings.ACTION_DISPLAY_SETTINGS))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.Brightness6, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Brillo")
                        }
                        OutlinedButton(
                            onClick = {
                                onLogShortcut("Ajustes de sonido")
                                SecurityValidator.safeStartActivity(context, Intent(Settings.ACTION_SOUND_SETTINGS))
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sonido")
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = {
                                onLogShortcut("Ajustes de batería")
                                SecurityValidator.safeStartActivity(
                                    context,
                                    Intent(Intent.ACTION_POWER_USAGE_SUMMARY),
                                    fallbackIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                                )
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.BatteryChargingFull, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Batería")
                        }
                        OutlinedButton(
                            onClick = {
                                onLogShortcut("Historial de rendimiento")
                                onOpenPerformanceHistory()
                            },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Filled.History, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Historial")
                        }
                    }
                }
            }
        }
    }
}
