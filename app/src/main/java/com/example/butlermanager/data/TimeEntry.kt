package com.example.butlermanager.data

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "devices")
data class Device(
    @PrimaryKey val name: String
)

@Entity(tableName = "time_slots")
data class TimeSlot(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val deviceOwnerName: String,
    val rowIndex: Int,
    val hour: Int,
    val minute: Int,
    val channel: Int,
    val onOff: Int
)

data class DeviceWithTimeSlots(
    @Embedded val device: Device,
    @Relation(
        parentColumn = "name",
        entityColumn = "deviceOwnerName"
    )
    val timeSlots: List<TimeSlot>
)
