package com.gameturbo.ai.performance

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.Sensor
import android.hardware.SensorManager
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.PowerManager
import android.os.StatFs
import android.view.Display
import java.io.File

class SystemTelemetryReader(private val context: Context) {

    fun captureSnapshot(): DeviceSnapshot {
        val ram = readRam()
        val cpu = readCpu()
        val battery = readBattery()
        val temperature = readTemperature(battery.temperatureC)
        val storage = readStorage()
        val connectivity = readConnectivity()
        val display = readDisplay()

        val overallHealth = when {
            temperature.level == ThermalLevel.HIGH || ram.lowMemory -> "Atención requerida"
            temperature.level == ThermalLevel.ELEVATED || ram.usedPercent > 85f -> "Carga moderada"
            else -> "Sistema óptimo"
        }

        val optimizationStatus = when {
            ram.lowMemory -> "Memoria baja detectada por Android"
            ram.usedPercent > 82f -> "Uso de RAM elevado (${ram.usedPercent.toInt()}%)"
            temperature.level == ThermalLevel.HIGH -> "Temperatura de batería alta"
            else -> "Parámetros estables"
        }

        return DeviceSnapshot(
            manufacturer = Build.MANUFACTURER.replaceFirstChar { it.uppercase() },
            model = Build.MODEL,
            brand = Build.BRAND.replaceFirstChar { it.uppercase() },
            androidVersion = Build.VERSION.RELEASE ?: "Desconocida",
            sdkInt = Build.VERSION.SDK_INT,
            securityPatch = Build.VERSION.SECURITY_PATCH ?: "No disponible",
            ram = ram,
            cpu = cpu,
            battery = battery,
            temperature = temperature,
            storage = storage,
            connectivity = connectivity,
            display = display,
            overallHealthLabel = overallHealth,
            optimizationStatusLabel = optimizationStatus
        )
    }

    fun readRam(): RamTelemetry {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memInfo)

        val totalMb = (memInfo.totalMem / (1024L * 1024L)).coerceAtLeast(1L)
        val availMb = (memInfo.availMem / (1024L * 1024L)).coerceAtLeast(0L)
        val usedMb = (totalMb - availMb).coerceAtLeast(0L)
        val usedPercent = ((usedMb.toFloat() / totalMb.toFloat()) * 100f).coerceIn(0f, 100f)

        val runtime = Runtime.getRuntime()
        val appUsedMb = ((runtime.totalMemory() - runtime.freeMemory()) / (1024L * 1024L)).coerceAtLeast(0L)
        val appMaxMb = (runtime.maxMemory() / (1024L * 1024L)).coerceAtLeast(1L)

        return RamTelemetry(
            totalMb = totalMb,
            availableMb = availMb,
            usedMb = usedMb,
            usedPercent = usedPercent,
            lowMemory = memInfo.lowMemory,
            thresholdMb = memInfo.threshold / (1024L * 1024L),
            appHeapUsedMb = appUsedMb,
            appHeapMaxMb = appMaxMb
        )
    }

    fun readCpu(): CpuTelemetry {
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)
        val abis = Build.SUPPORTED_ABIS?.toList().orEmpty()
        val arch = abis.firstOrNull() ?: System.getProperty("os.arch") ?: "Desconocida"

        val socModel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val model = Build.SOC_MODEL
            val mfg = Build.SOC_MANUFACTURER
            listOfNotNull(
                mfg.takeIf { it.isNotBlank() && it != Build.UNKNOWN },
                model.takeIf { it.isNotBlank() && it != Build.UNKNOWN }
            ).joinToString(" ").ifBlank { null }
        } else {
            null
        }

        val hardwareFromProc = readProcCpuHardware()
        val processorLabel = socModel
            ?: hardwareFromProc
            ?: Build.HARDWARE.takeIf { it.isNotBlank() && it != Build.UNKNOWN }
            ?: "Procesador no expuesto por Android"

        val coreFreqs = (0 until cores).map { index ->
            CpuCoreFreq(
                coreIndex = index,
                currentMhz = readSysInt("/sys/devices/system/cpu/cpu$index/cpufreq/scaling_cur_freq")?.div(1000),
                minMhz = readSysInt("/sys/devices/system/cpu/cpu$index/cpufreq/cpuinfo_min_freq")?.div(1000),
                maxMhz = readSysInt("/sys/devices/system/cpu/cpu$index/cpufreq/cpuinfo_max_freq")?.div(1000)
            )
        }
        val anyFreqReadable = coreFreqs.any { it.currentMhz != null || it.maxMhz != null }
        val governor = readSysText("/sys/devices/system/cpu/cpu0/cpufreq/scaling_governor")

        val restrictionNote = if (anyFreqReadable) {
            "Frecuencias consultadas en modo lectura. Android bloquea la lectura global de /proc/stat para apps sin privilegios, por lo que no se inventa el % de carga."
        } else {
            "Android restringe el acceso directo a las frecuencias y carga de CPU en este dispositivo (SELinux). Solo se muestran metadatos oficiales del sistema."
        }

        return CpuTelemetry(
            processorLabel = processorLabel,
            socModel = socModel,
            architecture = arch,
            supportedAbis = abis,
            coreCount = cores,
            coreFrequencies = coreFreqs,
            governor = governor,
            loadRestrictedNote = restrictionNote
        )
    }

    fun readBattery(): BatteryTelemetry {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, 100)?.takeIf { it > 0 } ?: 100
        val percent = if (level >= 0) ((level * 100f) / scale).toInt().coerceIn(0, 100) else 0

        val status = intent?.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
            ?: BatteryManager.BATTERY_STATUS_UNKNOWN
        val statusLabel = when (status) {
            BatteryManager.BATTERY_STATUS_CHARGING -> "Cargando"
            BatteryManager.BATTERY_STATUS_DISCHARGING -> "En uso (descargando)"
            BatteryManager.BATTERY_STATUS_FULL -> "Carga completa"
            BatteryManager.BATTERY_STATUS_NOT_CHARGING -> "Conectada (sin carga)"
            else -> "No disponible"
        }

        val plugged = intent?.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) ?: 0
        val pluggedLabel = when (plugged) {
            BatteryManager.BATTERY_PLUGGED_AC -> "Adaptador AC"
            BatteryManager.BATTERY_PLUGGED_USB -> "Puerto USB"
            BatteryManager.BATTERY_PLUGGED_WIRELESS -> "Carga inalámbrica"
            else -> "Sin cargador"
        }

        val tempRaw = intent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, Int.MIN_VALUE) ?: Int.MIN_VALUE
        val tempC = if (tempRaw != Int.MIN_VALUE && tempRaw > -200 && tempRaw < 1500) {
            tempRaw / 10f
        } else {
            null
        }

        val voltageRaw = intent?.getIntExtra(BatteryManager.EXTRA_VOLTAGE, -1) ?: -1
        val voltageMv = voltageRaw.takeIf { it > 0 }

        val technology = intent?.getStringExtra(BatteryManager.EXTRA_TECHNOLOGY)?.takeIf { it.isNotBlank() }

        val health = intent?.getIntExtra(BatteryManager.EXTRA_HEALTH, BatteryManager.BATTERY_HEALTH_UNKNOWN)
            ?: BatteryManager.BATTERY_HEALTH_UNKNOWN
        val healthLabel = when (health) {
            BatteryManager.BATTERY_HEALTH_GOOD -> "Buena"
            BatteryManager.BATTERY_HEALTH_OVERHEAT -> "Sobrecalentamiento"
            BatteryManager.BATTERY_HEALTH_DEAD -> "Degradada / Dañada"
            BatteryManager.BATTERY_HEALTH_OVER_VOLTAGE -> "Sobrevoltaje"
            BatteryManager.BATTERY_HEALTH_COLD -> "Fría"
            BatteryManager.BATTERY_HEALTH_UNSPECIFIED_FAILURE -> "Fallo reportado"
            else -> "Android no la expone en este dispositivo"
        }

        val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
        val currentNowUa = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CURRENT_NOW)
        val currentNowMa = if (currentNowUa != null && currentNowUa != Int.MIN_VALUE && currentNowUa != 0) {
            // Most devices report microamperes; normalize safely if value looks like µA
            if (kotlin.math.abs(currentNowUa) > 10_000) currentNowUa / 1000 else currentNowUa
        } else {
            null
        }

        return BatteryTelemetry(
            levelPercent = percent,
            statusLabel = statusLabel,
            pluggedLabel = pluggedLabel,
            temperatureC = tempC,
            voltageMv = voltageMv,
            technology = technology,
            healthLabel = healthLabel,
            currentNowMa = currentNowMa
        )
    }

    fun readTemperature(batteryTempC: Float?): TemperatureTelemetry {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        val thermalStatusLabel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && powerManager != null) {
            when (powerManager.currentThermalStatus) {
                PowerManager.THERMAL_STATUS_NONE -> "Normal (Sin restricción térmica)"
                PowerManager.THERMAL_STATUS_LIGHT -> "Ligera"
                PowerManager.THERMAL_STATUS_MODERATE -> "Moderada"
                PowerManager.THERMAL_STATUS_SEVERE -> "Severa"
                PowerManager.THERMAL_STATUS_CRITICAL -> "Crítica"
                PowerManager.THERMAL_STATUS_EMERGENCY -> "Emergencia"
                PowerManager.THERMAL_STATUS_SHUTDOWN -> "Apagado térmico"
                else -> "No disponible"
            }
        } else {
            "API térmica oficial disponible desde Android 10"
        }

        val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as? SensorManager
        val ambientAvailable = sensorManager?.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null

        val level = when {
            batteryTempC == null -> ThermalLevel.UNAVAILABLE
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                powerManager != null &&
                powerManager.currentThermalStatus >= PowerManager.THERMAL_STATUS_SEVERE -> ThermalLevel.HIGH
            batteryTempC >= 41.5f -> ThermalLevel.HIGH
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q &&
                powerManager != null &&
                powerManager.currentThermalStatus == PowerManager.THERMAL_STATUS_MODERATE -> ThermalLevel.ELEVATED
            batteryTempC >= 36.5f -> ThermalLevel.ELEVATED
            else -> ThermalLevel.NORMAL
        }

        val sensorNote = buildString {
            append("Los sensores térmicos accesibles dependen del fabricante. ")
            append("Android expone la temperatura oficial de batería y el estado térmico del sistema sin permitir lecturas directas de CPU/GPU en dispositivos no rooteados. ")
            if (!ambientAvailable) {
                append("Este dispositivo no incluye sensor de temperatura ambiente dedicado.")
            }
        }

        return TemperatureTelemetry(
            batteryTempC = batteryTempC,
            ambientTempC = null,
            systemThermalStatus = thermalStatusLabel,
            level = level,
            sensorNote = sensorNote
        )
    }

    fun readStorage(): StorageOverview {
        val stat = StatFs(Environment.getDataDirectory().path)
        val total = stat.totalBytes.coerceAtLeast(1L)
        val avail = stat.availableBytes.coerceAtLeast(0L)
        val used = (total - avail).coerceAtLeast(0L)
        val percent = ((used.toFloat() / total.toFloat()) * 100f).coerceIn(0f, 100f)
        return StorageOverview(
            totalBytes = total,
            availableBytes = avail,
            usedBytes = used,
            usedPercent = percent
        )
    }

    fun readConnectivity(): ConnectivitySnapshot {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
        val activeNetwork = cm?.activeNetwork
        val caps = if (activeNetwork != null) cm.getNetworkCapabilities(activeNetwork) else null

        if (caps == null) {
            return ConnectivitySnapshot(
                transportLabel = "Sin conexión",
                isConnected = false,
                isMetered = false,
                downstreamKbps = null,
                upstreamKbps = null
            )
        }

        val transport = when {
            caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> "Wi-Fi"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> "Datos móviles"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> "Ethernet"
            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> "VPN"
            else -> "Otra red"
        }

        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        val metered = !caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)

        return ConnectivitySnapshot(
            transportLabel = transport,
            isConnected = hasInternet,
            isMetered = metered,
            downstreamKbps = caps.linkDownstreamBandwidthKbps.takeIf { it > 0 },
            upstreamKbps = caps.linkUpstreamBandwidthKbps.takeIf { it > 0 }
        )
    }

    fun readDisplay(): DisplaySnapshot {
        val metrics = context.resources.displayMetrics
        val dm = context.getSystemService(Context.DISPLAY_SERVICE) as? DisplayManager
        val display = dm?.getDisplay(Display.DEFAULT_DISPLAY)

        val refreshRate = display?.refreshRate ?: 60f
        val hdrSupported = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            (display?.hdrCapabilities?.supportedHdrTypes?.isNotEmpty()) == true
        } else {
            false
        }

        val mode = display?.mode
        val widthPx = mode?.physicalWidth?.takeIf { it > 0 } ?: metrics.widthPixels
        val heightPx = mode?.physicalHeight?.takeIf { it > 0 } ?: metrics.heightPixels

        return DisplaySnapshot(
            widthPx = widthPx,
            heightPx = heightPx,
            densityDpi = metrics.densityDpi,
            refreshRateHz = refreshRate,
            hdrSupported = hdrSupported
        )
    }

    private fun readProcCpuHardware(): String? {
        return try {
            val lines = File("/proc/cpuinfo").readLines()
            lines.firstOrNull {
                it.startsWith("Hardware", ignoreCase = true) ||
                    it.startsWith("model name", ignoreCase = true)
            }?.substringAfter(':')?.trim()?.takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }

    private fun readSysInt(path: String): Int? {
        return try {
            File(path).readText().trim().toIntOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private fun readSysText(path: String): String? {
        return try {
            File(path).readText().trim().takeIf { it.isNotBlank() }
        } catch (_: Exception) {
            null
        }
    }
}
