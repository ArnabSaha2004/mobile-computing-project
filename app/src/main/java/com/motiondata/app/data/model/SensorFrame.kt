package com.motiondata.app.data.model

import kotlin.math.sqrt

/**
 * Represents a synchronized snapshot of motion sensor readings at a specific point in time.
 */
data class SensorFrame(
    val timestampNanos: Long = 0L,
    val relativeTimestampSec: Double = 0.0,
    val accelX: Float? = null,
    val accelY: Float? = null,
    val accelZ: Float? = null,
    val gyroX: Float? = null,
    val gyroY: Float? = null,
    val gyroZ: Float? = null,
    val rotX: Float? = null,
    val rotY: Float? = null,
    val rotZ: Float? = null,
    val rotW: Float? = null
) {
    /**
     * Total magnitude of linear acceleration: sqrt(x^2 + y^2 + z^2) in m/s^2.
     */
    val accelMagnitude: Float?
        get() {
            if (accelX == null || accelY == null || accelZ == null) return null
            return sqrt(accelX * accelX + accelY * accelY + accelZ * accelZ)
        }

    /**
     * Total magnitude of angular velocity: sqrt(x^2 + y^2 + z^2) in rad/s.
     */
    val gyroMagnitude: Float?
        get() {
            if (gyroX == null || gyroY == null || gyroZ == null) return null
            return sqrt(gyroX * gyroX + gyroY * gyroY + gyroZ * gyroZ)
        }
}
