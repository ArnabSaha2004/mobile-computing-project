package com.motiondata.app.data.model

/**
 * Detailed hardware information and availability status for device sensors.
 */
data class SensorSpec(
    val isAvailable: Boolean = false,
    val name: String = "Not Available",
    val vendor: String = "N/A",
    val version: Int = 0,
    val maxRange: Float = 0f,
    val resolution: Float = 0f,
    val powerMa: Float = 0f,
    val minDelayUs: Int = 0,
    val maxDelayUs: Int = 0
) {
    val maxSupportedFrequencyHz: Double
        get() = if (minDelayUs > 0) 1_000_000.0 / minDelayUs else 0.0
}

data class DeviceSensorsStatus(
    val accelerometer: SensorSpec = SensorSpec(),
    val gyroscope: SensorSpec = SensorSpec(),
    val rotationVector: SensorSpec = SensorSpec()
) {
    val isRoboticsReady: Boolean
        get() = accelerometer.isAvailable && gyroscope.isAvailable

    val sensorStatusSummary: String
        get() = when {
            accelerometer.isAvailable && gyroscope.isAvailable && rotationVector.isAvailable ->
                "Full 9-DOF Capture (Accel + Gyro + Rotation Vector)"
            accelerometer.isAvailable && gyroscope.isAvailable ->
                "6-DOF Capture (Accel + Gyro)"
            accelerometer.isAvailable ->
                "Limited 3-DOF Capture (Accel only, no Gyroscope)"
            else ->
                "Critical: Required motion sensors unavailable"
        }
}
