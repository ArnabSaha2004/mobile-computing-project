package com.motiondata.app.sensor

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import com.motiondata.app.data.local.entity.SensorSampleEntity
import com.motiondata.app.data.model.DeviceSensorsStatus
import com.motiondata.app.data.model.SamplingFrequency
import com.motiondata.app.data.model.SensorFrame
import com.motiondata.app.data.model.SensorSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong

class MotionSensorManager(
    context: Context,
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Default)
) : SensorEventListener {

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val accelerometer: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    private val gyroscope: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    private val rotationVector: Sensor? = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR)

    // Dedicated background thread for sensor processing to avoid dropping frames
    private var sensorThread: HandlerThread? = null
    private var sensorHandler: Handler? = null

    // Live frame state for UI
    private val _liveFrame = MutableStateFlow(SensorFrame())
    val liveFrame: StateFlow<SensorFrame> = _liveFrame.asStateFlow()

    // Recording State
    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _isPaused = MutableStateFlow(false)
    val isPaused: StateFlow<Boolean> = _isPaused.asStateFlow()

    private val _sampleCount = MutableStateFlow(0L)
    val sampleCount: StateFlow<Long> = _sampleCount.asStateFlow()

    private val _achievedFrequencyHz = MutableStateFlow(0.0)
    val achievedFrequencyHz: StateFlow<Double> = _achievedFrequencyHz.asStateFlow()

    private val _elapsedDurationMs = MutableStateFlow(0L)
    val elapsedDurationMs: StateFlow<Long> = _elapsedDurationMs.asStateFlow()

    // Recording internals
    private var activeSessionId: String = ""
    private var activeLabel: String = ""
    private var targetFrequency: SamplingFrequency = SamplingFrequency.HZ_50
    private var startSensorTimestampNanos: Long = 0L
    private var totalPausedDurationNanos: Long = 0L
    private var pauseStartNanos: Long = 0L
    private var isCurrentlyPaused = AtomicBoolean(false)

    // Rate calculation tracking
    private var recordingStartTimeRealtimeMs: Long = 0L
    private var pauseStartTimeRealtimeMs: Long = 0L
    private var totalPausedRealtimeMs: Long = 0L
    private val sampleCounter = AtomicLong(0)

    // Rolling sample latching for synchronized frame creation
    @Volatile private var latestAx: Float? = null
    @Volatile private var latestAy: Float? = null
    @Volatile private var latestAz: Float? = null

    @Volatile private var latestGx: Float? = null
    @Volatile private var latestGy: Float? = null
    @Volatile private var latestGz: Float? = null

    @Volatile private var latestRx: Float? = null
    @Volatile private var latestRy: Float? = null
    @Volatile private var latestRz: Float? = null
    @Volatile private var latestRw: Float? = null

    // Batch buffer for database persistence
    private val sampleBatchBuffer = mutableListOf<SensorSampleEntity>()
    private val batchLock = Any()
    private val BATCH_SIZE = 50
    private var lastBatchFlushTimeMs = 0L

    var onFlushBatch: (suspend (List<SensorSampleEntity>) -> Unit)? = null

    val deviceSensorsStatus: DeviceSensorsStatus by lazy {
        DeviceSensorsStatus(
            accelerometer = accelerometer?.let {
                SensorSpec(
                    isAvailable = true,
                    name = it.name,
                    vendor = it.vendor,
                    version = it.version,
                    maxRange = it.maximumRange,
                    resolution = it.resolution,
                    powerMa = it.power,
                    minDelayUs = it.minDelay,
                    maxDelayUs = it.maxDelay
                )
            } ?: SensorSpec(),
            gyroscope = gyroscope?.let {
                SensorSpec(
                    isAvailable = true,
                    name = it.name,
                    vendor = it.vendor,
                    version = it.version,
                    maxRange = it.maximumRange,
                    resolution = it.resolution,
                    powerMa = it.power,
                    minDelayUs = it.minDelay,
                    maxDelayUs = it.maxDelay
                )
            } ?: SensorSpec(),
            rotationVector = rotationVector?.let {
                SensorSpec(
                    isAvailable = true,
                    name = it.name,
                    vendor = it.vendor,
                    version = it.version,
                    maxRange = it.maximumRange,
                    resolution = it.resolution,
                    powerMa = it.power,
                    minDelayUs = it.minDelay,
                    maxDelayUs = it.maxDelay
                )
            } ?: SensorSpec()
        )
    }

    /**
     * Start continuous preview monitoring of sensors (without recording to database).
     */
    fun startMonitoring(frequency: SamplingFrequency = SamplingFrequency.HZ_50) {
        stopMonitoring()

        val thread = HandlerThread("MotionSensorThread").apply { start() }
        sensorThread = thread
        val handler = Handler(thread.looper)
        sensorHandler = handler

        accelerometer?.let {
            sensorManager.registerListener(this, it, frequency.periodMicros, handler)
        }
        gyroscope?.let {
            sensorManager.registerListener(this, it, frequency.periodMicros, handler)
        }
        rotationVector?.let {
            sensorManager.registerListener(this, it, frequency.periodMicros, handler)
        }
    }

    /**
     * Stop preview monitoring and release resources.
     */
    fun stopMonitoring() {
        try {
            sensorManager.unregisterListener(this)
        } catch (_: Exception) {}

        sensorThread?.quitSafely()
        sensorThread = null
        sensorHandler = null
    }

    /**
     * Begin an active recording session.
     */
    fun startRecording(
        sessionId: String,
        label: String,
        frequency: SamplingFrequency = SamplingFrequency.HZ_50
    ) {
        activeSessionId = sessionId
        activeLabel = label
        targetFrequency = frequency

        startSensorTimestampNanos = 0L
        totalPausedDurationNanos = 0L
        pauseStartNanos = 0L
        sampleCounter.set(0)
        _sampleCount.value = 0L
        _achievedFrequencyHz.value = 0.0
        _elapsedDurationMs.value = 0L

        isCurrentlyPaused.set(false)
        _isPaused.value = false
        _isRecording.value = true

        recordingStartTimeRealtimeMs = SystemClock.elapsedRealtime()
        pauseStartTimeRealtimeMs = 0L
        totalPausedRealtimeMs = 0L
        lastBatchFlushTimeMs = SystemClock.elapsedRealtime()

        synchronized(batchLock) {
            sampleBatchBuffer.clear()
        }

        startMonitoring(frequency)
    }

    /**
     * Pause the active recording session.
     */
    fun pauseRecording() {
        if (!_isRecording.value || isCurrentlyPaused.get()) return
        isCurrentlyPaused.set(true)
        _isPaused.value = true
        pauseStartNanos = System.nanoTime()
        pauseStartTimeRealtimeMs = SystemClock.elapsedRealtime()
        flushBatch(force = true)
    }

    /**
     * Resume the active recording session.
     */
    fun resumeRecording() {
        if (!_isRecording.value || !isCurrentlyPaused.get()) return
        val nowNanos = System.nanoTime()
        val nowRealtimeMs = SystemClock.elapsedRealtime()

        if (pauseStartNanos > 0) {
            totalPausedDurationNanos += (nowNanos - pauseStartNanos)
        }
        if (pauseStartTimeRealtimeMs > 0) {
            totalPausedRealtimeMs += (nowRealtimeMs - pauseStartTimeRealtimeMs)
        }

        pauseStartNanos = 0L
        pauseStartTimeRealtimeMs = 0L
        isCurrentlyPaused.set(false)
        _isPaused.value = false
    }

    /**
     * Stop active recording and return summary statistics.
     */
    fun stopRecording(): RecordingSummaryResult {
        _isRecording.value = false
        _isPaused.value = false
        isCurrentlyPaused.set(false)

        val totalDurationMs = calculateCurrentDurationMs()
        _elapsedDurationMs.value = totalDurationMs

        flushBatch(force = true)
        stopMonitoring()

        val finalSamples = sampleCounter.get()
        val finalRate = if (totalDurationMs > 0) {
            (finalSamples * 1000.0) / totalDurationMs
        } else 0.0

        return RecordingSummaryResult(
            sessionId = activeSessionId,
            durationMs = totalDurationMs,
            totalSamples = finalSamples,
            actualFrequencyHz = finalRate
        )
    }

    private fun calculateCurrentDurationMs(): Long {
        if (recordingStartTimeRealtimeMs == 0L) return 0L
        val now = SystemClock.elapsedRealtime()
        val currentPause = if (isCurrentlyPaused.get() && pauseStartTimeRealtimeMs > 0) {
            now - pauseStartTimeRealtimeMs
        } else 0L
        return (now - recordingStartTimeRealtimeMs - totalPausedRealtimeMs - currentPause).coerceAtLeast(0L)
    }

    override fun onSensorChanged(event: SensorEvent) {
        val nowEventNanos = event.timestamp

        when (event.sensor.type) {
            Sensor.TYPE_ACCELEROMETER -> {
                latestAx = event.values[0]
                latestAy = event.values[1]
                latestAz = event.values[2]
            }
            Sensor.TYPE_GYROSCOPE -> {
                latestGx = event.values[0]
                latestGy = event.values[1]
                latestGz = event.values[2]
            }
            Sensor.TYPE_ROTATION_VECTOR -> {
                latestRx = event.values[0]
                latestRy = event.values[1]
                latestRz = event.values[2]
                latestRw = if (event.values.size > 3) event.values[3] else 1.0f
            }
        }

        // Compute relative timestamp
        val relSec: Double
        if (_isRecording.value && !isCurrentlyPaused.get()) {
            if (startSensorTimestampNanos == 0L) {
                startSensorTimestampNanos = nowEventNanos
            }
            val activeNanos = (nowEventNanos - startSensorTimestampNanos) - totalPausedDurationNanos
            relSec = (activeNanos.coerceAtLeast(0L)) / 1_000_000_000.0
        } else {
            relSec = 0.0
        }

        val frame = SensorFrame(
            timestampNanos = nowEventNanos,
            relativeTimestampSec = relSec,
            accelX = latestAx,
            accelY = latestAy,
            accelZ = latestAz,
            gyroX = latestGx,
            gyroY = latestGy,
            gyroZ = latestGz,
            rotX = latestRx,
            rotY = latestRy,
            rotZ = latestRz,
            rotW = latestRw
        )

        _liveFrame.value = frame

        // If recording and not paused, process sample ingestion
        if (_isRecording.value && !isCurrentlyPaused.get()) {
            val count = sampleCounter.incrementAndGet()
            _sampleCount.value = count

            val currentDurationMs = calculateCurrentDurationMs()
            _elapsedDurationMs.value = currentDurationMs

            if (currentDurationMs > 300) {
                _achievedFrequencyHz.value = (count * 1000.0) / currentDurationMs
            }

            val entity = SensorSampleEntity(
                sessionId = activeSessionId,
                relativeTimestampSec = relSec,
                accelX = latestAx,
                accelY = latestAy,
                accelZ = latestAz,
                gyroX = latestGx,
                gyroY = latestGy,
                gyroZ = latestGz,
                rotX = latestRx,
                rotY = latestRy,
                rotZ = latestRz,
                rotW = latestRw,
                label = activeLabel
            )

            synchronized(batchLock) {
                sampleBatchBuffer.add(entity)
            }

            val nowRealtime = SystemClock.elapsedRealtime()
            if (sampleBatchBuffer.size >= BATCH_SIZE || (nowRealtime - lastBatchFlushTimeMs > 400)) {
                flushBatch(force = false)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Sensors accuracy changes can be tracked if needed for quality metrics
    }

    private fun flushBatch(force: Boolean) {
        val toFlush: List<SensorSampleEntity>
        synchronized(batchLock) {
            if (sampleBatchBuffer.isEmpty()) return
            if (!force && sampleBatchBuffer.size < BATCH_SIZE && (SystemClock.elapsedRealtime() - lastBatchFlushTimeMs < 400)) {
                return
            }
            toFlush = ArrayList(sampleBatchBuffer)
            sampleBatchBuffer.clear()
            lastBatchFlushTimeMs = SystemClock.elapsedRealtime()
        }

        onFlushBatch?.let { callback ->
            scope.launch(Dispatchers.IO) {
                try {
                    callback(toFlush)
                } catch (_: Exception) {}
            }
        }
    }
}

data class RecordingSummaryResult(
    val sessionId: String,
    val durationMs: Long,
    val totalSamples: Long,
    val actualFrequencyHz: Double
)
