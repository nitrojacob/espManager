package com.example.butlermanager.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.butlermanager.data.TimeEntryConfiguration
import com.example.butlermanager.data.TimeEntryDatabase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SavedConfigsScreen(navController: NavController) {
    val context = LocalContext.current
    var configs by remember { mutableStateOf<List<TimeEntryConfiguration>>(emptyList()) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCopyDialog by remember { mutableStateOf<TimeEntryConfiguration?>(null) }
    var showRenameDialog by remember { mutableStateOf<TimeEntryConfiguration?>(null) }
    var showDeleteDialog by remember { mutableStateOf<TimeEntryConfiguration?>(null) }
    var newConfigName by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    fun loadConfigs() {
        coroutineScope.launch {
            val database = TimeEntryDatabase.getDatabase(context)
            configs = database.timeEntryDao().getAllConfigurations()
        }
    }

    LaunchedEffect(Unit) {
        loadConfigs()
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Filled.Add, contentDescription = "Add new configuration")
            }
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(configs) { config ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { navController.navigate("timeEntryConfig/${config.name}") },
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = config.name)
                        Box {
                            var expanded by remember { mutableStateOf(false) }
                            IconButton(onClick = { expanded = true }) {
                                Icon(Icons.Filled.MoreVert, contentDescription = "More options")
                            }
                            DropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Copy") },
                                    onClick = {
                                        showCopyDialog = config
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Rename") },
                                    onClick = {
                                        showRenameDialog = config
                                        expanded = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text("Delete") },
                                    onClick = {
                                        showDeleteDialog = config
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("New Configuration") },
            text = {
                TextField(
                    value = newConfigName,
                    onValueChange = { newConfigName = it },
                    label = { Text("Configuration Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val database = TimeEntryDatabase.getDatabase(context)
                            database.timeEntryDao().insertConfiguration(TimeEntryConfiguration(newConfigName))
                            showAddDialog = false
                            navController.navigate("timeEntryConfig/$newConfigName")
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                Button(onClick = { showAddDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    showCopyDialog?.let { configToCopy ->
        var newCopyName by remember { mutableStateOf("${configToCopy.name} (copy)") }
        AlertDialog(
            onDismissRequest = { showCopyDialog = null },
            title = { Text("Copy Configuration") },
            text = {
                TextField(
                    value = newCopyName,
                    onValueChange = { newCopyName = it },
                    label = { Text("New Configuration Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val database = TimeEntryDatabase.getDatabase(context)
                            val timeSlots = database.timeEntryDao().getTimeSlotsForConfiguration(configToCopy.name)
                            database.timeEntryDao().insertConfiguration(TimeEntryConfiguration(newCopyName))
                            timeSlots.forEach {
                                database.timeEntryDao().insertTimeSlot(it.copy(configurationName = newCopyName, id = 0))
                            }
                            showCopyDialog = null
                            loadConfigs()
                        }
                    }
                ) {
                    Text("Copy")
                }
            },
            dismissButton = {
                Button(onClick = { showCopyDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    showRenameDialog?.let { configToRename ->
        var newName by remember { mutableStateOf(configToRename.name) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = null },
            title = { Text("Rename Configuration") },
            text = {
                TextField(
                    value = newName,
                    onValueChange = { newName = it },
                    label = { Text("New Configuration Name") }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val database = TimeEntryDatabase.getDatabase(context)
                            database.timeEntryDao().renameConfiguration(configToRename.name, newName)
                            showRenameDialog = null
                            loadConfigs()
                        }
                    }
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                Button(onClick = { showRenameDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    showDeleteDialog?.let { configToDelete ->
        AlertDialog(
            onDismissRequest = { showDeleteDialog = null },
            title = { Text("Delete Configuration") },
            text = { Text("Are you sure you want to delete '${configToDelete.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            val database = TimeEntryDatabase.getDatabase(context)
                            database.timeEntryDao().deleteConfigurationAndSlots(configToDelete.name)
                            showDeleteDialog = null
                            loadConfigs()
                        }
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                Button(onClick = { showDeleteDialog = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}
