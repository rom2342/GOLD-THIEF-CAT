package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.MainScreen
import com.example.ui.PrivacyViewModel
import com.example.ui.theme.CamMicBlockerTheme

class MainActivity : ComponentActivity() {

    private val viewModel: PrivacyViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            CamMicBlockerTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                // Launcher for Device Admin activation intent
                val adminLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.refreshState()
                }

                // Launcher for general settings
                val settingsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.StartActivityForResult()
                ) {
                    viewModel.refreshState()
                }

                // Launcher for runtime permissions (Camera, Mic test, Notifications)
                val permissionsLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestMultiplePermissions()
                ) {
                    viewModel.refreshState()
                }

                LaunchedEffect(Unit) {
                    val permsToRequest = mutableListOf(
                        Manifest.permission.CAMERA,
                        Manifest.permission.RECORD_AUDIO
                    )
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        permsToRequest.add(Manifest.permission.POST_NOTIFICATIONS)
                    }
                    permissionsLauncher.launch(permsToRequest.toTypedArray())
                }

                MainScreen(
                    viewModel = viewModel,
                    uiState = uiState,
                    onLaunchDeviceAdmin = { intent ->
                        adminLauncher.launch(intent)
                    },
                    onLaunchSettings = { intent ->
                        settingsLauncher.launch(intent)
                    }
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.refreshState()
    }
}
