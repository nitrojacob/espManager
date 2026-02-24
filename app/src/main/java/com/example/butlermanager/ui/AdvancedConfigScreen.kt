package com.example.butlermanager.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.butlermanager.EspressifManager

@Composable
fun AdvancedConfigScreen(navController: NavController, name: String, espressifManager: EspressifManager) {
    var ssid by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Advanced Configuration for $name")
        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = ssid,
            onValueChange = { ssid = it },
            label = { Text("SSID") }
        )
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") }
        )
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            espressifManager.ssid = ssid
            espressifManager.password = password
            navController.previousBackStackEntry?.savedStateHandle?.set("advanced_settings_saved", true)
            navController.popBackStack()
        }) {
            Text("Save")
        }
    }
}
