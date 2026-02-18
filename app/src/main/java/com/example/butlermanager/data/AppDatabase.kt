package com.example.butlermanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [Device::class, TimeSlot::class], version = 6, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timeEntryDao(): TimeEntryDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        val MIGRATION_4_5: Migration = object : Migration(4, 5) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // Create new table
                database.execSQL(
                    "CREATE TABLE `time_slots_new` (" +
                            "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                            "`deviceOwnerName` TEXT NOT NULL, " +
                            "`rowIndex` INTEGER NOT NULL, " +
                            "`channel` INTEGER NOT NULL, " +
                            "`hour` INTEGER NOT NULL, " +
                            "`minute` INTEGER NOT NULL, " +
                            "`onOff` INTEGER NOT NULL, " +
                            "FOREIGN KEY(`deviceOwnerName`) REFERENCES `devices`(`name`) ON UPDATE NO ACTION ON DELETE CASCADE)"
                )

                // Copy the data
                database.execSQL(
                    "INSERT INTO time_slots_new (id, deviceOwnerName, rowIndex, channel, hour, minute, onOff) " +
                            "SELECT id, deviceOwnerName, rowIndex, channel, time / 100, time % 100, onOff FROM time_slots"
                )

                // Remove the old table
                database.execSQL("DROP TABLE time_slots")

                // Change the table name to the correct one
                database.execSQL("ALTER TABLE time_slots_new RENAME TO time_slots")
            }
        }

        val MIGRATION_5_6: Migration = object : Migration(5, 6) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("CREATE INDEX `index_time_slots_deviceOwnerName` ON `time_slots` (`deviceOwnerName`)")
            }
        }

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                .addMigrations(MIGRATION_4_5, MIGRATION_5_6)
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}