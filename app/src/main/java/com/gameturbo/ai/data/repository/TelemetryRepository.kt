package com.gameturbo.ai.data.repository

import com.gameturbo.ai.data.db.TelemetryDao
import com.gameturbo.ai.data.db.TelemetrySampleEntity
import com.gameturbo.ai.performance.DeviceSnapshot
import com.gameturbo.ai.performance.SystemTelemetryReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class TelemetryRepository(
    private val reader: SystemTelemetryReader,
    private val telemetryDao: TelemetryDao
) {
    val recentSamples: Flow<List<TelemetrySampleEntity>> = telemetryDao.observeRecentSamples()

    suspend fun captureAndPersist(): DeviceSnapshot = withContext(Dispatchers.IO) {
        val snapshot = reader.captureSnapshot()
        telemetryDao.insertSample(
            TelemetrySampleEntity(
                timestamp = snapshot.timestamp,
                ramUsedPercent = snapshot.ram.usedPercent,
                ramAvailMb = snapshot.ram.availableMb,
                batteryLevel = snapshot.battery.levelPercent,
                batteryTempC = snapshot.battery.temperatureC ?: -1f,
                cpuActiveCores = snapshot.cpu.coreCount
            )
        )
        telemetryDao.trimOldSamples()
        snapshot
    }

    suspend fun captureImmediate(): DeviceSnapshot = withContext(Dispatchers.IO) {
        reader.captureSnapshot()
    }

    suspend fun clearHistory() = withContext(Dispatchers.IO) {
        telemetryDao.clearSamples()
    }
}
