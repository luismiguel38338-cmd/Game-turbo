package com.gameturbo.ai.games

import android.app.GameManager
import android.app.NotificationManager
import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.provider.Settings
import com.gameturbo.ai.data.db.GamingProfileEntity
import com.gameturbo.ai.data.db.SecurityAuditDao
import com.gameturbo.ai.data.db.SecurityAuditEntity
import com.gameturbo.ai.data.repository.StorageRepository
import com.gameturbo.ai.performance.PingResult
import com.gameturbo.ai.performance.SystemTelemetryReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.math.roundToInt

data class OptimizationResult(
    val ramBeforeMb: Long,
    val ramAfterMb: Long,
    val tempFilesCleared: Int,
    val freedTempBytes: Long,
    val dndApplied: Boolean,
    val systemBrightnessApplied: Boolean,
    val mediaVolumeAppliedPercent: Int?,
    val appliedRefreshHz: Int,
    val sustainedModeSupported: Boolean,
    val isPowerSaveModeActive: Boolean,
    val readinessScore: Int,
    val thermalHeadroomPercent: Int?,
    val pingResult: PingResult?,
    val androidGameModeLabel: String
)

class GameOptimizerEngine(
    private val context: Context,
    private val telemetryReader: SystemTelemetryReader,
    private val storageRepository: StorageRepository,
    private val auditDao: SecurityAuditDao
) {
    suspend fun runOptimizationPipeline(
        targetPackage: String? = null,
        profile: GamingProfileEntity? = null,
        enableFocusMode: Boolean = profile?.focusMode == true,
        measurePing: Boolean = true
    ): OptimizationResult = withContext(Dispatchers.IO) {
        val ramBefore = telemetryReader.readRam().availableMb

        // 1. Clean app-owned temporary files only (safe sandboxed cleanup)
        val inspection = storageRepository.inspectAppOwnedStorage()
        val tempFiles = inspection.files.filter { it.isTemporary }
        val tempPaths = tempFiles.map { it.path }.toSet()
        val expectedFreedBytes = tempFiles.sumOf { it.sizeBytes }
        val clearedCount = if (tempPaths.isNotEmpty()) {
            storageRepository.deleteSelectedFiles(tempPaths)
        } else {
            0
        }
        val freedBytes = if (clearedCount > 0) expectedFreedBytes else 0L

        // Encourage JVM heap reclamation inside our own app process
        System.gc()

        // 2. Apply System Do Not Disturb if explicitly permitted by the user
        var dndApplied = false
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        if (nm != null && nm.isNotificationPolicyAccessGranted) {
            runCatching {
                nm.setInterruptionFilter(
                    if (enableFocusMode) NotificationManager.INTERRUPTION_FILTER_PRIORITY
                    else NotificationManager.INTERRUPTION_FILTER_ALL
                )
                dndApplied = enableFocusMode
            }
        }

        // 3. Apply System Screen Brightness if WRITE_SETTINGS is explicitly authorized
        var systemBrightnessApplied = false
        val targetBrightness = profile?.brightnessPercent ?: -1
        if (targetBrightness in 10..100 && Settings.System.canWrite(context)) {
            runCatching {
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL
                )
                val value255 = ((targetBrightness / 100f) * 255f).roundToInt().coerceIn(25, 255)
                Settings.System.putInt(
                    context.contentResolver,
                    Settings.System.SCREEN_BRIGHTNESS,
                    value255
                )
                systemBrightnessApplied = true
            }
        }

        // 4. Apply Media Volume preset via official AudioManager (no special permission required)
        var appliedVolume: Int? = null
        val targetVolume = profile?.mediaVolumePercent ?: -1
        if (targetVolume in 0..100) {
            val am = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (am != null) {
                runCatching {
                    val maxVol = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
                    val targetIdx = ((targetVolume / 100f) * maxVol).roundToInt().coerceIn(0, maxVol)
                    am.setStreamVolume(AudioManager.STREAM_MUSIC, targetIdx, 0)
                    appliedVolume = targetVolume
                }
            }
        }

        // 5. Inspect Android 12+ GameManager mode, PowerSave, Thermal Headroom & best TCP route
        val gameModeLabel = queryAndroidGameModeLabel(targetPackage)
        val snapshot = telemetryReader.captureSnapshot()
        val ping = if (measurePing) {
            val primary = telemetryReader.measureNetworkLatency("1.1.1.1", 443)
            val secondary = telemetryReader.measureNetworkLatency("8.8.8.8", 443)
            listOfNotNull(primary, secondary).minByOrNull { it.avgMs }
        } else {
            null
        }
        val ramAfter = snapshot.ram.availableMb
        val activeHz = profile?.preferredRefreshHz?.takeIf { it > 0 }
            ?: snapshot.display.refreshRateHz.roundToInt()

        auditDao.insertLog(
            SecurityAuditEntity(
                category = "OPTIMIZER",
                action = "Rutina pre-juego ejecutada",
                detail = buildString {
                    append("Índice ${snapshot.gamingReadinessScore}/100 · RAM ${ramBefore}->${ramAfter}MB")
                    if (clearedCount > 0) append(" · Temp: $clearedCount")
                    if (dndApplied) append(" · DND")
                    if (systemBrightnessApplied) append(" · Brillo ${targetBrightness}%")
                    if (appliedVolume != null) append(" · Audio ${appliedVolume}%")
                    if (ping != null) append(" · Ping ${ping.avgMs}ms (${ping.host})")
                    if (snapshot.isPowerSaveMode) append(" · Alerta: Ahorro energía activo")
                },
                authorizedMode = "Normal"
            )
        )

        OptimizationResult(
            ramBeforeMb = ramBefore,
            ramAfterMb = ramAfter,
            tempFilesCleared = clearedCount,
            freedTempBytes = freedBytes,
            dndApplied = dndApplied,
            systemBrightnessApplied = systemBrightnessApplied,
            mediaVolumeAppliedPercent = appliedVolume,
            appliedRefreshHz = activeHz,
            sustainedModeSupported = snapshot.sustainedPerformanceSupported,
            isPowerSaveModeActive = snapshot.isPowerSaveMode,
            readinessScore = snapshot.gamingReadinessScore,
            thermalHeadroomPercent = snapshot.temperature.thermalHeadroomPercent,
            pingResult = ping,
            androidGameModeLabel = gameModeLabel
        )
    }

    fun applySystemDnd(enable: Boolean): Boolean {
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager ?: return false
        if (!nm.isNotificationPolicyAccessGranted) return false
        return runCatching {
            nm.setInterruptionFilter(
                if (enable) NotificationManager.INTERRUPTION_FILTER_PRIORITY
                else NotificationManager.INTERRUPTION_FILTER_ALL
            )
            true
        }.getOrDefault(false)
    }

    private fun queryAndroidGameModeLabel(packageName: String?): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return "API GameManager desde Android 12"
        }
        val gm = context.getSystemService(GameManager::class.java) ?: return "No disponible"
        val mode = runCatching { gm.gameMode }.getOrNull() ?: return "Protegido por sistema"
        val suffix = if (!packageName.isNullOrBlank() && packageName != context.packageName) " (app)" else ""
        return when (mode) {
            GameManager.GAME_MODE_PERFORMANCE -> "Rendimiento$suffix"
            GameManager.GAME_MODE_BATTERY -> "Batería$suffix"
            GameManager.GAME_MODE_STANDARD -> "Estándar$suffix"
            GameManager.GAME_MODE_CUSTOM -> "Personalizado$suffix"
            else -> "Sin asignar"
        }
    }
}
