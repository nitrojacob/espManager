package com.example.butlermanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [TimeEntryConfiguration::class, TimeSlot::class], version = 1, exportSchema = false)
abstract class TimeEntryDatabase : RoomDatabase() {

    abstract fun timeEntryDao(): TimeEntryDao

    companion object {
        @Volatile
        private var INSTANCE: TimeEntryDatabase? = null

        fun getDatabase(context: Context): TimeEntryDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    TimeEntryDatabase::class.java,
                    "time_entry_database"
                ).fallbackToDestructiveMigration().build()
                INSTANCE = instance
                instance
            }
        }
    }
}
