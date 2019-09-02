package com.mrklie.yangtao.persistence

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = arrayOf(Hanzi::class), version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun hanziDao(): HanziDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                // Create database here
                val instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java, "database-name"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}