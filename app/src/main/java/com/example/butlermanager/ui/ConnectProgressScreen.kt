package com.example.butlermanager.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.Network
import android.os.Build
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.navigation.NavController

private const val TAG = "ConnectProgressScreen"

@Composable
fun ConnectProgressScreen(navController: NavController, ssid: String, pass: String) {
    Log.d(TAG, "Attempting to connect to SSID: $ssid")
    val context = LocalContext.current
    var connectionStatus by remember { mutableStateOf("Connecting...") }
    var showProgress by remember { mutableStateOf(true) }
    var retryEnabled by remember { mutableStateOf(false) }
    var retryCount by remember { mutableIntStateOf(0) }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.CHANGE_WIFI_STATE,
        Manifest.permission.CHANGE_NETWORK_STATE
    )

    var hasPermissions by remember {
        mutableStateOf(
            permissions.all {
                ContextCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
            }
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions(),
        onResult = { permissionsMap ->
            hasPermissions = permissionsMap.values.all { it }
            if (!hasPermissions) {
                Log.w(TAG, "Permissions not granted")
                connectionStatus = "Permission denied. Cannot connect to Wi-Fi."
                showProgress = false
            } else {
                Log.d(TAG, "Permissions granted")
            }
        }
    )

    LaunchedEffect(key1 = true) {
        Log.d(TAG, "hasPermissions: $hasPermissions")
        if (!hasPermissions) {
            Log.d(TAG, "Requesting permissions")
            launcher.launch(permissions)
        }
    }

    DisposableEffect(hasPermissions, retryCount) {
        Log.d(
            TAG,
            "DisposableEffect triggered. hasPermissions: $hasPermissions, retryCount: $retryCount"
        )
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                Log.d(TAG, "Network available: $network")
                if (showProgress) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        Log.d(TAG, "Binding process to network")
                        connectivityManager.bindProcessToNetwork(network)
                    }
                    connectionStatus = "Connected!"
                    showProgress = false
                    Log.d(TAG, "Navigating to timeEntry screen")
                    navController.navigate("timeEntry/$ssid")
                }
            }

            override fun onUnavailable() {
                super.onUnavailable()
                Log.w(TAG, "Network unavailable")
                if (showProgress) {
                    connectionStatus = "Failed to connect."
                    showProgress = false
                    retryEnabled = true
                }
            }
        }

        var isCallbackRegistered = false
        if (hasPermissions) {
            connectionStatus = "Connecting..."
            showProgress = true
            retryEnabled = false

            Log.d(TAG, "Android version: ${Build.VERSION.SDK_INT}")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                connectToWifiV29(connectivityManager, ssid, pass, networkCallback)
                isCallbackRegistered = true
            } else {
                Log.w(TAG, "Feature requires Android Q or higher.")
                connectionStatus = "Failed: This feature requires Android Q or higher."
                showProgress = false
            }
        }

        onDispose {
            Log.d(TAG, "Disposing effect")
            if (isCallbackRegistered) {
                Log.d(TAG, "Unregistering network callback")
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (showProgress) {
            CircularProgressIndicator()
        }
        Text(text = connectionStatus)
        if (!showProgress && connectionStatus.startsWith("Failed")) {
            Button(onClick = { navController.popBackStack() }) {
                Text("Back to Scanner")
            }
            if (retryEnabled) {
                Button(onClick = { retryCount++ }) {
                    Text("Retry")
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.Q)
private fun connectToWifiV29(
    connectivityManager: ConnectivityManager,
    ssid: String,
    pass: String,
    networkCallback: ConnectivityManager.NetworkCallback
) {
    Log.d(TAG, "Calling QWifiConnector.connect")
    QWifiConnector.connect(connectivityManager, ssid, pass, networkCallback)
}
