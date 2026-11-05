package com.motiondata.app.ui.screens.recording

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.model.SamplingFrequency
import com.motiondata.app.data.model.SensorFrame
import com.motiondata.app.data.repository.MotionRepository
import com.motiondata.app.sensor.MotionSensorManager
import com.motiondata.app.ui.components.ChartPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class LiveUiState(
    val sessionId: String = "",
    val datasetName: String = "",
    val actionLabel: String = "",
    val targetHz: Int = 50,
    val durationLimitSec: Int = 0,
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val durationMs: Long = 0L,
    val sampleCount: Long = 0L,
    val achievedHz: Double = 0.0,
    val currentFrame: SensorFrame = SensorFrame(),
    val isCompleted: Boolean = false
)

class LiveRecordingViewModel(
    private val sensorManager: MotionSensorManager,
    private val repository: MotionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(LiveUiState())
    val uiState: StateFlow<LiveUiState> = _uiState.asStateFlow()

    // Chart Rolling Buffers (last 120 points for smooth 60fps waveform)
    private val accelHistory = ArrayDeque<ChartPoint>(150)
    private val gyroHistory = ArrayDeque<ChartPoint>(150)

    private val _accelPoints = MutableStateFlow<List<ChartPoint>>(emptyList())
    val accelPoints: StateFlow<List<ChartPoint>> = _accelPoints.asStateFlow()

    private val _gyroPoints = MutableStateFlow<List<ChartPoint>>(emptyList())
    val gyroPoints: StateFlow<List<ChartPoint>> = _gyroPoints.asStateFlow()

    private var durationLimitJob: Job? = null

    init {
        // Wire sensor manager batch flush directly to Room repository
        sensorManager.onFlushBatch = { batch ->
            repository.insertSamplesBatch(batch)
        }

        // Collect live frame updates for waveform rendering
        viewModelScope.launch {
            sensorManager.liveFrame.collect { frame ->
                _uiState.value = _uiState.value.copy(currentFrame = frame)

                val ax = frame.accelX ?: 0f
                val ay = frame.accelY ?: 0f
                val az = frame.accelZ ?: 0f
                if (accelHistory.size >= 120) accelHistory.removeFirst()
                accelHistory.add(ChartPoint(ax, ay, az))
                _accelPoints.value = accelHistory.toList()

                val gx = frame.gyroX ?: 0f
                val gy = frame.gyroY ?: 0f
                val gz = frame.gyroZ ?: 0f
                if (gyroHistory.size >= 120) gyroHistory.removeFirst()
                gyroHistory.add(ChartPoint(gx, gy, gz))
                _gyroPoints.value = gyroHistory.toList()
            }
        }

        viewModelScope.launch {
            sensorManager.elapsedDurationMs.collect { duration ->
                _uiState.value = _uiState.value.copy(durationMs = duration)
            }
        }

        viewModelScope.launch {
            sensorManager.sampleCount.collect { count ->
                _uiState.value = _uiState.value.copy(sampleCount = count)
            }
        }

        viewModelScope.launch {
            sensorManager.achievedFrequencyHz.collect { hz ->
                _uiState.value = _uiState.value.copy(achievedHz = hz)
            }
        }

        viewModelScope.launch {
            sensorManager.isPaused.collect { paused ->
                _uiState.value = _uiState.value.copy(isPaused = paused)
            }
        }
    }

    fun startSession(
        sessionId: String,
        name: String,
        label: String,
        hz: Int,
        durationLimitSec: Int
    ) {
        _uiState.value = _uiState.value.copy(
            sessionId = sessionId,
            datasetName = name,
            actionLabel = label,
            targetHz = hz,
            durationLimitSec = durationLimitSec,
            isRecording = true,
            isPaused = false,
            isCompleted = false
        )

        accelHistory.clear()
        gyroHistory.clear()

        // Insert initial session metadata into Room
        val initialSession = RecordingSessionEntity(
            sessionId = sessionId,
            datasetName = name,
            actionLabel = label,
            actionSlug = com.motiondata.app.data.model.MotionAction.toSlug(label),
            targetSamplingRateHz = hz,
            status = "RECORDING",
            hasAccelerometer = sensorManager.deviceSensorsStatus.accelerometer.isAvailable,
            hasGyroscope = sensorManager.deviceSensorsStatus.gyroscope.isAvailable,
            hasRotationVector = sensorManager.deviceSensorsStatus.rotationVector.isAvailable
        )

        viewModelScope.launch {
            repository.saveSession(initialSession)
        }

        val freqEnum = SamplingFrequency.fromHz(hz)
        sensorManager.startRecording(sessionId, label, freqEnum)

        // If duration limit is set, auto-stop when reached
        if (durationLimitSec > 0) {
            durationLimitJob?.cancel()
            durationLimitJob = viewModelScope.launch {
                while (true) {
                    delay(200)
                    if (_uiState.value.durationMs >= durationLimitSec * 1000L) {
                        stopSession()
                        break
                    }
                }
            }
        }
    }

    fun togglePauseResume() {
        if (_uiState.value.isPaused) {
            sensorManager.resumeRecording()
        } else {
            sensorManager.pauseRecording()
        }
    }

    fun stopSession(onFinished: ((String) -> Unit)? = null) {
        durationLimitJob?.cancel()
        val result = sensorManager.stopRecording()

        _uiState.value = _uiState.value.copy(
            isRecording = false,
            isCompleted = true
        )

        viewModelScope.launch {
            // Update session in Room database
            val existing = repository.getSessionById(result.sessionId)
            if (existing != null) {
                val updated = existing.copy(
                    durationMs = result.durationMs,
                    actualSamplingRateHz = result.actualFrequencyHz,
                    totalSampleCount = result.totalSamples,
                    status = "COMPLETED"
                )
                repository.updateSession(updated)
            }
            onFinished?.invoke(result.sessionId)
        }
    }

    override fun onCleared() {
        super.onCleared()
        sensorManager.stopRecording()
    }
}
