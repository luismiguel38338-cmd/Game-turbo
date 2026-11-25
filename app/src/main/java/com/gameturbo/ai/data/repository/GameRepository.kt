package com.gameturbo.ai.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Build
import com.gameturbo.ai.data.db.GameDao
import com.gameturbo.ai.data.db.GameSessionEntity
import com.gameturbo.ai.data.db.RegisteredGameEntity
import com.gameturbo.ai.data.db.SecurityAuditDao
import com.gameturbo.ai.data.db.SecurityAuditEntity
import com.gameturbo.ai.security.SecurityValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class LaunchableAppItem(
    val packageName: String,
    val label: String,
    val isCategoryGame: Boolean,
    val icon: Drawable?
)

class GameRepository(
    private val context: Context,
    private val gameDao: GameDao,
    private val auditDao: SecurityAuditDao
) {
    val registeredGames: Flow<List<RegisteredGameEntity>> = gameDao.observeGames()
    val registeredGameCount: Flow<Int> = gameDao.observeGameCount()
    val sessions: Flow<List<GameSessionEntity>> = gameDao.observeSessions()

    suspend fun queryInstalledLauncherApps(): List<LaunchableAppItem> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val launcherIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            pm.queryIntentActivities(launcherIntent, PackageManager.ResolveInfoFlags.of(0L))
        } else {
            @Suppress("DEPRECATION")
            pm.queryIntentActivities(launcherIntent, 0)
        }

        val myPkg = context.packageName
        resolveInfos
            .asSequence()
            .mapNotNull { info ->
                val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                if (pkg == myPkg) return@mapNotNull null
                val appInfo = info.activityInfo.applicationInfo
                val isGame = (appInfo.category == ApplicationInfo.CATEGORY_GAME) ||
                    (@Suppress("DEPRECATION") (appInfo.flags and ApplicationInfo.FLAG_IS_GAME) != 0)
                val label = info.loadLabel(pm)?.toString().orEmpty().ifBlank { pkg }
                val icon = try {
                    info.loadIcon(pm)
                } catch (_: Exception) {
                    null
                }
                LaunchableAppItem(
                    packageName = pkg,
                    label = label,
                    isCategoryGame = isGame,
                    icon = icon
                )
            }
            .distinctBy { it.packageName }
            .sortedWith(compareByDescending<LaunchableAppItem> { it.isCategoryGame }.thenBy { it.label.lowercase() })
            .toList()
    }

    suspend fun syncDetectedGames(): Int = withContext(Dispatchers.IO) {
        val apps = queryInstalledLauncherApps().filter { it.isCategoryGame }
        apps.forEach { app ->
            gameDao.upsertGame(
                RegisteredGameEntity(
                    packageName = app.packageName,
                    displayName = app.label,
                    isAutoDetected = true
                )
            )
        }
        auditDao.insertLog(
            SecurityAuditEntity(
                category = "GAME_BOOSTER",
                action = "Escaneo de videojuegos",
                detail = "Se detectaron ${apps.size} videojuegos declarados con CATEGORY_GAME.",
                authorizedMode = "Normal"
            )
        )
        apps.size
    }

    suspend fun registerManualGame(
        packageName: String,
        displayName: String,
        profileId: Long?,
        focusMode: Boolean,
        keepScreenOn: Boolean
    ) = withContext(Dispatchers.IO) {
        val cleanName = SecurityValidator.sanitizeLabel(displayName).ifBlank { packageName }
        gameDao.upsertGame(
            RegisteredGameEntity(
                packageName = packageName.trim(),
                displayName = cleanName,
                isAutoDetected = false,
                assignedProfileId = profileId,
                focusModeEnabled = focusMode,
                keepScreenOn = keepScreenOn
            )
        )
        auditDao.insertLog(
            SecurityAuditEntity(
                category = "GAME_BOOSTER",
                action = "Juego registrado manualmente",
                detail = "$cleanName ($packageName)",
                authorizedMode = "Normal"
            )
        )
    }

    suspend fun updateGameConfig(game: RegisteredGameEntity) = withContext(Dispatchers.IO) {
        gameDao.upsertGame(game)
    }

    suspend fun removeGame(packageName: String) = withContext(Dispatchers.IO) {
        gameDao.deleteGame(packageName)
    }

    suspend fun recordSessionLaunch(
        game: RegisteredGameEntity,
        profileName: String,
        ramPercent: Int,
        batteryPercent: Int,
        tempC: Float
    ) = withContext(Dispatchers.IO) {
        gameDao.insertSession(
            GameSessionEntity(
                packageName = game.packageName,
                gameName = game.displayName,
                profileName = profileName,
                ramPercentAtStart = ramPercent,
                batteryPercentAtStart = batteryPercent,
                tempCelsiusAtStart = tempC
            )
        )
        auditDao.insertLog(
            SecurityAuditEntity(
                category = "GAME_BOOSTER",
                action = "Inicio de sesión gaming",
                detail = "${game.displayName} con perfil '$profileName'",
                authorizedMode = "Normal"
            )
        )
    }

    suspend fun clearSessions() = withContext(Dispatchers.IO) {
        gameDao.clearSessions()
    }

    fun getLaunchIntent(packageName: String): Intent? {
        return context.packageManager.getLaunchIntentForPackage(packageName)
    }

    fun loadAppIcon(packageName: String): Drawable? {
        return try {
            context.packageManager.getApplicationIcon(packageName)
        } catch (_: Exception) {
            null
        }
    }
}
