package com.example.butlermanager.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction

@Dao
interface TimeEntryDao {

    @Transaction
    @Query("SELECT * FROM time_entry_configurations WHERE name = :name")
    suspend fun getConfigurationWithTimeSlots(name: String): ConfigurationWithTimeSlots?

    @Query("SELECT * FROM time_entry_configurations")
    suspend fun getAllConfigurations(): List<TimeEntryConfiguration>

    @Query("SELECT * FROM time_slots WHERE configurationName = :configurationName")
    suspend fun getTimeSlotsForConfiguration(configurationName: String): List<TimeSlot>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfiguration(configuration: TimeEntryConfiguration)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlot(timeSlot: TimeSlot)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeSlots(timeSlots: List<TimeSlot>)

    @Query("DELETE FROM time_slots WHERE configurationName = :configurationName")
    suspend fun deleteTimeSlotsForConfiguration(configurationName: String)

    @Transaction
    suspend fun updateTimeSlotsForConfiguration(configurationName: String, timeSlots: List<TimeSlot>) {
        insertConfiguration(TimeEntryConfiguration(configurationName))
        deleteTimeSlotsForConfiguration(configurationName)
        insertTimeSlots(timeSlots)
    }

    @Transaction
    suspend fun renameConfiguration(oldName: String, newName: String) {
        val timeSlots = getTimeSlotsForConfiguration(oldName)
        deleteTimeSlotsForConfiguration(oldName)
        timeSlots.forEach { insertTimeSlot(it.copy(configurationName = newName)) }
        deleteConfig(oldName)
        insertConfiguration(TimeEntryConfiguration(newName))

    }

    @Query("DELETE FROM time_entry_configurations WHERE name = :name")
    suspend fun deleteConfig(name: String)

    @Transaction
    suspend fun deleteConfigurationAndSlots(name: String) {
        deleteTimeSlotsForConfiguration(name)
        deleteConfig(name)
    }
}
