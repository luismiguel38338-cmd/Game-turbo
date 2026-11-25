package com.gameturbo.ai.storage

import android.text.format.Formatter
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SdStorage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.gameturbo.ai.data.repository.AppStorageInspection
import com.gameturbo.ai.performance.StorageOverview
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.MetricKeyValueRow
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.RoyalBorder
import com.gameturbo.ai.ui.theme.SectionHeader
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusHigh
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill

@Composable
fun StorageManagerContent(
    storage: StorageOverview?,
    inspection: AppStorageInspection,
    selectedPaths: Set<String>,
    lastMessage: String?,
    onRefresh: () -> Unit,
    onToggleSelect: (String) -> Unit,
    onSelectTempFiles: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteConfirmed: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showConfirmDeleteDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (storage != null) {
            item {
                CyberCard(accentColor = StatusInfo) {
                    SectionHeader(
                        title = "Almacenamiento del Dispositivo",
                        subtitle = "Consulta oficial StatFs sin acceso a carpetas privadas de terceros",
                        icon = Icons.Filled.SdStorage,
                        accentColor = StatusInfo
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    MetricKeyValueRow(
                        "Espacio total",
                        Formatter.formatFileSize(context, storage.totalBytes)
                    )
                    MetricKeyValueRow(
                        "Espacio disponible",
                        Formatter.formatFileSize(context, storage.availableBytes),
                        valueColor = StatusOptimal
                    )
                    MetricKeyValueRow(
                        "Espacio utilizado",
                        "${Formatter.formatFileSize(context, storage.usedBytes)} (${"%.1f".format(storage.usedPercent)}%)",
                        valueColor = NeonCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { storage.usedPercent / 100f },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = StatusInfo,
                        trackColor = RoyalBorder,
                        strokeCap = StrokeCap.Round
                    )
                }
            }
        }

        item {
            CyberCard(accentColor = NeonCyan) {
                SectionHeader(
                    title = "Archivos Propios de la Aplicación",
                    subtitle = "Caché y temporales internos analizables con seguridad",
                    icon = Icons.Filled.DeleteOutline,
                    accentColor = NeonCyan
                )
                Spacer(modifier = Modifier.height(10.dp))
                MetricKeyValueRow(
                    "Total archivos propios",
                    Formatter.formatFileSize(context, inspection.totalAppFilesBytes)
                )
                MetricKeyValueRow(
                    "Temporales / caché detectados",
                    Formatter.formatFileSize(context, inspection.totalTempCacheBytes),
                    valueColor = StatusElevated
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onRefresh,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Analizar")
                    }
                    OutlinedButton(
                        onClick = if (selectedPaths.isEmpty()) onSelectTempFiles else onClearSelection,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (selectedPaths.isEmpty()) "Marcar temp." else "Desmarcar")
                    }
                }
                if (selectedPaths.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { showConfirmDeleteDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("delete_selected_storage_button"),
                        colors = ButtonDefaults.buttonColors(containerColor = StatusHigh)
                    ) {
                        Icon(Icons.Filled.DeleteOutline, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Eliminar ${selectedPaths.size} seleccionados")
                    }
                }
                lastMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = msg,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusOptimal
                    )
                }
            }
        }

        item {
            CyberCard(accentColor = StatusInfo) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Filled.Info, contentDescription = null, tint = StatusInfo)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Protección de privacidad: No se eliminan archivos personales automáticamente ni se accede a carpetas protegidas del sistema.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (inspection.files.isEmpty()) {
            item {
                CyberCard(accentColor = StatusOptimal) {
                    Text(
                        text = "No hay archivos temporales pendientes en el directorio propio de Game Turbo AI.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            items(inspection.files, key = { it.path }) { item ->
                val checked = selectedPaths.contains(item.path)
                CyberCard(
                    modifier = Modifier.clickable { onToggleSelect(item.path) },
                    accentColor = if (item.isTemporary) StatusElevated else StatusInfo
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = checked,
                            onCheckedChange = { onToggleSelect(item.path) }
                        )
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = item.name,
                                style = MaterialTheme.typography.titleMedium,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = "${item.category} · ${Formatter.formatShortFileSize(context, item.sizeBytes)}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (item.isTemporary) {
                            StatusPill(label = "Temporal", color = StatusElevated)
                        }
                    }
                }
            }
        }
    }

    if (showConfirmDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDeleteDialog = false },
            title = { Text("Confirmar eliminación") },
            text = {
                Text("¿Deseas eliminar permanentemente los ${selectedPaths.size} archivos propios seleccionados de Game Turbo AI? Esta acción no afecta archivos personales fuera de la app.")
            },
            confirmButton = {
                Button(
                    onClick = {
                        showConfirmDeleteDialog = false
                        onDeleteConfirmed()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusHigh)
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDeleteDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}
