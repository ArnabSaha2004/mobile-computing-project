package com.motiondata.app.data.model

/**
 * Standard sampling frequencies supported by MotionData.
 * Android SensorManager expects period in microseconds.
 */
enum class SamplingFrequency(
    val hz: Int,
    val periodMicros: Int,
    val displayName: String,
    val description: String
) {
    HZ_25(25, 40_000, "25 Hz (40 ms)", "Low power, suitable for slow macroscopic gestures"),
    HZ_50(50, 20_000, "50 Hz (20 ms)", "Balanced, standard for human activity recognition"),
    HZ_100(100, 10_000, "100 Hz (10 ms)", "High precision, recommended for robotics imitation learning");

    companion object {
        fun fromHz(hz: Int): SamplingFrequency {
            return entries.find { it.hz == hz } ?: HZ_50
        }
    }
}
