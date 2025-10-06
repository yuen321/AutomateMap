package com.example.automatemap

import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.automatemap.service.MapAutomationService
import com.example.automatemap.ui.MainScreen
import com.example.automatemap.ui.theme.AutomateMapTheme
import com.example.automatemap.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AutomateMapTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainScreen()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(viewModel.serviceResultReceiver, IntentFilter(MapAutomationService.ACTION_AUTOMATION_COMPLETE), RECEIVER_EXPORTED)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(viewModel.serviceResultReceiver)
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    AutomateMapTheme {
        MainScreen()
    }
}