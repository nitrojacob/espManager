package com.example.butlermanager.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.butlermanager.EspressifManager
import com.example.butlermanager.data.Device
import com.example.butlermanager.data.TimeEntryDatabase
import com.example.butlermanager.data.TimeSlot
import kotlinx.coroutines.launch

@Composable
fun TimeEntryScreen(navController: NavController, name: String, espressifManager: EspressifManager) {
    val context = LocalContext.current
    val db = TimeEntryDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    var timeSlots by remember { mutableStateOf(emptyList<TimeSlot>()) }
    var initialTimeSlots by remember { mutableStateOf(emptyList<TimeSlot>()) }
    var advancedSettingsChanged by remember { mutableStateOf(false) }
    var isProvisioning by remember { mutableStateOf(false) }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    LaunchedEffect(navBackStackEntry) {
        if (navBackStackEntry?.savedStateHandle?.remove<Boolean>("advanced_settings_saved") == true) {
            advancedSettingsChanged = true
        }
    }

    DisposableEffect(navController) {
        onDispose {
            // Only disconnect if we are not navigating to the AdvancedConfigScreen
            val destinationRoute = navController.currentBackStackEntry?.destination?.route
            if (destinationRoute?.startsWith("advanced_config/") != true) {
                scope.launch {
                    espressifManager.disconnect()
                }
            }
        }
    }


    LaunchedEffect(key1 = name) {
        scope.launch {
            val deviceWithTimeSlots = db.timeEntryDao().getDeviceWithTimeSlots(name)
            val loadedTimeSlots = if (deviceWithTimeSlots != null) {
                val existingTimeSlots = deviceWithTimeSlots.timeSlots
                List(15) { index ->
                    existingTimeSlots.find { it.rowIndex == index } ?: TimeSlot(
                        deviceOwnerName = name,
                        rowIndex = index,
                        channel = 0,
                        hour = 24,
                        minute = 0,
                        onOff = 0
                    )
                }
            } else {
                List(15) { TimeSlot(deviceOwnerName = name, rowIndex = it, channel = 0, hour = 24, minute = 0, onOff = 0) }
            }
            timeSlots = loadedTimeSlots
            initialTimeSlots = loadedTimeSlots
        }
    }

    val isFormDirty = (timeSlots != initialTimeSlots) || advancedSettingsChanged

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Device: $name")
        Spacer(modifier = Modifier.height(16.dp))
        Box(modifier = Modifier.weight(1f)) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                itemsIndexed(timeSlots) { index, timeSlot ->
                    if (timeSlot.hour < 24) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = timeSlot.hour.toString(),
                                onValueChange = { newHour ->
                                    val updatedTimeSlots = timeSlots.toMutableList()
                                    val hour = newHour.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                                    if (hour in 0..23) {
                                        updatedTimeSlots[index] = timeSlot.copy(hour = hour)
                                        timeSlots = updatedTimeSlots
                                    }
                                },
                                label = { Text("Hr") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = timeSlot.minute.toString(),
                                onValueChange = { newMinute ->
                                    val updatedTimeSlots = timeSlots.toMutableList()
                                    val minute = newMinute.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0
                                    if (minute in 0..59) {
                                        updatedTimeSlots[index] = timeSlot.copy(minute = minute)
                                        timeSlots = updatedTimeSlots
                                    }
                                },
                                label = { Text("Min") },
                                modifier = Modifier.weight(1f),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = timeSlot.channel.toString(),
                                onValueChange = { newChannel ->
                                    val updatedTimeSlots = timeSlots.toMutableList()
                                    updatedTimeSlots[index] = timeSlot.copy(channel = newChannel.filter { it.isDigit() }.take(2).toIntOrNull() ?: 0)
                                    timeSlots = updatedTimeSlots
                                },
                                label = { Text("Ch") },
                                modifier = Modifier.width(80.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            OutlinedTextField(
                                value = timeSlot.onOff.toString(),
                                onValueChange = { newOnOff ->
                                    val updatedTimeSlots = timeSlots.toMutableList()
                                    updatedTimeSlots[index] = timeSlot.copy(onOff = newOnOff.filter { it.isDigit() }.take(1).toIntOrNull() ?: 0)
                                    timeSlots = updatedTimeSlots
                                },
                                label = { Text("On/Off") },
                                modifier = Modifier.width(80.dp),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            IconButton(onClick = {
                                val updatedTimeSlots = timeSlots.toMutableList()
                                updatedTimeSlots[index] = timeSlot.copy(hour = 24, minute = 0, channel = 0, onOff = 0)
                                timeSlots = updatedTimeSlots
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "Delete")
                            }
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
            FloatingActionButton(
                onClick = {
                    val firstEmptyIndex = timeSlots.indexOfFirst { it.hour >= 24 }
                    if (firstEmptyIndex != -1) {
                        val updatedTimeSlots = timeSlots.toMutableList()
                        updatedTimeSlots[firstEmptyIndex] = timeSlots[firstEmptyIndex].copy(hour = 0, minute = 0, channel = 0, onOff = 0)
                        timeSlots = updatedTimeSlots
                    }
                },
                modifier = Modifier.align(Alignment.BottomEnd)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Add")
            }
        }
        if (isProvisioning) {
            CircularProgressIndicator()
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row {
            Button(
                onClick = {
                    navController.navigate("advanced_config/$name")
                },
                enabled = !isProvisioning
            ) {
                Text("Advanced")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        isProvisioning = true
                        db.timeEntryDao().insertDevice(Device(name))
                        db.timeEntryDao().insertTimeSlots(timeSlots)
                        espressifManager.writeCronData()
                        espressifManager.provision()
                        initialTimeSlots = timeSlots
                        advancedSettingsChanged = false
                        isProvisioning = false
                        navController.navigate("qrScanner")
                    }
                },
                enabled = isFormDirty && !isProvisioning
            ) {
                Text("Update")
            }
        }
    }
}
