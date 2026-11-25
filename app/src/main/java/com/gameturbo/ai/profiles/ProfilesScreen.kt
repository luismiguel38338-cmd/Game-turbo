package com.gameturbo.ai.profiles

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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.R
import com.gameturbo.ai.data.db.GamingProfileEntity
import com.gameturbo.ai.security.SecurityValidator
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.ElectricRoyalBlue
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.ObsidianDeep
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill
import kotlin.math.roundToInt

@Composable
fun ProfilesScreen(
    state: ProfilesUiState,
    onActivateProfile: (GamingProfileEntity) -> Unit,
    onSaveProfile: (
        existingId: Long?,
        name: String,
        samplingIntervalMs: Long,
        focusMode: Boolean,
        keepScreenOn: Boolean,
        hapticFeedback: Boolean,
        brightnessPercent: Int,
        description: String
    ) -> Unit,
    onDeleteProfile: (GamingProfileEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val canWriteSystem = Settings.System.canWrite(context)

    var showEditorDialog by remember { mutableStateOf(false) }
    var editingProfile by remember { mutableStateOf<GamingProfileEntity?>(null) }
    var deletingProfile by remember { mutableStateOf<GamingProfileEntity?>(null) }

    Box(modifier = modifier.fillMaxSize(), contentAlignment = Alignment.TopCenter) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 860.dp),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Button(
                        onClick = {
                            editingProfile = null
                            showEditorDialog = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("create_profile_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = ObsidianDeep
                        )
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Nuevo perfil")
                    }
                    OutlinedButton(
                        onClick = {
                            SecurityValidator.safeStartActivity(
                                context,
                                Intent(
                                    Settings.ACTION_MANAGE_WRITE_SETTINGS,
                                    Uri.parse("package:${context.packageName}")
                                ),
                                fallbackIntent = Intent(Settings.ACTION_DISPLAY_SETTINGS)
                            )
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.Brightness6, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (canWriteSystem) "Brillo OK" else "Permiso brillo")
                    }
                }
            }

            items(state.profiles, key = { it.id }) { profile ->
                val isActive = state.preferences.activeProfileId == profile.id
                CyberCard(
                    accentColor = if (isActive) StatusOptimal else ElectricRoyalBlue,
                    elevated = isActive
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = profile.name,
                                style = MaterialTheme.typography.titleLarge,
                                color = if (isActive) NeonCyan else MaterialTheme.colorScheme.onBackground
                            )
                            Text(
                                text = "${profile.samplingIntervalMs / 1000.0}s · ${
                                    if (profile.focusMode) "DND ON" else "DND OFF"
                                } · ${
                                    if (profile.brightnessPercent < 0) "Brillo auto" else "Brillo ${profile.brightnessPercent}%"
                                }",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (isActive) {
                            StatusPill(label = "Activo", color = StatusOptimal)
                        } else {
                            StatusPill(
                                label = if (profile.isBuiltIn) "Base" else "Custom",
                                color = if (profile.isBuiltIn) StatusInfo else StatusElevated
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { onActivateProfile(profile) },
                            enabled = !isActive,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("activate_profile_${profile.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isActive) StatusOptimal else NeonCyan,
                                contentColor = ObsidianDeep
                            )
                        ) {
                            if (isActive) {
                                Icon(Icons.Filled.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("En uso")
                            } else {
                                Text("Activar")
                            }
                        }

                        IconButton(
                            onClick = {
                                editingProfile = profile
                                showEditorDialog = true
                            }
                        ) {
                            Icon(
                                Icons.Filled.Edit,
                                contentDescription = stringResource(R.string.cd_edit_profile),
                                tint = StatusInfo
                            )
                        }

                        if (!profile.isBuiltIn) {
                            IconButton(onClick = { deletingProfile = profile }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = stringResource(R.string.cd_delete_profile),
                                    tint = StatusElevated
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showEditorDialog) {
        ProfileEditorDialog(
            initial = editingProfile,
            onDismiss = { showEditorDialog = false },
            onConfirm = { name, intervalMs, focus, keepOn, haptic, brightness, desc ->
                onSaveProfile(
                    editingProfile?.id,
                    name,
                    intervalMs,
                    focus,
                    keepOn,
                    haptic,
                    brightness,
                    desc
                )
                showEditorDialog = false
            }
        )
    }

    deletingProfile?.let { target ->
        AlertDialog(
            onDismissRequest = { deletingProfile = null },
            title = { Text("Eliminar perfil") },
            text = { Text("¿Eliminar \"${target.name}\"?") },
            confirmButton = {
                Button(
                    onClick = {
                        onDeleteProfile(target)
                        deletingProfile = null
                    }
                ) {
                    Text("Eliminar")
                }
            },
            dismissButton = {
                TextButton(onClick = { deletingProfile = null }) { Text("Cancelar") }
            }
        )
    }
}

@Composable
private fun ProfileEditorDialog(
    initial: GamingProfileEntity?,
    onDismiss: () -> Unit,
    onConfirm: (
        name: String,
        samplingIntervalMs: Long,
        focusMode: Boolean,
        keepScreenOn: Boolean,
        hapticFeedback: Boolean,
        brightnessPercent: Int,
        description: String
    ) -> Unit
) {
    var name by remember { mutableStateOf(initial?.name ?: "Personalizado") }
    var description by remember {
        mutableStateOf(initial?.description ?: "Perfil personalizado.")
    }
    var intervalSec by remember {
        mutableFloatStateOf(((initial?.samplingIntervalMs ?: 2000L) / 1000f).coerceIn(1f, 10f))
    }
    var focusMode by remember { mutableStateOf(initial?.focusMode ?: true) }
    var keepScreenOn by remember { mutableStateOf(initial?.keepScreenOn ?: true) }
    var hapticFeedback by remember { mutableStateOf(initial?.hapticFeedback ?: true) }
    var useCustomBrightness by remember { mutableStateOf((initial?.brightnessPercent ?: -1) >= 10) }
    var brightnessPercent by remember {
        mutableFloatStateOf((initial?.brightnessPercent?.takeIf { it >= 10 } ?: 80).toFloat())
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (initial == null) "Crear perfil" else "Editar perfil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nombre") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Muestreo: ${intervalSec.roundToInt()}s",
                    style = MaterialTheme.typography.bodyMedium
                )
                Slider(
                    value = intervalSec,
                    onValueChange = { intervalSec = it },
                    valueRange = 1f..10f,
                    steps = 8
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Concentración")
                    Switch(checked = focusMode, onCheckedChange = { focusMode = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pantalla activa")
                    Switch(checked = keepScreenOn, onCheckedChange = { keepScreenOn = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Vibración háptica")
                    Switch(checked = hapticFeedback, onCheckedChange = { hapticFeedback = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Brillo en ventana")
                    Switch(checked = useCustomBrightness, onCheckedChange = { useCustomBrightness = it })
                }
                if (useCustomBrightness) {
                    Text(
                        text = "Brillo: ${brightnessPercent.roundToInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Slider(
                        value = brightnessPercent,
                        onValueChange = { brightnessPercent = it },
                        valueRange = 15f..100f
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(
                        name,
                        intervalSec.roundToInt() * 1000L,
                        focusMode,
                        keepScreenOn,
                        hapticFeedback,
                        if (useCustomBrightness) brightnessPercent.roundToInt() else -1,
                        description
                    )
                },
                enabled = name.isNotBlank()
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}
