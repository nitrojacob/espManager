package com.example.butlermanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TimeEntryDao {

    @Transaction
    @Query("SELECT * FROM devices WHERE name = :name")
    suspend fun getDeviceWithTimeSlots(name: String): DeviceWithTimeSlots?

    @Query("SELECT * FROM devices")
    suspend fun getAllDevices(): List<Device>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDevice(device: Device)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlots(timeSlots: List<TimeSlot>)
}