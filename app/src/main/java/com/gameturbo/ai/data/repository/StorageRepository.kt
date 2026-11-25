package com.gameturbo.ai.data.repository

import android.content.Context
import com.gameturbo.ai.data.db.SecurityAuditDao
import com.gameturbo.ai.data.db.SecurityAuditEntity
import com.gameturbo.ai.security.SecurityValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class AppStorageFileItem(
    val path: String,
    val name: String,
    val sizeBytes: Long,
    val category: String,
    val isTemporary: Boolean,
    val lastModified: Long
)

data class AppStorageInspection(
    val files: List<AppStorageFileItem>,
    val totalAppFilesBytes: Long,
    val totalTempCacheBytes: Long
)

class StorageRepository(
    private val context: Context,
    private val auditDao: SecurityAuditDao
) {
    suspend fun inspectAppOwnedStorage(): AppStorageInspection = withContext(Dispatchers.IO) {
        val collected = mutableListOf<AppStorageFileItem>()

        fun scanDir(dir: File?, category: String, isTemp: Boolean) {
            if (dir == null || !dir.exists()) return
            dir.walkTopDown()
                .filter { it.isFile && SecurityValidator.isAppOwnedDeletableFile(context, it) }
                .forEach { file ->
                    // Do not expose internal SQLite database files as deletable temp files
                    val isDbFile = file.name.startsWith("game_turbo_ai.db")
                    if (!isDbFile) {
                        collected.add(
                            AppStorageFileItem(
                                path = file.absolutePath,
                                name = file.name,
                                sizeBytes = file.length(),
                                category = category,
                                isTemporary = isTemp || file.extension.lowercase() in setOf("tmp", "cache", "log"),
                                lastModified = file.lastModified()
                            )
                        )
                    }
                }
        }

        scanDir(context.cacheDir, "Caché interna", true)
        scanDir(context.externalCacheDir, "Caché externa propia", true)
        scanDir(context.filesDir, "Archivos de la app", false)

        val sorted = collected.sortedByDescending { it.sizeBytes }
        val tempBytes = sorted.filter { it.isTemporary }.sumOf { it.sizeBytes }
        val totalBytes = sorted.sumOf { it.sizeBytes }

        AppStorageInspection(
            files = sorted,
            totalAppFilesBytes = totalBytes,
            totalTempCacheBytes = tempBytes
        )
    }

    suspend fun createDiagnosticCacheSnapshot(): Boolean = withContext(Dispatchers.IO) {
        return@withContext try {
            val diagDir = File(context.cacheDir, "telemetry_logs").apply { mkdirs() }
            val file = File(diagDir, "diag_${System.currentTimeMillis()}.tmp")
            file.writeText("Game Turbo AI local cache diagnostic entry\n")
            true
        } catch (_: Exception) {
            false
        }
    }

    suspend fun deleteSelectedFiles(paths: Set<String>): Int = withContext(Dispatchers.IO) {
        var deletedCount = 0
        var freedBytes = 0L
        paths.forEach { rawPath ->
            val target = File(rawPath)
            if (target.exists() && target.isFile && SecurityValidator.isAppOwnedDeletableFile(context, target)) {
                val len = target.length()
                if (target.delete()) {
                    deletedCount++
                    freedBytes += len
                }
            }
        }
        if (deletedCount > 0) {
            auditDao.insertLog(
                SecurityAuditEntity(
                    category = "STORAGE",
                    action = "Limpieza confirmada por usuario",
                    detail = "Eliminados $deletedCount archivos propios (${freedBytes / 1024} KB liberados).",
                    authorizedMode = "Normal"
                )
            )
        }
        deletedCount
    }
}
