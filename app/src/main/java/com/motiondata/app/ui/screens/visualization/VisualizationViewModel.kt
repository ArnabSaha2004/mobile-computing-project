package com.motiondata.app.ui.screens.visualization

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.local.entity.SensorSampleEntity
import com.motiondata.app.data.repository.MotionRepository
import com.motiondata.app.ui.components.SensorTypeToDisplay
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class VisualizationUiState(
    val session: RecordingSessionEntity? = null,
    val samples: List<SensorSampleEntity> = emptyList(),
    val sensorType: SensorTypeToDisplay = SensorTypeToDisplay.ACCELEROMETER,
    val showX: Boolean = true,
    val showY: Boolean = true,
    val showZ: Boolean = true,
    val selectedTimestampSec: Double = 0.0,
    val isPlaying: Boolean = false,
    val playbackSpeed: Float = 1.0f,
    val isLoading: Boolean = true
)

class VisualizationViewModel(
    private val repository: MotionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(VisualizationUiState())
    val uiState: StateFlow<VisualizationUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            val session = repository.getSessionById(sessionId)
            val samples = repository.getSamplesForSessionSync(sessionId)

            _uiState.value = _uiState.value.copy(
                session = session,
                samples = samples,
                selectedTimestampSec = 0.0,
                isLoading = false
            )
        }
    }

    fun setSensorType(type: SensorTypeToDisplay) {
        _uiState.value = _uiState.value.copy(sensorType = type)
    }

    fun toggleX() {
        _uiState.value = _uiState.value.copy(showX = !_uiState.value.showX)
    }

    fun toggleY() {
        _uiState.value = _uiState.value.copy(showY = !_uiState.value.showY)
    }

    fun toggleZ() {
        _uiState.value = _uiState.value.copy(showZ = !_uiState.value.showZ)
    }

    fun setTimestamp(sec: Double) {
        val total = _uiState.value.samples.lastOrNull()?.relativeTimestampSec ?: 0.0
        val clamped = sec.coerceIn(0.0, total)
        _uiState.value = _uiState.value.copy(selectedTimestampSec = clamped)
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pausePlayback()
        } else {
            startPlayback()
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(playbackSpeed = speed)
    }

    private fun startPlayback() {
        val totalSec = _uiState.value.samples.lastOrNull()?.relativeTimestampSec ?: return
        if (totalSec <= 0.0) return

        _uiState.value = _uiState.value.copy(isPlaying = true)

        // If at the end, restart from beginning
        if (_uiState.value.selectedTimestampSec >= totalSec - 0.05) {
            _uiState.value = _uiState.value.copy(selectedTimestampSec = 0.0)
        }

        playbackJob?.cancel()
        playbackJob = viewModelScope.launch {
            val stepMs = 33L // ~30 fps stepping
            while (_uiState.value.isPlaying) {
                delay(stepMs)
                val advanceSec = (stepMs / 1000.0) * _uiState.value.playbackSpeed
                val nextTime = _uiState.value.selectedTimestampSec + advanceSec

                if (nextTime >= totalSec) {
                    _uiState.value = _uiState.value.copy(
                        selectedTimestampSec = totalSec,
                        isPlaying = false
                    )
                    break
                } else {
                    _uiState.value = _uiState.value.copy(selectedTimestampSec = nextTime)
                }
            }
        }
    }

    private fun pausePlayback() {
        playbackJob?.cancel()
        _uiState.value = _uiState.value.copy(isPlaying = false)
    }

    override fun onCleared() {
        super.onCleared()
        playbackJob?.cancel()
    }
}
