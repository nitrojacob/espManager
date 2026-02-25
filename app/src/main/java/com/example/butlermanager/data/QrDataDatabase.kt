package com.example.butlermanager.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [QrData::class], version = 1, exportSchema = false)
abstract class QrDataDatabase : RoomDatabase() {

    abstract fun qrDataDao(): QrDataDao

    companion object {
        @Volatile
        private var INSTANCE: QrDataDatabase? = null

        fun getDatabase(context: Context): QrDataDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QrDataDatabase::class.java,
                    "qr_data_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
