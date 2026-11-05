package com.motiondata.app.data.repository

import com.motiondata.app.data.local.dao.SensorSampleDao
import com.motiondata.app.data.local.dao.SessionDao
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.local.entity.SensorSampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MotionRepositoryImpl(
    private val sessionDao: SessionDao,
    private val sampleDao: SensorSampleDao
) : MotionRepository {

    override fun getAllSessions(): Flow<List<RecordingSessionEntity>> =
        sessionDao.getAllSessions()

    override fun getRecentSessions(limit: Int): Flow<List<RecordingSessionEntity>> =
        sessionDao.getRecentSessions(limit)

    override fun searchSessions(query: String): Flow<List<RecordingSessionEntity>> =
        sessionDao.searchSessions(query)

    override suspend fun getSessionById(sessionId: String): RecordingSessionEntity? =
        withContext(Dispatchers.IO) {
            sessionDao.getSessionById(sessionId)
        }

    override suspend fun saveSession(session: RecordingSessionEntity) =
        withContext(Dispatchers.IO) {
            sessionDao.insertSession(session)
        }

    override suspend fun updateSession(session: RecordingSessionEntity) =
        withContext(Dispatchers.IO) {
            sessionDao.updateSession(session)
        }

    override suspend fun renameSession(sessionId: String, newName: String) =
        withContext(Dispatchers.IO) {
            sessionDao.renameSession(sessionId, newName)
        }

    override suspend fun deleteSession(sessionId: String) =
        withContext(Dispatchers.IO) {
            sampleDao.deleteSamplesForSession(sessionId)
            sessionDao.deleteSession(sessionId)
        }

    override fun getSessionCount(): Flow<Int> =
        sessionDao.getSessionCount()

    override fun getTotalDurationMs(): Flow<Long> =
        sessionDao.getTotalDurationMs()

    override fun getTotalSampleCount(): Flow<Long> =
        sessionDao.getTotalSampleCount()

    override suspend fun insertSamplesBatch(samples: List<SensorSampleEntity>) =
        withContext(Dispatchers.IO) {
            if (samples.isNotEmpty()) {
                sampleDao.insertSamples(samples)
            }
        }

    override fun getSamplesForSession(sessionId: String): Flow<List<SensorSampleEntity>> =
        sampleDao.getSamplesForSession(sessionId)

    override suspend fun getSamplesForSessionSync(sessionId: String): List<SensorSampleEntity> =
        withContext(Dispatchers.IO) {
            sampleDao.getSamplesForSessionSync(sessionId)
        }

    override suspend fun getPreviewSamples(sessionId: String, limit: Int): List<SensorSampleEntity> =
        withContext(Dispatchers.IO) {
            sampleDao.getPreviewSamples(sessionId, limit)
        }
}
