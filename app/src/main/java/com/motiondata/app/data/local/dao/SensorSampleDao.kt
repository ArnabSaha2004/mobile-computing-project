package com.motiondata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.motiondata.app.data.local.entity.SensorSampleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SensorSampleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSamples(samples: List<SensorSampleEntity>)

    @Query("SELECT * FROM sensor_samples WHERE sessionId = :sessionId ORDER BY relativeTimestampSec ASC")
    fun getSamplesForSession(sessionId: String): Flow<List<SensorSampleEntity>>

    @Query("SELECT * FROM sensor_samples WHERE sessionId = :sessionId ORDER BY relativeTimestampSec ASC")
    suspend fun getSamplesForSessionSync(sessionId: String): List<SensorSampleEntity>

    @Query("SELECT * FROM sensor_samples WHERE sessionId = :sessionId ORDER BY relativeTimestampSec ASC LIMIT :limit")
    suspend fun getPreviewSamples(sessionId: String, limit: Int = 25): List<SensorSampleEntity>

    @Query("SELECT COUNT(*) FROM sensor_samples WHERE sessionId = :sessionId")
    suspend fun getSampleCountForSession(sessionId: String): Int

    @Query("DELETE FROM sensor_samples WHERE sessionId = :sessionId")
    suspend fun deleteSamplesForSession(sessionId: String)
}
