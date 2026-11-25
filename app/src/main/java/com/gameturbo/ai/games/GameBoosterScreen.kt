package com.gameturbo.ai.games

import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.provider.Settings
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DisplaySettings
import androidx.compose.material.icons.filled.DoNotDisturbOn
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.R
import com.gameturbo.ai.data.db.GameSessionEntity
import com.gameturbo.ai.data.db.GamingProfileEntity
import com.gameturbo.ai.data.db.RegisteredGameEntity
import com.gameturbo.ai.data.repository.LaunchableAppItem
import com.gameturbo.ai.security.SecurityValidator
import com.gameturbo.ai.ui.theme.CyberCard
import com.gameturbo.ai.ui.theme.ElectricRoyalBlue
import com.gameturbo.ai.ui.theme.NeonCyan
import com.gameturbo.ai.ui.theme.ObsidianDeep
import com.gameturbo.ai.ui.theme.RoyalSurface
import com.gameturbo.ai.ui.theme.StatusElevated
import com.gameturbo.ai.ui.theme.StatusInfo
import com.gameturbo.ai.ui.theme.StatusOptimal
import com.gameturbo.ai.ui.theme.StatusPill
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun GameBoosterScreen(
    state: GameBoosterUiState,
    onScanGames: () -> Unit,
    onRegisterApp: (LaunchableAppItem, Long?, Boolean, Boolean) -> Unit,
    onUpdateGame: (RegisteredGameEntity) -> Unit,
    onRemoveGame: (String) -> Unit,
    onLaunchGame: (Context, GameCardModel) -> Unit,
    onToggleFocusMode: () -> Unit,
    onClearSessions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var showAddAppDialog by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf(false) }
    var editingGame by remember { mutableStateOf<GameCardModel?>(null) }

    val nm = remember(context) {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }
    val hasDndPermission = nm?.isNotificationPolicyAccessGranted == true

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
                    text = { Text("Biblioteca (${state.registeredGames.size})") },
                    icon = { Icon(Icons.Filled.SportsEsports, contentDescription = null) }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Sesiones (${state.sessions.size})") },
                    icon = { Icon(Icons.Filled.History, contentDescription = null) }
                )
            }

            if (selectedTab == 0) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        CyberCard(accentColor = NeonCyan) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Game Booster", style = MaterialTheme.typography.titleLarge)
                                IconButton(onClick = { showInfoDialog = true }) {
                                    Icon(
                                        Icons.Filled.Info,
                                        contentDescription = "Información del Booster",
                                        tint = StatusInfo
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = onScanGames,
                                    enabled = !state.isScanning,
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("scan_games_button"),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = NeonCyan,
                                        contentColor = ObsidianDeep
                                    )
                                ) {
                                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(if (state.isScanning) "Escaneando" else "Detectar")
                                }
                                OutlinedButton(
                                    onClick = { showAddAppDialog = true },
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("add_game_button")
                                ) {
                                    Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Añadir")
                                }
                            }
                        }
                    }

                    item {
                        CyberCard(accentColor = ElectricRoyalBlue) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(
                                        Icons.Filled.DoNotDisturbOn,
                                        contentDescription = null,
                                        tint = if (state.focusModeEnabled) NeonCyan else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column {
                                        Text("Modo Concentración", style = MaterialTheme.typography.titleMedium)
                                        Text(
                                            text = if (hasDndPermission) "DND autorizado" else "Solo en-app",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                                Switch(
                                    checked = state.focusModeEnabled,
                                    onCheckedChange = { onToggleFocusMode() },
                                    modifier = Modifier.testTag("booster_focus_switch")
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        SecurityValidator.safeStartActivity(
                                            context,
                                            Intent(Settings.ACTION_NOTIFICATION_POLICY_ACCESS_SETTINGS)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("Permiso DND", maxLines = 1)
                                }
                                OutlinedButton(
                                    onClick = {
                                        SecurityValidator.safeStartActivity(
                                            context,
                                            Intent(Settings.ACTION_DISPLAY_SETTINGS)
                                        )
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Filled.DisplaySettings, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Pantalla")
                                }
                            }
                        }
                    }

                    if (state.registeredGames.isEmpty()) {
                        item {
                            CyberCard(accentColor = NeonCyan) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 18.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        Icons.Filled.SportsEsports,
                                        contentDescription = null,
                                        tint = NeonCyan,
                                        modifier = Modifier.size(42.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text("Sin juegos añadidos", style = MaterialTheme.typography.titleMedium)
                                    Text(
                                        text = "Usa Detectar o Añadir para configurar tus juegos.",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    } else {
                        items(state.registeredGames, key = { it.entity.packageName }) { game ->
                            GameItemCard(
                                model = game,
                                onLaunch = { onLaunchGame(context, game) },
                                onEdit = { editingGame = game },
                                onDelete = { onRemoveGame(game.entity.packageName) }
                            )
                        }
                    }
                }
            } else {
                SessionHistoryTab(
                    sessions = state.sessions,
                    onClearSessions = onClearSessions
                )
            }
        }
    }

    if (showInfoDialog) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = false },
            title = { Text("Sobre Game Booster") },
            text = {
                Text("Game Turbo AI organiza tus juegos, registra métricas al iniciar sesión y aplica ajustes permitidos por Android. Ninguna aplicación estándar puede incrementar artificialmente los FPS.")
            },
            confirmButton = {
                TextButton(onClick = { showInfoDialog = false }) { Text("Entendido") }
            }
        )
    }

    if (showAddAppDialog) {
        AddGamePickerDialog(
            apps = state.installedLauncherApps,
            profiles = state.profiles,
            onDismiss = { showAddAppDialog = false },
            onConfirm = { app, profileId, focus, keepOn ->
                onRegisterApp(app, profileId, focus, keepOn)
                showAddAppDialog = false
            }
        )
    }

    editingGame?.let { target ->
        EditGameConfigDialog(
            model = target,
            profiles = state.profiles,
            onDismiss = { editingGame = null },
            onSave = { updated ->
                onUpdateGame(updated)
                editingGame = null
            }
        )
    }
}

@Composable
private fun GameItemCard(
    model: GameCardModel,
    onLaunch: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    CyberCard(accentColor = if (model.isInstalled) NeonCyan else StatusElevated) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppDrawableIcon(
                drawable = model.icon,
                modifier = Modifier.size(44.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = model.entity.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    StatusPill(label = model.assignedProfileName, color = StatusInfo)
                    if (model.entity.focusModeEnabled) {
                        StatusPill(label = "DND", color = StatusOptimal)
                    }
                }
            }
            IconButton(onClick = onEdit) {
                Icon(Icons.Filled.Settings, contentDescription = "Configurar perfil del juego", tint = StatusInfo)
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Filled.Delete, contentDescription = "Eliminar de biblioteca", tint = StatusElevated)
            }
        }
        Spacer(modifier = Modifier.height(8.dp))
        Button(
            onClick = onLaunch,
            enabled = model.isInstalled,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("launch_game_${model.entity.packageName}"),
            colors = ButtonDefaults.buttonColors(
                containerColor = NeonCyan,
                contentColor = ObsidianDeep
            )
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = stringResource(R.string.cd_launch_game))
            Spacer(modifier = Modifier.width(4.dp))
            Text(if (model.isInstalled) "Abrir" else "No instalado")
        }
    }
}

@Composable
private fun SessionHistoryTab(
    sessions: List<GameSessionEntity>,
    onClearSessions: () -> Unit
) {
    val formatter = remember { SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (sessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Sesiones recientes", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = onClearSessions) { Text("Limpiar") }
                }
            }
            items(sessions, key = { it.id }) { s ->
                CyberCard(accentColor = ElectricRoyalBlue) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = s.gameName, style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = formatter.format(Date(s.startedAt)),
                            style = MaterialTheme.typography.labelMedium,
                            color = NeonCyan
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${s.profileName} · RAM ${s.ramPercentAtStart}% · Bat ${s.batteryPercentAtStart}% · ${
                            if (s.tempCelsiusAtStart >= 0f) "${"%.1f".format(s.tempCelsiusAtStart)}°C" else "N/D"
                        }",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            item {
                CyberCard(accentColor = ElectricRoyalBlue) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.History,
                            contentDescription = null,
                            tint = StatusInfo,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Sin sesiones registradas", style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun AddGamePickerDialog(
    apps: List<LaunchableAppItem>,
    profiles: List<GamingProfileEntity>,
    onDismiss: () -> Unit,
    onConfirm: (LaunchableAppItem, Long?, Boolean, Boolean) -> Unit
) {
    var query by remember { mutableStateOf("") }
    var selectedApp by remember { mutableStateOf<LaunchableAppItem?>(null) }
    var selectedProfileId by remember { mutableStateOf(profiles.firstOrNull()?.id) }
    var focusMode by remember { mutableStateOf(true) }
    var keepScreenOn by remember { mutableStateOf(true) }

    val filtered = remember(apps, query) {
        if (query.isBlank()) apps else apps.filter {
            it.label.contains(query, ignoreCase = true) || it.packageName.contains(query, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Añadir aplicación") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text("Buscar app") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(190.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered, key = { it.packageName }) { app ->
                        val isSelected = selectedApp?.packageName == app.packageName
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    if (isSelected) NeonCyan.copy(alpha = 0.18f)
                                    else RoyalSurface
                                )
                                .clickable { selectedApp = app }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AppDrawableIcon(drawable = app.icon, modifier = Modifier.size(30.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            if (app.isCategoryGame) {
                                StatusPill(label = "GAME", color = StatusOptimal)
                            }
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Concentración", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = focusMode, onCheckedChange = { focusMode = it })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Pantalla activa", style = MaterialTheme.typography.bodyMedium)
                    Switch(checked = keepScreenOn, onCheckedChange = { keepScreenOn = it })
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    selectedApp?.let { onConfirm(it, selectedProfileId, focusMode, keepScreenOn) }
                },
                enabled = selectedApp != null
            ) {
                Text("Guardar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
private fun EditGameConfigDialog(
    model: GameCardModel,
    profiles: List<GamingProfileEntity>,
    onDismiss: () -> Unit,
    onSave: (RegisteredGameEntity) -> Unit
) {
    var assignedProfileId by remember { mutableStateOf(model.entity.assignedProfileId) }
    var focusMode by remember { mutableStateOf(model.entity.focusModeEnabled) }
    var keepScreenOn by remember { mutableStateOf(model.entity.keepScreenOn) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(model.entity.displayName) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                profiles.forEach { p ->
                    FilterChip(
                        selected = assignedProfileId == p.id,
                        onClick = { assignedProfileId = p.id },
                        label = { Text(p.name) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Concentración al abrir")
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
            }
        },
        confirmButton = {
            Button(onClick = {
                onSave(
                    model.entity.copy(
                        assignedProfileId = assignedProfileId,
                        focusModeEnabled = focusMode,
                        keepScreenOn = keepScreenOn
                    )
                )
            }) {
                Text("Aplicar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

@Composable
fun AppDrawableIcon(
    drawable: Drawable?,
    modifier: Modifier = Modifier
) {
    val bitmap = remember(drawable) {
        drawable?.let { d ->
            if (d is BitmapDrawable && d.bitmap != null) {
                d.bitmap
            } else {
                val width = d.intrinsicWidth.coerceAtLeast(64)
                val height = d.intrinsicHeight.coerceAtLeast(64)
                Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { bmp ->
                    val canvas = Canvas(bmp)
                    d.setBounds(0, 0, canvas.width, canvas.height)
                    d.draw(canvas)
                }
            }
        }
    }

    if (bitmap != null) {
        Image(
            bitmap = bitmap.asImageBitmap(),
            contentDescription = stringResource(R.string.cd_game_icon),
            modifier = modifier.clip(RoundedCornerShape(12.dp))
        )
    } else {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(12.dp))
                .background(ElectricRoyalBlue.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.SportsEsports,
                contentDescription = stringResource(R.string.cd_game_icon),
                tint = NeonCyan
            )
        }
    }
}
