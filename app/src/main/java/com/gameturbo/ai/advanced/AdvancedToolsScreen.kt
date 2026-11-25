package com.gameturbo.ai.advanced

import android.content.Intent
import android.net.Uri
import android.provider.Settings
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.gameturbo.ai.data.repository.AdvancedTier
import com.gameturbo.ai.security.SecurityValidator
import com.gameturbo.ai.settings.GamingToolsContent
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.ElectricRoyalBlue
import com.gameturbo.ai.ui.theme.JetBrainsMonoFamily
import com.gameturbo.ai.ui.theme.MetricKeyValueRow
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.ObsidianDeep
import com.gameturbo.ai.ui.theme.RoyalSurface
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusHigh
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun AdvancedToolsScreen(
    state: AdvancedUiState,
    initialSubTab: Int = 0,
    onSelectTier: (AdvancedTier) -> Unit,
    onRefreshCapabilities: () -> Unit,
    onLogShortcut: (String) -> Unit,
    onClearAuditLogs: () -> Unit,
    onOpenPerformanceHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember(initialSubTab) { mutableIntStateOf(initialSubTab) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp)
        ) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = RoyalSurface,
                contentColor = NeonCyan
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Herramientas") },
                    icon = { Icon(Icons.Filled.Build, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Avanzado") },
                    icon = { Icon(Icons.Filled.AdminPanelSettings, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                GamingToolsContent(
                    snapshot = state.snapshot,
                    capabilities = state.capabilities,
                    onLogShortcut = onLogShortcut,
                    onOpenPerformanceHistory = onOpenPerformanceHistory
                )
            } else {
                AdvancedModeContent(
                    state = state,
                    onSelectTier = onSelectTier,
                    onRefreshCapabilities = onRefreshCapabilities,
                    onClearAuditLogs = onClearAuditLogs
                )
            }
        }
    }
}

@Composable
private fun AdvancedModeContent(
    state: AdvancedUiState,
    onSelectTier: (AdvancedTier) -> Unit,
    onRefreshCapabilities: () -> Unit,
    onClearAuditLogs: () -> Unit
) {
    val context = LocalContext.current
    val caps = state.capabilities
    val timeFormatter = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }
    var pendingSensitiveAction by remember { mutableStateOf<Pair<String, () -> Unit>?>(null) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    AdvancedTier.entries.forEach { tier ->
                        FilterChip(
                            selected = state.selectedTier == tier,
                            onClick = { onSelectTier(tier) },
                            modifier = Modifier.testTag("tier_${tier.name.lowercase()}"),
                            label = { Text(tier.title.removePrefix("Modo ")) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonCyan,
                                selectedLabelColor = ObsidianDeep
                            )
                        )
                    }
                }
                IconButton(onClick = onRefreshCapabilities) {
                    Icon(Icons.Filled.Refresh, contentDescription = "Reverificar", tint = NeonCyan)
                }
            }
        }

        item {
            when (state.selectedTier) {
                AdvancedTier.NORMAL -> {
                    CyberCard(accentColor = StatusOptimal) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo Normal", style = MaterialTheme.typography.titleLarge)
                            StatusPill(label = "Sin Root", color = StatusOptimal)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        MetricKeyValueRow(
                            "Permiso Brillo",
                            if (caps.canWriteSystemSettings) "OK" else "Pendiente",
                            valueColor = if (caps.canWriteSystemSettings) StatusOptimal else StatusElevated
                        )
                        MetricKeyValueRow(
                            "Permiso DND",
                            if (caps.hasDndPolicyAccess) "OK" else "Pendiente",
                            valueColor = if (caps.hasDndPolicyAccess) StatusOptimal else StatusElevated
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = {
                                    pendingSensitiveAction = "Autorizar escritura de brillo" to {
                                        SecurityValidator.safeStartActivity(
                                            context,
                                            Intent(
                                                Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                                Uri.parse("package:${context.packageName}")
                                            )
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("Brillo")
                            }
                            OutlinedButton(
                                onClick = {
                                    pendingSensitiveAction = "Autorizar acceso No Molestar" to {
                                        SecurityValidator.safeStartActivity(
                                            context,
                                            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                        )
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("DND")
                            }
                        }
                    }
                }

                AdvancedTier.SHIZUKU -> {
                    CyberCard(accentColor = if (caps.isShizukuInstalled) StatusOptimal else StatusElevated) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo Shizuku", style = MaterialTheme.typography.titleLarge)
                            StatusPill(
                                label = if (caps.isShizukuInstalled) "Detectado" else "No instalado",
                                color = if (caps.isShizukuInstalled) StatusOptimal else StatusElevated
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• dumpsys térmico y de sistema\n• Inspección de paquetes autorizada por el propietario",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                AdvancedTier.ADB -> {
                    CyberCard(accentColor = if (caps.isAdbEnabledInSystem) NeonCyan else StatusElevated) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo ADB", style = MaterialTheme.typography.titleLarge)
                            StatusPill(
                                label = if (caps.isAdbEnabledInSystem) "Activo" else "Inactivo",
                                color = if (caps.isAdbEnabledInSystem) StatusOptimal else StatusElevated
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "adb shell dumpsys battery\nadb shell dumpsys thermalservice\nadb shell dumpsys meminfo",
                            style = MaterialTheme.typography.labelMedium.copy(fontFamily = JetBrainsMonoFamily),
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        OutlinedButton(
                            onClick = {
                                pendingSensitiveAction = "Abrir Opciones de Desarrollador" to {
                                    SecurityValidator.safeStartActivity(
                                        context,
                                        Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS),
                                        fallbackIntent = Intent(Settings.ACTION_DEVICE_INFO_SETTINGS)
                                    )
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Filled.Terminal, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Opciones de Desarrollador")
                        }
                    }
                }

                AdvancedTier.ROOT -> {
                    CyberCard(accentColor = if (caps.suBinaryPresent) StatusElevated else StatusHigh) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Modo Root", style = MaterialTheme.typography.titleLarge)
                            StatusPill(
                                label = if (caps.suBinaryPresent) "su detectado" else "Sin Root",
                                color = if (caps.suBinaryPresent) StatusElevated else StatusHigh
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Comprobación pasiva. Sin elevación automática ni puertas traseras.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Auditoría (${state.auditLogs.size})", style = MaterialTheme.typography.titleMedium)
                if (state.auditLogs.isNotEmpty()) {
                    TextButton(onClick = {
                        pendingSensitiveAction = "Limpiar auditoría" to onClearAuditLogs
                    }) {
                        Icon(Icons.Filled.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Limpiar")
                    }
                }
            }
        }

        items(state.auditLogs.take(15), key = { it.id }) { log ->
            CyberCard(accentColor = ElectricRoyalBlue) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = log.action, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = timeFormatter.format(Date(log.timestamp)),
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonCyan
                    )
                }
                Text(
                    text = log.detail,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    pendingSensitiveAction?.let { (label, action) ->
        AlertDialog(
            onDismissRequest = { pendingSensitiveAction = null },
            title = { Text("Confirmar") },
            text = { Text("¿Ejecutar: $label?") },
            confirmButton = {
                Button(
                    onClick = {
                        pendingSensitiveAction = null
                        action()
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = ObsidianDeep
                    )
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingSensitiveAction = null }) { Text("Cancelar") }
            }
        )
    }
}
