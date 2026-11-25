package com.gameturbo.ai.data

import android.content.Context
import com.gameturbo.ai.data.db.GameTurboDatabase
import com.gameturbo.ai.data.prefs.PreferencesRepository
import com.gameturbo.ai.data.repository.AdvancedSystemRepository
import com.gameturbo.ai.data.repository.GameRepository
import com.gameturbo.ai.data.repository.ProfileRepository
import com.gameturbo.ai.data.repository.StorageRepository
import com.gameturbo.ai.data.repository.TelemetryRepository
import com.gameturbo.ai.performance.SystemTelemetryReader

class AppContainer(context: Context) {
    private val appContext = context.applicationContext
    private val database = GameTurboDatabase.getInstance(appContext)

    val preferencesRepository = PreferencesRepository(appContext)
    val telemetryReader = SystemTelemetryReader(appContext)
    val telemetryRepository = TelemetryRepository(telemetryReader, database.telemetryDao())
    val gameRepository = GameRepository(appContext, database.gameDao(), database.securityAuditDao())
    val profileRepository = ProfileRepository(
        database.profileDao(),
        preferencesRepository,
        database.securityAuditDao()
    )
    val storageRepository = StorageRepository(appContext, database.securityAuditDao())
    val advancedRepository = AdvancedSystemRepository(appContext, database.securityAuditDao())
}
