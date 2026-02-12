package com.example.butlermanager

import android.content.Context
import com.espressif.provisioning.ESPProvisionManager

object EspressifManager {
    fun init(context: Context) {
        val provisionManager = ESPProvisionManager.getInstance(context)
    }
}