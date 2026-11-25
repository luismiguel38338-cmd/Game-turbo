package com.gameturbo.ai.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {
    @Query("SELECT * FROM registered_games ORDER BY displayName ASC")
    fun observeGames(): Flow<List<RegisteredGameEntity>>

    @Query("SELECT COUNT(*) FROM registered_games")
    fun observeGameCount(): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGame(game: RegisteredGameEntity)

    @Query("DELETE FROM registered_games WHERE packageName = :packageName")
    suspend fun deleteGame(packageName: String)

    @Query("SELECT * FROM game_sessions ORDER BY startedAt DESC LIMIT 50")
    fun observeSessions(): Flow<List<GameSessionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: GameSessionEntity)

    @Query("DELETE FROM game_sessions")
    suspend fun clearSessions()
}

@Dao
interface ProfileDao {
    @Query("SELECT * FROM gaming_profiles ORDER BY isBuiltIn DESC, id ASC")
    fun observeProfiles(): Flow<List<GamingProfileEntity>>

    @Query("SELECT * FROM gaming_profiles WHERE id = :id LIMIT 1")
    suspend fun getProfileById(id: Long): GamingProfileEntity?

    @Query("SELECT COUNT(*) FROM gaming_profiles")
    suspend fun countProfiles(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: GamingProfileEntity): Long

    @Update
    suspend fun updateProfile(profile: GamingProfileEntity)

    @Query("DELETE FROM gaming_profiles WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteCustomProfile(id: Long)
}

@Dao
interface TelemetryDao {
    @Query("SELECT * FROM telemetry_samples ORDER BY timestamp DESC LIMIT 60")
    fun observeRecentSamples(): Flow<List<TelemetrySampleEntity>>

    @Insert
    suspend fun insertSample(sample: TelemetrySampleEntity)

    @Query("DELETE FROM telemetry_samples WHERE id NOT IN (SELECT id FROM telemetry_samples ORDER BY timestamp DESC LIMIT 120)")
    suspend fun trimOldSamples()

    @Query("DELETE FROM telemetry_samples")
    suspend fun clearSamples()
}

@Dao
interface SecurityAuditDao {
    @Query("SELECT * FROM security_audit ORDER BY timestamp DESC LIMIT 80")
    fun observeAuditLogs(): Flow<List<SecurityAuditEntity>>

    @Insert
    suspend fun insertLog(entry: SecurityAuditEntity)

    @Query("DELETE FROM security_audit")
    suspend fun clearLogs()
}
