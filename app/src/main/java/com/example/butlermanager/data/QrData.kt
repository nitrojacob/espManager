package com.example.butlermanager.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "qr_data")
data class QrData(
    @PrimaryKey
    val name: String,
    val pop: String?,
    val transport: String?,
    val security: String?,
    val password: String? = null // Add password for SoftAP
)
