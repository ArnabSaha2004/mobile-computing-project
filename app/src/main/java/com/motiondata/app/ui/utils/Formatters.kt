package com.motiondata.app.ui.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {

    fun formatDurationStopwatch(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        val hundredths = (durationMs % 1000) / 10
        return String.format(Locale.US, "%02d:%02d.%02d", minutes, seconds, hundredths)
    }

    fun formatDurationHuman(durationMs: Long): String {
        val totalSeconds = durationMs / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return if (minutes > 0) {
            "${minutes}m ${seconds}s"
        } else {
            "${seconds}s"
        }
    }

    fun formatDate(timestampMs: Long): String {
        val sdf = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
        return sdf.format(Date(timestampMs))
    }

    fun formatFrequency(hz: Double): String {
        return String.format(Locale.US, "%.1f Hz", hz)
    }

    fun formatNumber(count: Long): String {
        return String.format(Locale.US, "%,d", count)
    }

    fun formatSensorValue(value: Float?, decimals: Int = 2): String {
        if (value == null) return "--"
        return String.format(Locale.US, "%.${decimals}f", value)
    }
}
