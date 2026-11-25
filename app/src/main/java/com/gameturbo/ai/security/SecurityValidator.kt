package com.gameturbo.ai.security

import android.content.Context
import android.content.Intent
import android.widget.Toast
import java.io.File

object SecurityValidator {

    /**
     * Validates that [targetFile] is strictly contained inside one of the app's own
     * sandboxed directories (filesDir, cacheDir, externalCacheDir) to prevent path traversal
     * or accidental deletion of external/system files.
     */
    fun isAppOwnedDeletableFile(context: Context, targetFile: File): Boolean {
        return try {
            val canonicalTarget = targetFile.canonicalFile
            val allowedRoots = buildList {
                add(context.filesDir.canonicalFile)
                add(context.cacheDir.canonicalFile)
                context.externalCacheDir?.let { add(it.canonicalFile) }
            }
            allowedRoots.any { root ->
                canonicalTarget.path.startsWith(root.path + File.separator) &&
                    canonicalTarget != root
            }
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Sanitizes display names or user-provided labels to avoid control characters.
     */
    fun sanitizeLabel(input: String, maxLength: Int = 48): String {
        return input
            .replace(Regex("[\\p{Cntrl}]"), "")
            .trim()
            .take(maxLength)
    }

    /**
     * Launches an external Android system Intent safely without crashing if the OEM
     * omits a specific settings screen.
     */
    fun safeStartActivity(
        context: Context,
        intent: Intent,
        fallbackIntent: Intent? = null,
        errorMessage: String = "Esta pantalla del sistema no está disponible en este dispositivo."
    ): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            true
        } catch (_: Exception) {
            if (fallbackIntent != null) {
                try {
                    fallbackIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(fallbackIntent)
                    return true
                } catch (_: Exception) {
                    // Fall through to toast
                }
            }
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            false
        }
    }
}
