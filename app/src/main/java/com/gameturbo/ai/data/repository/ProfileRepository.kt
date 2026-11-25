package com.gameturbo.ai.data.repository

import com.gameturbo.ai.data.db.GamingProfileEntity
import com.gameturbo.ai.data.db.ProfileDao
import com.gameturbo.ai.data.db.SecurityAuditDao
import com.gameturbo.ai.data.db.SecurityAuditEntity
import com.gameturbo.ai.data.prefs.PreferencesRepository
import com.gameturbo.ai.security.SecurityValidator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class ProfileRepository(
    private val profileDao: ProfileDao,
    private val prefsRepo: PreferencesRepository,
    private val auditDao: SecurityAuditDao
) {
    val profiles: Flow<List<GamingProfileEntity>> = profileDao.observeProfiles()

    suspend fun ensureDefaultProfiles() = withContext(Dispatchers.IO) {
        if (profileDao.countProfiles() > 0) return@withContext
        val defaults = listOf(
            GamingProfileEntity(
                name = "Rendimiento equilibrado",
                category = "BALANCED",
                samplingIntervalMs = 3000L,
                focusMode = false,
                keepScreenOn = false,
                hapticFeedback = true,
                brightnessPercent = -1,
                mediaVolumePercent = -1,
                preferredRefreshHz = 0,
                isBuiltIn = true,
                description = "Equilibrio térmico y de batería."
            ),
            GamingProfileEntity(
                name = "Gaming",
                category = "GAMING",
                samplingIntervalMs = 1500L,
                focusMode = true,
                keepScreenOn = true,
                hapticFeedback = true,
                brightnessPercent = 85,
                mediaVolumePercent = 75,
                preferredRefreshHz = 120,
                isBuiltIn = true,
                description = "Máximo refresco permitido, DND y brillo alto."
            ),
            GamingProfileEntity(
                name = "Ahorro de batería",
                category = "BATTERY_SAVER",
                samplingIntervalMs = 8000L,
                focusMode = false,
                keepScreenOn = false,
                hapticFeedback = false,
                brightnessPercent = 40,
                mediaVolumePercent = -1,
                preferredRefreshHz = 60,
                isBuiltIn = true,
                description = "Muestreo bajo y brillo reducido."
            )
        )
        defaults.forEach { profileDao.insertProfile(it) }
    }

    suspend fun activateProfile(profile: GamingProfileEntity) = withContext(Dispatchers.IO) {
        prefsRepo.applyProfileSettings(
            profileId = profile.id,
            samplingIntervalMs = profile.samplingIntervalMs,
            focusMode = profile.focusMode,
            keepScreenOn = profile.keepScreenOn,
            hapticFeedback = profile.hapticFeedback,
            brightnessPercent = profile.brightnessPercent,
            mediaVolumePercent = profile.mediaVolumePercent,
            preferredRefreshHz = profile.preferredRefreshHz,
            sustainedPerformanceMode = profile.category == "GAMING"
        )
        auditDao.insertLog(
            SecurityAuditEntity(
                category = "PROFILES",
                action = "Perfil activado",
                detail = "${profile.name} (${profile.samplingIntervalMs}ms)",
                authorizedMode = "Normal"
            )
        )
    }

    suspend fun saveCustomProfile(
        existingId: Long? = null,
        name: String,
        samplingIntervalMs: Long,
        focusMode: Boolean,
        keepScreenOn: Boolean,
        hapticFeedback: Boolean,
        brightnessPercent: Int,
        mediaVolumePercent: Int,
        preferredRefreshHz: Int,
        description: String
    ) = withContext(Dispatchers.IO) {
        val cleanName = SecurityValidator.sanitizeLabel(name, 36).ifBlank { "Personalizado" }
        val cleanDesc = SecurityValidator.sanitizeLabel(description, 120)
            .ifBlank { "Perfil personalizado." }
        val entity = GamingProfileEntity(
            id = existingId ?: 0L,
            name = cleanName,
            category = "CUSTOM",
            samplingIntervalMs = samplingIntervalMs.coerceIn(1000L, 10000L),
            focusMode = focusMode,
            keepScreenOn = keepScreenOn,
            hapticFeedback = hapticFeedback,
            brightnessPercent = brightnessPercent,
            mediaVolumePercent = mediaVolumePercent,
            preferredRefreshHz = preferredRefreshHz,
            isBuiltIn = false,
            description = cleanDesc
        )
        if (existingId != null && existingId > 0L) {
            val current = profileDao.getProfileById(existingId)
            if (current != null) {
                profileDao.updateProfile(
                    entity.copy(
                        isBuiltIn = current.isBuiltIn,
                        category = current.category
                    )
                )
            }
        } else {
            profileDao.insertProfile(entity)
        }
        auditDao.insertLog(
            SecurityAuditEntity(
                category = "PROFILES",
                action = if (existingId != null) "Perfil actualizado" else "Perfil creado",
                detail = cleanName,
                authorizedMode = "Normal"
            )
        )
    }

    suspend fun deleteCustomProfile(profile: GamingProfileEntity) = withContext(Dispatchers.IO) {
        if (!profile.isBuiltIn) {
            profileDao.deleteCustomProfile(profile.id)
            auditDao.insertLog(
                SecurityAuditEntity(
                    category = "PROFILES",
                    action = "Perfil eliminado",
                    detail = profile.name,
                    authorizedMode = "Normal"
                )
            )
        }
    }
}
