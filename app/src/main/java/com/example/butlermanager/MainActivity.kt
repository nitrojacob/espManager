package com.example.butlermanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.butlermanager.ui.AdvancedConfigScreen
import com.example.butlermanager.ui.AllDevicesScreen
import com.example.butlermanager.ui.ConnectProgressScreen
import com.example.butlermanager.ui.NearbyDevicesScreen
import com.example.butlermanager.ui.QrScannerScreen
import com.example.butlermanager.ui.TimeEntryScreen
import com.example.butlermanager.ui.theme.ButlerManagerTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val espressifManager = EspressifManager(applicationContext)
        setContent {
            ButlerManagerTheme {
                AppNavigation(espressifManager)
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppNavigation(espressifManager: EspressifManager) {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                title = { Text("Butler Manager") }
            )
        }
    ) { paddingValues ->
        NavHost(
            navController = navController, startDestination = "qrScanner",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("qrScanner") {
                QrScannerScreen(navController)
            }
            composable("nearbyDevices") {
                NearbyDevicesScreen(navController)
            }
            composable(
                route = "timeEntry/{name}",
                arguments = listOf(navArgument("name") { defaultValue = "" })
            ) { backStackEntry ->
                TimeEntryScreen(
                    navController = navController,
                    name = backStackEntry.arguments?.getString("name") ?: "",
                    espressifManager = espressifManager
                )
            }
            composable(
                route = "advanced_config/{name}",
                arguments = listOf(navArgument("name") { defaultValue = "" })
            ) { backStackEntry ->
                AdvancedConfigScreen(
                    navController = navController,
                    name = backStackEntry.arguments?.getString("name") ?: "",
                    espressifManager = espressifManager
                )
            }
            composable("allDevices") {
                AllDevicesScreen(navController)
            }
            composable(
                route = "connectProgress/{qrDataJson}",
                arguments = listOf(
                    navArgument("qrDataJson") { defaultValue = "" }
                )
            ) { backStackEntry ->
                ConnectProgressScreen(
                    navController = navController,
                    qrDataJson = backStackEntry.arguments?.getString("qrDataJson") ?: "",
                    espressifManager = espressifManager
                )
            }
        }
    }

}