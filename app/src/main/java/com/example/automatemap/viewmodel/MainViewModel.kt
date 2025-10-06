package com.example.automatemap.viewmodel

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Settings
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.automatemap.UiState
import com.example.automatemap.service.MapAutomationService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel: ViewModel(){
    companion object{
        const val MAPS_PACKAGE = "com.google.android.apps.maps"
    }

    private val _uiState: MutableStateFlow<UiState> = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState

    val serviceResultReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == MapAutomationService.ACTION_AUTOMATION_COMPLETE) {
                // Get the data from the Service
                val data = intent.getStringExtra(MapAutomationService.PLACE_DETAIL)
                // 2. Post the data to the ViewModel
                data?.let { updateToastMessage(it) }
            }
        }
    }

    fun launchAutomateMapScreen(context: Context){
        if(!isAccessibilityServiceEnabled(context)){
            //prompt user to enable service
            val intent = Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
            context.startActivity(intent)
            return
        }else{
            //Launch Google Map as a new task
            val mapIntent = Intent(Intent.ACTION_VIEW).apply {
                setPackage(MAPS_PACKAGE)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(mapIntent)

            // Find the running service and trigger automation
            viewModelScope.launch {
                getAccessibilityService(context)?.startAutomation()
            }
        }
    }

    private fun isAccessibilityServiceEnabled(context: Context): Boolean {
        // Implementation to check if the service is enabled
        // (This part can be complex; for simplicity, we assume the user will enable it)
        val enabledServices = Settings.Secure.getString(context.contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)
        val serviceName = context.packageName + "/" + MapAutomationService::class.java.name
        return enabledServices?.contains(serviceName) == true
    }

    private fun getAccessibilityService(context: Context): MapAutomationService? {
        return MapAutomationService.getInstance()
    }

    private fun updateToastMessage(message: String) {
        _uiState.value = _uiState.value.copy(toastMessage = message)
    }

    fun clearToastMessage() {
        _uiState.value = _uiState.value.copy(toastMessage = null)
    }
}