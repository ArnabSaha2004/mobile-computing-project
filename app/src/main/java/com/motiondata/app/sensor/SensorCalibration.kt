package com.motiondata.app.sensor

import kotlin.math.sqrt

/**
 * Utility to calculate sensor bias and noise variance when the device is at rest.
 */
data class CalibrationResult(
    val samplesCollected: Int,
    val accelBiasX: Float,
    val accelBiasY: Float,
    val accelBiasZ: Float,
    val gyroBiasX: Float,
    val gyroBiasY: Float,
    val gyroBiasZ: Float,
    val gyroNoiseStdDev: Float,
    val isStaticAndStable: Boolean
)

class SensorCalibration {
    private val accelXList = mutableListOf<Float>()
    private val accelYList = mutableListOf<Float>()
    private val accelZList = mutableListOf<Float>()
    private val gyroXList = mutableListOf<Float>()
    private val gyroYList = mutableListOf<Float>()
    private val gyroZList = mutableListOf<Float>()

    fun reset() {
        accelXList.clear()
        accelYList.clear()
        accelZList.clear()
        gyroXList.clear()
        gyroYList.clear()
        gyroZList.clear()
    }

    fun addSample(
        ax: Float?, ay: Float?, az: Float?,
        gx: Float?, gy: Float?, gz: Float?
    ) {
        if (ax != null) accelXList.add(ax)
        if (ay != null) accelYList.add(ay)
        if (az != null) accelZList.add(az)
        if (gx != null) gyroXList.add(gx)
        if (gy != null) gyroYList.add(gy)
        if (gz != null) gyroZList.add(gz)
    }

    val sampleCount: Int
        get() = minOf(accelXList.size, gyroXList.size.coerceAtLeast(accelXList.size))

    fun compute(): CalibrationResult {
        val count = accelXList.size
        if (count < 10) {
            return CalibrationResult(count, 0f, 0f, 0f, 0f, 0f, 0f, 0f, false)
        }

        val axMean = accelXList.average().toFloat()
        val ayMean = accelYList.average().toFloat()
        val azMean = accelZList.average().toFloat()

        val gxMean = if (gyroXList.isNotEmpty()) gyroXList.average().toFloat() else 0f
        val gyMean = if (gyroYList.isNotEmpty()) gyroYList.average().toFloat() else 0f
        val gzMean = if (gyroZList.isNotEmpty()) gyroZList.average().toFloat() else 0f

        // Gyro noise variance calculation
        val gyroVariance = if (gyroXList.isNotEmpty()) {
            gyroXList.map { (it - gxMean) * (it - gxMean) }.average().toFloat()
        } else 0f
        val gyroStdDev = sqrt(gyroVariance)

        // Device is stable if gyro standard deviation is low (< 0.15 rad/s)
        val isStatic = gyroStdDev < 0.15f

        return CalibrationResult(
            samplesCollected = count,
            accelBiasX = axMean,
            accelBiasY = ayMean,
            accelBiasZ = azMean,
            gyroBiasX = gxMean,
            gyroBiasY = gyMean,
            gyroBiasZ = gzMean,
            gyroNoiseStdDev = gyroStdDev,
            isStaticAndStable = isStatic
        )
    }
}
