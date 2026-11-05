package com.motiondata.app.ui.screens.setup

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motiondata.app.data.model.DeviceSensorsStatus
import com.motiondata.app.data.model.MotionAction
import com.motiondata.app.data.model.SamplingFrequency
import com.motiondata.app.sensor.CalibrationResult
import com.motiondata.app.sensor.MotionSensorManager
import com.motiondata.app.sensor.SensorCalibration
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

data class SetupUiState(
    val sessionId: String = UUID.randomUUID().toString(),
    val datasetName: String = "",
    val selectedAction: MotionAction = MotionAction.PRESETS.first(),
    val customActionText: String = "",
    val description: String = "",
    val samplingFrequency: SamplingFrequency = SamplingFrequency.HZ_50,
    val durationLimitSeconds: Int = 0, // 0 = unlimited
    val sensorsStatus: DeviceSensorsStatus = DeviceSensorsStatus(),
    val isCalibrating: Boolean = false,
    val calibrationProgress: Float = 0f,
    val calibrationResult: CalibrationResult? = null
)

class SetupViewModel(
    private val sensorManager: MotionSensorManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(SetupUiState())
    val uiState: StateFlow<SetupUiState> = _uiState.asStateFlow()

    private val calibrationHelper = SensorCalibration()
    private var calibrationJob: Job? = null

    init {
        val defaultName = generateDefaultDatasetName(_uiState.value.selectedAction.displayName)
        _uiState.value = _uiState.value.copy(
            datasetName = defaultName,
            sensorsStatus = sensorManager.deviceSensorsStatus
        )
    }

    fun updateDatasetName(name: String) {
        _uiState.value = _uiState.value.copy(datasetName = name)
    }

    fun selectAction(action: MotionAction) {
        val newName = generateDefaultDatasetName(if (action.isCustom) _uiState.value.customActionText else action.displayName)
        _uiState.value = _uiState.value.copy(
            selectedAction = action,
            datasetName = newName
        )
    }

    fun updateCustomActionText(text: String) {
        val newName = generateDefaultDatasetName(text)
        _uiState.value = _uiState.value.copy(
            customActionText = text,
            datasetName = newName
        )
    }

    fun updateDescription(desc: String) {
        _uiState.value = _uiState.value.copy(description = desc)
    }

    fun updateSamplingFrequency(freq: SamplingFrequency) {
        _uiState.value = _uiState.value.copy(samplingFrequency = freq)
    }

    fun updateDurationLimit(seconds: Int) {
        _uiState.value = _uiState.value.copy(durationLimitSeconds = seconds)
    }

    fun startCalibration() {
        if (_uiState.value.isCalibrating) return
        calibrationHelper.reset()
        _uiState.value = _uiState.value.copy(
            isCalibrating = true,
            calibrationProgress = 0f,
            calibrationResult = null
        )

        sensorManager.startMonitoring(SamplingFrequency.HZ_50)

        calibrationJob = viewModelScope.launch {
            val totalSteps = 20
            for (step in 1..totalSteps) {
                delay(100)
                val frame = sensorManager.liveFrame.value
                calibrationHelper.addSample(
                    frame.accelX, frame.accelY, frame.accelZ,
                    frame.gyroX, frame.gyroY, frame.gyroZ
                )
                _uiState.value = _uiState.value.copy(
                    calibrationProgress = step.toFloat() / totalSteps
                )
            }
            sensorManager.stopMonitoring()
            val result = calibrationHelper.compute()
            _uiState.value = _uiState.value.copy(
                isCalibrating = false,
                calibrationResult = result
            )
        }
    }

    fun dismissCalibration() {
        calibrationJob?.cancel()
        sensorManager.stopMonitoring()
        _uiState.value = _uiState.value.copy(
            isCalibrating = false,
            calibrationResult = null
        )
    }

    fun getEffectiveActionLabel(): String {
        val action = _uiState.value.selectedAction
        return if (action.isCustom && _uiState.value.customActionText.isNotBlank()) {
            _uiState.value.customActionText.trim()
        } else {
            action.displayName
        }
    }

    private fun generateDefaultDatasetName(action: String): String {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val cleanAction = action.trim().replace("\\s+".toRegex(), "_")
        return if (cleanAction.isNotBlank()) {
            "Motion_${cleanAction}_$timeStamp"
        } else {
            "Motion_Dataset_$timeStamp"
        }
    }
}
