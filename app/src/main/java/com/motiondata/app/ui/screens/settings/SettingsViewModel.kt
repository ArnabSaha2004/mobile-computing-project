package com.motiondata.app.ui.screens.settings

import androidx.lifecycle.ViewModel
import com.motiondata.app.data.model.DeviceSensorsStatus
import com.motiondata.app.data.model.SamplingFrequency
import com.motiondata.app.sensor.MotionSensorManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val defaultFrequency: SamplingFrequency = SamplingFrequency.HZ_50,
    val graphFps: Int = 60,
    val csvDecimals: Int = 4,
    val sensorsStatus: DeviceSensorsStatus = DeviceSensorsStatus()
)

class SettingsViewModel(
    sensorManager: MotionSensorManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(sensorsStatus = sensorManager.deviceSensorsStatus)
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun updateDefaultFrequency(freq: SamplingFrequency) {
        _uiState.value = _uiState.value.copy(defaultFrequency = freq)
    }

    fun updateGraphFps(fps: Int) {
        _uiState.value = _uiState.value.copy(graphFps = fps)
    }

    fun updateCsvDecimals(decimals: Int) {
        _uiState.value = _uiState.value.copy(csvDecimals = decimals)
    }
}
