package com.example.butlermanager

import android.content.Context
import android.util.Log
import com.espressif.provisioning.DeviceConnectionEvent
import com.espressif.provisioning.ESPConstants
import com.espressif.provisioning.ESPDevice
import com.espressif.provisioning.ESPProvisionManager
import com.example.butlermanager.data.QrData
import kotlinx.coroutines.suspendCancellableCoroutine
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class EspressifManager(context: Context) {
    private val provisionManager: ESPProvisionManager = ESPProvisionManager.getInstance(context.applicationContext)
    private var espDevice: ESPDevice? = null

    suspend fun connect(qrData: QrData) {
        val transport = qrData.transport ?: "softap" // Default to softap
        val transportType = if (transport.equals("ble", ignoreCase = true)) {
            ESPConstants.TransportType.TRANSPORT_BLE
        } else {
            ESPConstants.TransportType.TRANSPORT_SOFTAP
        }

        val security = qrData.security ?: "0" // Default to security 0
        val securityType = if (security == "1") {
            ESPConstants.SecurityType.SECURITY_1
        } else {
            ESPConstants.SecurityType.SECURITY_0
        }

        if (securityType == ESPConstants.SecurityType.SECURITY_1 && qrData.pop == null) {
            throw IllegalArgumentException("Proof of possession is required for Security 1")
        }

        if (transportType == ESPConstants.TransportType.TRANSPORT_BLE && qrData.name == null) {
            throw IllegalArgumentException("Device name is required for BLE transport")
        }

        val device = provisionManager.createESPDevice(transportType, securityType)
        espDevice = device

        device.proofOfPossession = qrData.pop ?: ""
        device.deviceName = qrData.name ?: ""

        return suspendCancellableCoroutine { continuation ->
            val eventBus = EventBus.getDefault()

            val subscriber = object {
                @Subscribe(threadMode = ThreadMode.MAIN)
                fun onDeviceConnectionEvent(event: DeviceConnectionEvent) {
                    // Once we get an event, unregister the listener to avoid leaks and multiple callbacks.
                    if (eventBus.isRegistered(this)) {
                        eventBus.unregister(this)
                    }

                    if (continuation.isActive) {
                        when (event.eventType) {
                            ESPConstants.EVENT_DEVICE_CONNECTED -> {
                                Log.d(TAG, "Device connected")
                                continuation.resume(Unit)
                            }
                            ESPConstants.EVENT_DEVICE_CONNECTION_FAILED -> {
                                Log.e(TAG, "Device connection failed")
                                continuation.resumeWithException(Exception("Failed to connect to device"))
                            }
                        }
                    }
                }
            }

            continuation.invokeOnCancellation {
                // If the coroutine is cancelled, we should also unregister the listener.
                if (eventBus.isRegistered(subscriber)) {
                    eventBus.unregister(subscriber)
                }
            }

            eventBus.register(subscriber)

            if (transportType == ESPConstants.TransportType.TRANSPORT_SOFTAP) {
                // For SoftAP, we need to connect to the device's Wi-Fi network first.
                device.connectWiFiDevice(qrData.name, qrData.password)
            } else {
                // BLE transport requires scanning first, which is not implemented.
                if (eventBus.isRegistered(subscriber)) {
                    eventBus.unregister(subscriber)
                }
                if (continuation.isActive) {
                    continuation.resumeWithException(NotImplementedError("Only SoftAP is supported at the moment."))
                }
            }
        }
    }

    companion object {
        private const val TAG = "EspressifManager"
    }
}
