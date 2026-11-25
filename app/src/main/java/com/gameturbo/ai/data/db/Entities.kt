package com.gameturbo.ai.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "registered_games")
data class RegisteredGameEntity(
    @PrimaryKey val packageName: String,
    val displayName: String,
    val isAutoDetected: Boolean,
    val assignedProfileId: Long? = null,
    val focusModeEnabled: Boolean = false,
    val keepScreenOn: Boolean = true,
    val notes: String = "",
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "game_sessions")
data class GameSessionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val packageName: String,
    val gameName: String,
    val profileName: String,
    val startedAt: Long = System.currentTimeMillis(),
    val ramPercentAtStart: Int,
    val batteryPercentAtStart: Int,
    val tempCelsiusAtStart: Float
)

@Entity(tableName = "gaming_profiles")
data class GamingProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val name: String,
    val category: String, // BALANCED, GAMING, BATTERY_SAVER, CUSTOM
    val samplingIntervalMs: Long,
    val focusMode: Boolean,
    val keepScreenOn: Boolean,
    val hapticFeedback: Boolean,
    val brightnessPercent: Int, // -1 = System default, 10..100 = Preferred level
    val isBuiltIn: Boolean,
    val description: String
)

@Entity(tableName = "telemetry_samples")
data class TelemetrySampleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val ramUsedPercent: Float,
    val ramAvailMb: Long,
    val batteryLevel: Int,
    val batteryTempC: Float,
    val cpuActiveCores: Int
)

@Entity(tableName = "security_audit")
data class SecurityAuditEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val timestamp: Long = System.currentTimeMillis(),
    val category: String,
    val action: String,
    val detail: String,
    val authorizedMode: String
)
