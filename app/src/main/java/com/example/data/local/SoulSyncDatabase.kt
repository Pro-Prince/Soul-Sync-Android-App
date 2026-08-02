package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.local.dao.*
import com.example.data.local.entity.*

@Database(
    entities = [
        DiaryEntry::class,
        MoodLog::class,
        CycleLog::class,
        CyclePeriod::class,
        Achievement::class,
        AppSettings::class,
        UserAccount::class
    ],
    version = 4,
    exportSchema = false
)
abstract class SoulSyncDatabase : RoomDatabase() {
    abstract fun diaryEntryDao(): DiaryEntryDao
    abstract fun moodLogDao(): MoodLogDao
    abstract fun cycleLogDao(): CycleLogDao
    abstract fun cyclePeriodDao(): CyclePeriodDao
    abstract fun achievementDao(): AchievementDao
    abstract fun appSettingsDao(): AppSettingsDao
    abstract fun userAccountDao(): UserAccountDao

    companion object {
        @Volatile
        private var INSTANCE: SoulSyncDatabase? = null

        @Volatile
        private var activeDbName: String? = null

        val MIGRATION_1_2 = object : androidx.room.migration.Migration(1, 2) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE diary_entries ADD COLUMN contentPlain TEXT NOT NULL DEFAULT ''")
            }
        }

        val MIGRATION_2_3 = object : androidx.room.migration.Migration(2, 3) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS user_accounts_new (id TEXT NOT NULL PRIMARY KEY, email TEXT NOT NULL, passwordKey TEXT NOT NULL, displayName TEXT NOT NULL)")
                db.execSQL("INSERT OR REPLACE INTO user_accounts_new (id, email, passwordKey, displayName) SELECT email, email, passwordKey, displayName FROM user_accounts")
                db.execSQL("DROP TABLE IF EXISTS user_accounts")
                db.execSQL("ALTER TABLE user_accounts_new RENAME TO user_accounts")
            }
        }

        val MIGRATION_3_4 = object : androidx.room.migration.Migration(3, 4) {
            override fun migrate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                // Placeholder to satisfy version 4 requirement
            }
        }

        fun migrateDatabaseIfNecessary(context: Context, email: String, userId: String) {
            if (email.isBlank() || userId.isBlank()) return
            val oldNormalized = email.trim().lowercase().replace(Regex("[^a-zA-Z0-9]"), "_")
            val oldDbName = "soulsync_database_$oldNormalized"
            val newDbName = "soulsync_database_${userId.replace(Regex("[^a-zA-Z0-9]"), "_")}"
            
            val oldDbFile = context.getDatabasePath(oldDbName)
            val newDbFile = context.getDatabasePath(newDbName)
            
            if (oldDbFile.exists() && !newDbFile.exists()) {
                try {
                    oldDbFile.renameTo(newDbFile)
                    
                    val oldWalFile = java.io.File(oldDbFile.path + "-wal")
                    val newWalFile = java.io.File(newDbFile.path + "-wal")
                    if (oldWalFile.exists()) {
                        oldWalFile.renameTo(newWalFile)
                    }
                    
                    val oldShmFile = java.io.File(oldDbFile.path + "-shm")
                    val newShmFile = java.io.File(newDbFile.path + "-shm")
                    if (oldShmFile.exists()) {
                        oldShmFile.renameTo(newShmFile)
                    }
                    android.util.Log.d("SoulSyncDatabase", "Successfully migrated local database from email ($email) to UUID ($userId)")
                } catch (e: Exception) {
                    android.util.Log.e("SoulSyncDatabase", "Failed to migrate local database from email to UUID", e)
                }
            }
        }

        fun getDatabase(context: Context, userId: String? = null): SoulSyncDatabase {
            val PREPOPULATE_CALLBACK = object : RoomDatabase.Callback() {
                override fun onCreate(db: androidx.sqlite.db.SupportSQLiteDatabase) {
                    super.onCreate(db)
                    val achievements = listOf(
                        "first_entry", "streak_3", "streak_7", "streak_14",
                        "emotional_awareness", "pattern_breaker", "first_image",
                        "first_voice", "sticker_user", "theme_explorer", 
                        "reflection_starter", "deep_reflection"
                    )
                    achievements.forEach { id ->
                        db.execSQL(
                            "INSERT INTO achievements(id, unlockedAt) VALUES('$id', NULL)"
                        )
                    }
                }
            }

            val dbName = if (userId.isNullOrBlank()) {
                "soulsync_database_guest"
            } else {
                "soulsync_database_" + userId.replace(Regex("[^a-zA-Z0-9]"), "_")
            }

            return synchronized(this) {
                val activeInstance = INSTANCE
                if (activeInstance != null && activeDbName == dbName) {
                    activeInstance
                } else {
                    activeInstance?.close()
                    val instance = Room.databaseBuilder(
                        context.applicationContext,
                        SoulSyncDatabase::class.java,
                        dbName
                    )
                    .addCallback(PREPOPULATE_CALLBACK)
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4)
                    .fallbackToDestructiveMigration()
                    .build()
                    INSTANCE = instance
                    activeDbName = dbName
                    instance
                }
            }
        }
    }
}
