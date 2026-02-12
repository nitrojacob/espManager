package com.example.butlermanager.ui

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi

private const val TAG = "QWifiConnector"

@RequiresApi(Build.VERSION_CODES.Q)
object QWifiConnector {
    fun connect(
        connectivityManager: ConnectivityManager,
        ssid: String,
        pass: String,
        networkCallback: ConnectivityManager.NetworkCallback
    ) {
        Log.d(TAG, "connect() called with SSID: $ssid")

        val specifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(pass)
            .build()
        Log.d(TAG, "WifiNetworkSpecifier built")

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(specifier)
            .build()
        Log.d(TAG, "NetworkRequest built")

        connectivityManager.requestNetwork(request, networkCallback, 60_000)
        Log.d(TAG, "Network request sent")
    }
}
