package com.motiondata.app

import com.motiondata.app.sensor.SensorCalibration
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SensorCalibrationTest {

    @Test
    fun testCalibrationComputationOnStaticSensor() {
        val calibration = SensorCalibration()
        for (i in 0 until 50) {
            calibration.addSample(
                ax = 0.05f, ay = 0.02f, az = 9.80f,
                gx = 0.001f, gy = -0.002f, gz = 0.001f
            )
        }

        val result = calibration.compute()
        assertEquals(50, result.samplesCollected)
        assertEquals(0.05f, result.accelBiasX, 0.001f)
        assertEquals(0.02f, result.accelBiasY, 0.001f)
        assertEquals(9.80f, result.accelBiasZ, 0.001f)
        assertTrue(result.gyroNoiseStdDev < 0.01f)
        assertTrue("Static sensor should be marked as stable", result.isStaticAndStable)
    }

    @Test
    fun testCalibrationRejectsMovingSensor() {
        val calibration = SensorCalibration()
        for (i in 0 until 50) {
            val noisyVal = if (i % 2 == 0) 1.5f else -1.5f
            calibration.addSample(
                ax = 2f, ay = 1f, az = 8f,
                gx = noisyVal, gy = noisyVal, gz = noisyVal
            )
        }

        val result = calibration.compute()
        assertFalse("Sensor with high gyro variance should NOT be marked stable", result.isStaticAndStable)
    }
}
