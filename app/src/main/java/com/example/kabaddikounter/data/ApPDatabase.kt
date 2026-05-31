package com.example.kabaddikounter.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MatchEntity::class, ScoreLogEntity::class], version = 4)
abstract class AppDatabase : RoomDatabase() {

    abstract fun matchDao(): MatchDao
    abstract fun scoreLogDao(): ScoreLogDao

    companion object {
        fun getInstance(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "kabaddi_db"
            )
                .fallbackToDestructiveMigration() // hapus & buat ulang DB saat schema berubah
                .build()
        }
    }
}