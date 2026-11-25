package com.gameturbo.ai.data.repository

import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.gameturbo.ai.data.db.SecurityAuditDao
import com.gameturbo.ai.data.db.SecurityAuditEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

enum class AdvancedTier(val title: String, val badge: String) {
    NORMAL("Modo Normal", "Sin Root"),
    SHIZUKU("Modo Shizuku", "Autorizado"),
    ADB("Modo ADB", "Depuración"),
    ROOT("Modo Root", "Superusuario")
}

data class AdvancedCapabilitiesSnapshot(
    val canWriteSystemSettings: Boolean,
    val hasDndPolicyAccess: Boolean,
    val ignoringBatteryOptimizations: Boolean,
    val isShizukuInstalled: Boolean,
    val isAdbEnabledInSystem: Boolean,
    val suBinaryPresent: Boolean
)

class AdvancedSystemRepository(
    private val context: Context,
    private val auditDao: SecurityAuditDao
) {
    val auditLogs: Flow<List<SecurityAuditEntity>> = auditDao.observeAuditLogs()

    suspend fun inspectCapabilities(): AdvancedCapabilitiesSnapshot = withContext(Dispatchers.IO) {
        val canWrite = Settings.System.canWrite(context)
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
        val dndAccess = nm?.isNotificationPolicyAccessGranted == true

        val pm = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val ignoringBatteryOpt = pm?.isIgnoringBatteryOptimizations(context.packageName) == true

        val shizukuInstalled = isPackageInstalled("moe.shizuku.privileged.api")

        val adbEnabled = try {
            Settings.Global.getInt(context.contentResolver, Settings.Global.ADB_ENABLED, 0) == 1
        } catch (_: Exception) {
            false
        }

        val suFound = listOf(
            "/system/bin/su",
            "/system/xbin/su",
            "/sbin/su",
            "/su/bin/su",
            "/vendor/bin/su"
        ).any { path ->
            try {
                File(path).exists()
            } catch (_: Exception) {
                false
            }
        }

        AdvancedCapabilitiesSnapshot(
            canWriteSystemSettings = canWrite,
            hasDndPolicyAccess = dndAccess,
            ignoringBatteryOptimizations = ignoringBatteryOpt,
            isShizukuInstalled = shizukuInstalled,
            isAdbEnabledInSystem = adbEnabled,
            suBinaryPresent = suFound
        )
    }

    suspend fun logAuditEvent(
        category: String,
        action: String,
        detail: String,
        mode: String
    ) = withContext(Dispatchers.IO) {
        auditDao.insertLog(
            SecurityAuditEntity(
                category = category,
                action = action,
                detail = detail,
                authorizedMode = mode
            )
        )
    }

    suspend fun clearAuditLogs() = withContext(Dispatchers.IO) {
        auditDao.clearLogs()
    }

    private fun isPackageInstalled(pkg: String): Boolean {
        return try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(pkg, PackageManager.PackageInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                context.packageManager.getPackageInfo(pkg, 0)
            }
            true
        } catch (_: Exception) {
            false
        }
    }
}
