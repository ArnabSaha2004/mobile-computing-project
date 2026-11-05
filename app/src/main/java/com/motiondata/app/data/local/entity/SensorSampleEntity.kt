package com.motiondata.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sensor_samples",
    foreignKeys = [
        ForeignKey(
            entity = RecordingSessionEntity::class,
            parentColumns = ["sessionId"],
            childColumns = ["sessionId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["sessionId"]),
        Index(value = ["sessionId", "relativeTimestampSec"])
    ]
)
data class SensorSampleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "sessionId")
    val sessionId: String,
    val relativeTimestampSec: Double,
    val accelX: Float?,
    val accelY: Float?,
    val accelZ: Float?,
    val gyroX: Float?,
    val gyroY: Float?,
    val gyroZ: Float?,
    val rotX: Float?,
    val rotY: Float?,
    val rotZ: Float?,
    val rotW: Float?,
    val label: String
)
