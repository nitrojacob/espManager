package com.example.butlermanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.butlermanager.data.AppDatabase
import com.example.butlermanager.data.Device

@Composable
fun AllDevicesScreen(navController: NavController) {
    val context = LocalContext.current
    var devices by remember { mutableStateOf<List<Device>>(emptyList()) }

    LaunchedEffect(Unit) {
        val database = AppDatabase.getDatabase(context)
        devices = database.timeEntryDao().getAllDevices()
    }

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn {
            items(devices) { device ->
                Text(
                    text = device.name,
                    modifier = Modifier.clickable { navController.navigate("timeEntry/${device.name}") }
                )
            }
        }
    }
}
