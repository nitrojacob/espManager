package com.example.butlermanager.ui

import android.Manifest
import android.content.pm.PackageManager
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.butlermanager.EspressifManager
import com.example.butlermanager.data.QrData
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "ConnectProgressScreen"

enum class StepStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILURE
}

data class ProvisioningStep(
    val title: String,
    val status: MutableState<StepStatus>,
    val errorText: MutableState<String?> = mutableStateOf(null)
)

@Composable
fun rememberProvisioningSteps(): List<ProvisioningStep> {
    return remember {
        listOf(
            ProvisioningStep("QR Code Parsed", mutableStateOf(StepStatus.SUCCESS)),
            ProvisioningStep("Device Connection", mutableStateOf(StepStatus.PENDING)),
        )
    }
}

@Composable
fun ConnectProgressScreen(
    navController: NavController, qrDataJson: String, espressifManager: EspressifManager
) {
    val qrData = remember(qrDataJson) {
        try {
            Gson().fromJson(qrDataJson, QrData::class.java)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Failed to parse QR data", e)
            null
        }
    }

    if (qrData == null) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Error: Invalid QR Code data.")
            Button(onClick = { navController.popBackStack() }) {
                Text("Back to Scanner")
            }
        }
        return
    }

    Log.d(TAG, "Attempting to connect to device: ${qrData.name}")
    val context = LocalContext.current
    var overallStatus by remember { mutableStateOf("Connecting...") }
    var showBackButton by remember { mutableStateOf(false) }

    val steps = rememberProvisioningSteps()

    fun updateStep(title: String, newStatus: StepStatus, error: String? = null) {
        steps.find { it.title == title }?.apply {
            status.value = newStatus
            errorText.value = error
        }
    }

    val permissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
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
                overallStatus = "Permission denied. Cannot connect to device."
                showBackButton = true
            } else {
                Log.d(TAG, "Permissions granted")
            }
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasPermissions) {
            launcher.launch(permissions)
        }
    }

    LaunchedEffect(hasPermissions) {
        if (hasPermissions) {
            updateStep("Device Connection", StepStatus.IN_PROGRESS)
            try {
                withContext(Dispatchers.IO) {
                    espressifManager.connect(qrData)
                }
                updateStep("Device Connection", StepStatus.SUCCESS)
                overallStatus = "Connected successfully!"
                navController.navigate("timeEntry/${qrData.name ?: ""}") {
                    popUpTo("qrScanner") { inclusive = true }
                }
            } catch (e: Throwable) {
                val errorMessage = when (e) {
                    is IllegalArgumentException -> "Invalid QR code data."
                    is NotImplementedError -> "BLE transport is not yet supported."
                    else -> e.message ?: "An unknown error occurred."
                }
                Log.e(TAG, "Failed to connect to device: $errorMessage", e)
                updateStep("Device Connection", StepStatus.FAILURE, errorMessage)
                overallStatus = "Failed to connect to device."
                showBackButton = true
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = overallStatus, modifier = Modifier.padding(bottom = 16.dp))

        steps.forEach { step ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                val status = step.status.value
                val iconModifier = Modifier.size(24.dp)

                when (status) {
                    StepStatus.PENDING -> Icon(
                        imageVector = Icons.Filled.AccessTime,
                        contentDescription = "Pending",
                        modifier = iconModifier,
                        tint = Color.Gray
                    )
                    StepStatus.IN_PROGRESS -> CircularProgressIndicator(modifier = iconModifier)
                    StepStatus.SUCCESS -> Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Success",
                        modifier = iconModifier,
                        tint = Color(0xFF00C853) // A nice green
                    )
                    StepStatus.FAILURE -> Icon(
                        imageVector = Icons.Filled.Warning,
                        contentDescription = "Failure",
                        modifier = iconModifier,
                        tint = Color.Red
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = step.title)
                    if (step.errorText.value != null) {
                        Text(text = step.errorText.value!!, color = Color.Red)
                    }
                }
            }
        }

        if (showBackButton) {
            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Back to Scanner")
            }
        }
    }
}
