package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MatchEntity::class,
        InningsEntity::class,
        BallEventEntity::class,
        ActiveMatchStateEntity::class,
        TeamEntity::class,
        PlayerEntity::class,
        TournamentEntity::class,
        TournamentTeamEntity::class,
        NotificationEntity::class,
        ScorerNoteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class MahiCricDatabase : RoomDatabase() {
    abstract fun matchDao(): MatchDao
    abstract fun inningsDao(): InningsDao
    abstract fun ballEventDao(): BallEventDao
    abstract fun activeMatchStateDao(): ActiveMatchStateDao
    abstract fun teamDao(): TeamDao
    abstract fun playerDao(): PlayerDao
    abstract fun tournamentDao(): TournamentDao
    abstract fun notificationDao(): NotificationDao
    abstract fun scorerNoteDao(): ScorerNoteDao

    companion object {
        @Volatile
        private var INSTANCE: MahiCricDatabase? = null

        fun getInstance(context: Context): MahiCricDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    MahiCricDatabase::class.java,
                    "mahi_cric_pro.db"
                )
                    .setJournalMode(JournalMode.WRITE_AHEAD_LOGGING)
                    .addCallback(object : Callback() {
                        override fun onOpen(db: SupportSQLiteDatabase) {
                            super.onOpen(db)
                            try {
                                // Enforce synchronous = FULL for maximum crash/power-loss resilience
                                db.execSQL("PRAGMA synchronous = FULL;")
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    })
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
