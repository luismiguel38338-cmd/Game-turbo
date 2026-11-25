package com.gameturbo.ai.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [
        RegisteredGameEntity::class,
        GameSessionEntity::class,
        GamingProfileEntity::class,
        TelemetrySampleEntity::class,
        SecurityAuditEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class GameTurboDatabase : RoomDatabase() {
    abstract fun gameDao(): GameDao
    abstract fun profileDao(): ProfileDao
    abstract fun telemetryDao(): TelemetryDao
    abstract fun securityAuditDao(): SecurityAuditDao

    companion object {
        @Volatile
        private var INSTANCE: GameTurboDatabase? = null

        fun getInstance(context: Context): GameTurboDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    GameTurboDatabase::class.java,
                    "game_turbo_ai.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
