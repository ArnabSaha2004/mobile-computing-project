package com.motiondata.app.data.repository

import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.local.entity.SensorSampleEntity
import kotlinx.coroutines.flow.Flow

interface MotionRepository {
    fun getAllSessions(): Flow<List<RecordingSessionEntity>>
    fun getRecentSessions(limit: Int = 5): Flow<List<RecordingSessionEntity>>
    fun searchSessions(query: String): Flow<List<RecordingSessionEntity>>
    suspend fun getSessionById(sessionId: String): RecordingSessionEntity?
    suspend fun saveSession(session: RecordingSessionEntity)
    suspend fun updateSession(session: RecordingSessionEntity)
    suspend fun renameSession(sessionId: String, newName: String)
    suspend fun deleteSession(sessionId: String)

    fun getSessionCount(): Flow<Int>
    fun getTotalDurationMs(): Flow<Long>
    fun getTotalSampleCount(): Flow<Long>

    suspend fun insertSamplesBatch(samples: List<SensorSampleEntity>)
    fun getSamplesForSession(sessionId: String): Flow<List<SensorSampleEntity>>
    suspend fun getSamplesForSessionSync(sessionId: String): List<SensorSampleEntity>
    suspend fun getPreviewSamples(sessionId: String, limit: Int = 25): List<SensorSampleEntity>
}
