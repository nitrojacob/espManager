package com.example.butlermanager.ui

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.butlermanager.data.AppDatabase
import com.example.butlermanager.data.Device
import com.example.butlermanager.data.TimeSlot
import kotlinx.coroutines.launch

@Composable
fun TimeEntryScreen(navController: NavController, name: String) {
    val context = LocalContext.current
    val db = AppDatabase.getDatabase(context)
    val scope = rememberCoroutineScope()
    var timeSlots by remember { mutableStateOf(emptyList<TimeSlot>()) }
    var initialTimeSlots by remember { mutableStateOf(emptyList<TimeSlot>()) }
    var isFormDirty by remember { mutableStateOf(false) }

    LaunchedEffect(key1 = name) {
        scope.launch {
            val deviceWithTimeSlots = db.timeEntryDao().getDeviceWithTimeSlots(name)
            val loadedTimeSlots = if (deviceWithTimeSlots != null) {
                val existingTimeSlots = deviceWithTimeSlots.timeSlots
                List(10) { index ->
                    existingTimeSlots.find { it.rowIndex == index } ?: TimeSlot(deviceOwnerName = name, rowIndex = index, channel = 0, startTime = "", stopTime = "")
                }
            } else {
                List(10) { TimeSlot(deviceOwnerName = name, rowIndex = it, channel = 0, startTime = "", stopTime = "") }
            }
            timeSlots = loadedTimeSlots
            initialTimeSlots = loadedTimeSlots
        }
    }

    LaunchedEffect(timeSlots, initialTimeSlots) {
        isFormDirty = timeSlots != initialTimeSlots
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Device: $name")
        Spacer(modifier = Modifier.height(16.dp))
        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(timeSlots) { index, timeSlot ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = timeSlot.channel.toString(),
                        onValueChange = { newChannel ->
                            val updatedTimeSlots = timeSlots.toMutableList()
                            updatedTimeSlots[index] = timeSlot.copy(channel = newChannel.toIntOrNull() ?: 0)
                            timeSlots = updatedTimeSlots
                        },
                        label = { Text("Channel") },
                        modifier = Modifier.width(80.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = timeSlot.startTime,
                        onValueChange = { newStartTime ->
                            val updatedTimeSlots = timeSlots.toMutableList()
                            updatedTimeSlots[index] = timeSlot.copy(startTime = newStartTime.filter { it.isDigit() }.take(4))
                            timeSlots = updatedTimeSlots
                        },
                        label = { Text("Start Time") },
                        modifier = Modifier.weight(1f),
                        visualTransformation = { text ->
                            formatTime(text.text)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    OutlinedTextField(
                        value = timeSlot.stopTime,
                        onValueChange = { newStopTime ->
                            val updatedTimeSlots = timeSlots.toMutableList()
                            updatedTimeSlots[index] = timeSlot.copy(stopTime = newStopTime.filter { it.isDigit() }.take(4))
                            timeSlots = updatedTimeSlots
                        },
                        label = { Text("Stop Time") },
                        modifier = Modifier.weight(1f),
                        visualTransformation = { text ->
                            formatTime(text.text)
                        },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = {
                scope.launch {
                    db.timeEntryDao().insertDevice(Device(name))
                    db.timeEntryDao().insertTimeSlots(timeSlots)
                    initialTimeSlots = timeSlots
                }
            },
            enabled = isFormDirty
        ) {
            Text("Update")
        }
    }
}

fun formatTime(text: String): TransformedText {
    val out = buildString {
        for (i in text.indices) {
            append(text[i])
            if (i == 1) {
                append(':')
            }
        }
    }

    val offsetMapping = object : OffsetMapping {
        override fun originalToTransformed(offset: Int): Int {
            if (offset <= 1) return offset
            return (offset + 1).coerceAtMost(out.length)
        }

        override fun transformedToOriginal(offset: Int): Int {
            if (offset <= 2) return offset
            return (offset - 1).coerceAtMost(text.length)
        }
    }

    return TransformedText(AnnotatedString(out), offsetMapping)
}