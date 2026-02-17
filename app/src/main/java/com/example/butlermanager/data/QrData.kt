package com.example.butlermanager.data

data class QrData(
    val name: String?,
    val pop: String?,
    val transport: String?,
    val security: String?,
    val password: String? = null // Add password for SoftAP
)
