package com.gameturbo.ai.dashboard

import android.content.Intent
import android.provider.Settings
import android.text.format.Formatter
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.DeveloperBoard
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Thermostat
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.gameturbo.ai.performance.ThermalLevel
import com.gameturbo.ai.security.SecurityValidator
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.ElectricRoyalBlue
import com.gameturbo.ai.ui.theme.JetBrainsMonoFamily
import com.gameturbo.ai.ui.theme.MetricKeyValueRow
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.ObsidianDeep
import com.gameturbo.ai.ui.theme.RadialTelemetryGauge
import com.gameturbo.ai.ui.theme.RoyalBorder
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusHigh
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill
import com.gameturbo.ai.ui.theme.TelemetryLineChart

@Composable
fun DashboardScreen(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    onQuickOptimize: () -> Unit,
    onDismissOptimization: () -> Unit,
    onTestPing: () -> Unit,
    onToggleFocusMode: () -> Unit,
    onOpenMonitorTab: (Int) -> Unit,
    onOpenGames: () -> Unit,
    onOpenProfiles: () -> Unit,
    onOpenTools: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val snapshot = state.snapshot

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.TopCenter
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                DashboardHeroCard(
                    state = state,
                    onRefresh = onRefresh,
                    onQuickOptimize = onQuickOptimize,
                    onToggleFocusMode = onToggleFocusMode,
                    onOpenProfiles = onOpenProfiles
                )
            }

            if (snapshot != null) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CyberCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onOpenMonitorTab(0) }
                                .testTag("dashboard_ram_card"),
                            accentColor = NeonCyan
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("RAM", style = MaterialTheme.typography.titleMedium)
                                Icon(
                                    Icons.Filled.Memory,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center
                            ) {
                                RadialTelemetryGauge(
                                    progress = snapshot.ram.usedPercent / 100f,
                                    centerLabel = "${snapshot.ram.usedPercent.toInt()}%",
                                    centerSubtitle = "uso",
                                    size = 86.dp,
                                    strokeWidth = 8.dp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${snapshot.ram.availableMb} MB libres",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonCyan,
                                maxLines = 1
                            )
                            Text(
                                text = "de ${snapshot.ram.totalMb} MB",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        val thermalColor = when (snapshot.temperature.level) {
                            ThermalLevel.NORMAL -> StatusOptimal
                            ThermalLevel.ELEVATED -> StatusElevated
                            ThermalLevel.HIGH -> StatusHigh
                            ThermalLevel.UNAVAILABLE -> StatusInfo
                        }
                        CyberCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onOpenMonitorTab(2) }
                                .testTag("dashboard_thermal_battery_card"),
                            accentColor = thermalColor
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Térmico", style = MaterialTheme.typography.titleMedium)
                                StatusPill(
                                    label = snapshot.temperature.level.label,
                                    color = thermalColor
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.Thermostat,
                                    contentDescription = null,
                                    tint = thermalColor,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = snapshot.temperature.batteryTempC?.let { "${"%.1f".format(it)} °C" }
                                        ?: "N/D",
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        fontFamily = JetBrainsMonoFamily
                                    ),
                                    color = thermalColor
                                )
                            }
                            snapshot.temperature.thermalHeadroomPercent?.let { margin ->
                                Text(
                                    text = "Margen térmico: $margin%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = StatusInfo
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    Icons.Filled.BatteryChargingFull,
                                    contentDescription = null,
                                    tint = StatusOptimal,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "${snapshot.battery.levelPercent}% · ${snapshot.battery.statusLabel}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.onBackground,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                    }
                }

                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(IntrinsicSize.Max),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        CyberCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onOpenMonitorTab(1) }
                                .testTag("dashboard_cpu_card"),
                            accentColor = ElectricRoyalBlue
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("CPU", style = MaterialTheme.typography.titleMedium)
                                Icon(
                                    Icons.Filled.DeveloperBoard,
                                    contentDescription = null,
                                    tint = ElectricRoyalBlue,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "${snapshot.cpu.coreCount} núcleos",
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFamily
                                ),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = snapshot.cpu.architecture,
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = snapshot.cpu.processorLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        CyberCard(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .clickable { onOpenMonitorTab(4) }
                                .testTag("dashboard_storage_card"),
                            accentColor = StatusInfo
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Espacio", style = MaterialTheme.typography.titleMedium)
                                Icon(
                                    Icons.Filled.SdStorage,
                                    contentDescription = null,
                                    tint = StatusInfo,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = Formatter.formatShortFileSize(context, snapshot.storage.availableBytes),
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontFamily = JetBrainsMonoFamily
                                ),
                                color = StatusInfo
                            )
                            Text(
                                text = "de ${Formatter.formatShortFileSize(context, snapshot.storage.totalBytes)}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { snapshot.storage.usedPercent / 100f },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = StatusInfo,
                                trackColor = RoyalBorder,
                                strokeCap = StrokeCap.Round
                            )
                        }
                    }
                }

                item {
                    CyberCard(
                        modifier = Modifier.clickable(onClick = onTestPing),
                        accentColor = NeonCyan
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    Icons.Filled.NetworkCheck,
                                    contentDescription = null,
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "${snapshot.connectivity.transportLabel} · ${snapshot.display.refreshRateHz.toInt()} Hz",
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                    Text(
                                        text = when {
                                            state.isTestingPing -> "Midiendo rutas TCP..."
                                            state.latestPing != null -> "Ping ${state.latestPing.avgMs} ms · Jitter ${state.latestPing.jitterMs} ms (${state.latestPing.host})"
                                            else -> "Toca para medir latencia y jitter"
                                        },
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            StatusPill(
                                label = state.latestPing?.let { "${it.avgMs} ms" } ?: "Medir",
                                color = when {
                                    state.latestPing == null -> StatusInfo
                                    state.latestPing.avgMs <= 45 -> StatusOptimal
                                    state.latestPing.avgMs <= 95 -> StatusElevated
                                    else -> StatusHigh
                                }
                            )
                        }
                    }
                }

                item {
                    CyberCard(accentColor = NeonCyan) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                Icons.Filled.SportsEsports,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(26.dp)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "${state.registeredGamesCount} juegos registrados",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Text(
                                    text = snapshot.optimizationStatusLabel,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            Button(
                                onClick = onOpenGames,
                                modifier = Modifier.testTag("open_booster_quick"),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = ObsidianDeep
                                )
                            ) {
                                Text("Juegos", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                item {
                    CyberCard(accentColor = ElectricRoyalBlue) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Tendencia RAM", style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "${state.preferences.samplingIntervalMs / 1000.0}s",
                                style = MaterialTheme.typography.labelMedium,
                                color = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TelemetryLineChart(
                            values = state.recentSamples.map { it.ramUsedPercent },
                            maxValue = 100f,
                            minValue = 0f,
                            lineColor = NeonCyan,
                            unitSuffix = "%"
                        )
                    }
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickToolTile(
                            icon = Icons.Filled.Brightness6,
                            label = "Brillo",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                SecurityValidator.safeStartActivity(
                                    context,
                                    Intent(Settings.ACTION_DISPLAY_SETTINGS)
                                )
                            }
                        )
                        QuickToolTile(
                            icon = Icons.AutoMirrored.Filled.VolumeUp,
                            label = "Sonido",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                SecurityValidator.safeStartActivity(
                                    context,
                                    Intent(Settings.ACTION_SOUND_SETTINGS)
                                )
                            }
                        )
                        QuickToolTile(
                            icon = Icons.Filled.BatteryChargingFull,
                            label = "Batería",
                            modifier = Modifier.weight(1f),
                            onClick = {
                                SecurityValidator.safeStartActivity(
                                    context,
                                    Intent(Intent.ACTION_POWER_USAGE_SUMMARY),
                                    fallbackIntent = Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                                )
                            }
                        )
                        QuickToolTile(
                            icon = Icons.Filled.Tune,
                            label = "Tools",
                            modifier = Modifier.weight(1f),
                            onClick = onOpenTools
                        )
                    }
                }
            }
        }
    }

    state.lastOptimization?.let { res ->
        AlertDialog(
            onDismissRequest = onDismissOptimization,
            title = { Text("Optimización completada") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    MetricKeyValueRow("Índice Gaming", "${res.readinessScore}/100", NeonCyan)
                    MetricKeyValueRow("RAM libre", "${res.ramBeforeMb} -> ${res.ramAfterMb} MB", StatusOptimal)
                    val freedLabel = if (res.freedTempBytes > 0L) {
                        "${res.tempFilesCleared} (${Formatter.formatShortFileSize(context, res.freedTempBytes)})"
                    } else {
                        "${res.tempFilesCleared} archivos"
                    }
                    MetricKeyValueRow("Temp. limpiados", freedLabel)
                    MetricKeyValueRow("DND sistema", if (res.dndApplied) "Activo" else "Sin cambio")
                    MetricKeyValueRow("Brillo sistema", if (res.systemBrightnessApplied) "Aplicado" else "Modo ventana")
                    MetricKeyValueRow("Refresco objetivo", "${res.appliedRefreshHz} Hz")
                    res.mediaVolumeAppliedPercent?.let {
                        MetricKeyValueRow("Volumen gaming", "$it%")
                    }
                    res.thermalHeadroomPercent?.let {
                        MetricKeyValueRow("Margen térmico", "$it%", NeonCyan)
                    }
                    if (res.isPowerSaveModeActive) {
                        MetricKeyValueRow("Ahorro energía", "Activo (limita CPU/Hz)", StatusHigh)
                    }
                    res.pingResult?.let {
                        MetricKeyValueRow("Ping (${it.host})", "${it.avgMs} ms · ${it.qualityLabel}", StatusOptimal)
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = onDismissOptimization) { Text("Listo") }
            },
            dismissButton = if (res.isPowerSaveModeActive) {
                {
                    TextButton(onClick = {
                        SecurityValidator.safeStartActivity(
                            context,
                            Intent(Settings.ACTION_BATTERY_SAVER_SETTINGS)
                        )
                    }) { Text("Ajustar ahorro") }
                }
            } else null
        )
    }
}

@Composable
private fun DashboardHeroCard(
    state: DashboardUiState,
    onRefresh: () -> Unit,
    onQuickOptimize: () -> Unit,
    onToggleFocusMode: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    val snapshot = state.snapshot
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(ObsidianDeep)
    ) {
        Image(
            painter = painterResource(id = R.drawable.img_hero_banner),
            contentDescription = stringResource(R.string.cd_hero_banner),
            contentScale = ContentScale.Crop,
            modifier = Modifier.matchParentSize(),
            alpha = 0.65f
        )
        Box(
            modifier = Modifier
                .matchParentSize()
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            ObsidianDeep.copy(alpha = 0.94f),
                            ObsidianDeep.copy(alpha = 0.72f),
                            Color.Transparent
                        )
                    )
                )
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "GAME TURBO AI",
                        style = MaterialTheme.typography.headlineSmall,
                        color = NeonCyan
                    )
                    Text(
                        text = if (snapshot != null) {
                            "${snapshot.manufacturer} ${snapshot.model} · API ${snapshot.sdkInt}"
                        } else {
                            "Iniciando lectura..."
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                StatusPill(
                    label = snapshot?.overallHealthLabel ?: "Activo",
                    color = if (snapshot?.ram?.lowMemory == true) StatusHigh else StatusOptimal
                )
                IconButton(
                    onClick = onRefresh,
                    modifier = Modifier.testTag("refresh_telemetry_button")
                ) {
                    Icon(
                        imageVector = Icons.Filled.Refresh,
                        contentDescription = stringResource(R.string.cd_refresh_telemetry),
                        tint = NeonCyan
                    )
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onQuickOptimize,
                    enabled = !state.isOptimizing,
                    modifier = Modifier.testTag("quick_optimize_button"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = ObsidianDeep
                    )
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.isOptimizing) "Optimizando..." else "Optimizar",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                FilledTonalButton(
                    onClick = onToggleFocusMode,
                    modifier = Modifier.testTag("quick_focus_toggle"),
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                    colors = ButtonDefaults.filledTonalButtonColors(
                        containerColor = if (state.preferences.focusModeEnabled) {
                            NeonCyan.copy(alpha = 0.22f)
                        } else {
                            ElectricRoyalBlue.copy(alpha = 0.28f)
                        },
                        contentColor = if (state.preferences.focusModeEnabled) NeonCyan else MaterialTheme.colorScheme.onBackground
                    )
                ) {
                    Icon(
                        Icons.Filled.DoNotDisturbOn,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (state.preferences.focusModeEnabled) "DND ON" else "DND",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
                state.activeProfile?.let { profile ->
                    StatusPill(
                        label = profile.name,
                        color = StatusInfo,
                        modifier = Modifier.clickable(onClick = onOpenProfiles)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickToolTile(
    icon: ImageVector,
    label: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    CyberCard(
        modifier = modifier.clickable(onClick = onClick),
        accentColor = ElectricRoyalBlue
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = NeonCyan,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
