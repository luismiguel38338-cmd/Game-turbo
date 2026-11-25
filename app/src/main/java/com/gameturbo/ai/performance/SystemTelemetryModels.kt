package com.gameturbo.ai.performance

enum class ThermalLevel(val label: String) {
    NORMAL("Normal"),
    ELEVATED("Elevada"),
    HIGH("Alta"),
    UNAVAILABLE("No disponible")
}

data class RamTelemetry(
    val totalMb: Long,
    val availableMb: Long,
    val usedMb: Long,
    val usedPercent: Float,
    val lowMemory: Boolean,
    val thresholdMb: Long,
    val appHeapUsedMb: Long,
    val appHeapMaxMb: Long
)

data class CpuCoreFreq(
    val coreIndex: Int,
    val currentMhz: Int?,
    val minMhz: Int?,
    val maxMhz: Int?
)

data class CpuTelemetry(
    val processorLabel: String,
    val socModel: String?,
    val architecture: String,
    val supportedAbis: List<String>,
    val coreCount: Int,
    val coreFrequencies: List<CpuCoreFreq>,
    val governor: String?,
    val loadRestrictedNote: String
)

data class BatteryTelemetry(
    val levelPercent: Int,
    val statusLabel: String,
    val pluggedLabel: String,
    val temperatureC: Float?,
    val voltageMv: Int?,
    val technology: String?,
    val healthLabel: String,
    val currentNowMa: Int?
)

data class TemperatureTelemetry(
    val batteryTempC: Float?,
    val ambientTempC: Float?,
    val systemThermalStatus: String,
    val level: ThermalLevel,
    val sensorNote: String
)

data class StorageOverview(
    val totalBytes: Long,
    val availableBytes: Long,
    val usedBytes: Long,
    val usedPercent: Float
)

data class ConnectivitySnapshot(
    val transportLabel: String,
    val isConnected: Boolean,
    val isMetered: Boolean,
    val downstreamKbps: Int?,
    val upstreamKbps: Int?
)

data class DisplaySnapshot(
    val widthPx: Int,
    val heightPx: Int,
    val densityDpi: Int,
    val refreshRateHz: Float,
    val hdrSupported: Boolean
)

data class DeviceSnapshot(
    val manufacturer: String,
    val model: String,
    val brand: String,
    val androidVersion: String,
    val sdkInt: Int,
    val securityPatch: String,
    val ram: RamTelemetry,
    val cpu: CpuTelemetry,
    val battery: BatteryTelemetry,
    val temperature: TemperatureTelemetry,
    val storage: StorageOverview,
    val connectivity: ConnectivitySnapshot,
    val display: DisplaySnapshot,
    val overallHealthLabel: String,
    val optimizationStatusLabel: String,
    val timestamp: Long = System.currentTimeMillis()
)
