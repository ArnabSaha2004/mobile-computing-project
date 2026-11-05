package com.motiondata.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recording_sessions")
data class RecordingSessionEntity(
    @PrimaryKey
    val sessionId: String,
    val datasetName: String,
    val actionLabel: String,
    val actionSlug: String,
    val description: String = "",
    val createdAtTimestamp: Long = System.currentTimeMillis(),
    val durationMs: Long = 0L,
    val targetSamplingRateHz: Int = 50,
    val actualSamplingRateHz: Double = 0.0,
    val totalSampleCount: Long = 0L,
    val status: String = "COMPLETED", // RECORDING, PAUSED, COMPLETED
    val hasAccelerometer: Boolean = true,
    val hasGyroscope: Boolean = true,
    val hasRotationVector: Boolean = false
)
