package com.motiondata.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSession(session: RecordingSessionEntity)

    @Update
    suspend fun updateSession(session: RecordingSessionEntity)

    @Query("SELECT * FROM recording_sessions ORDER BY createdAtTimestamp DESC")
    fun getAllSessions(): Flow<List<RecordingSessionEntity>>

    @Query("SELECT * FROM recording_sessions ORDER BY createdAtTimestamp DESC LIMIT :limit")
    fun getRecentSessions(limit: Int = 5): Flow<List<RecordingSessionEntity>>

    @Query("SELECT * FROM recording_sessions WHERE sessionId = :sessionId")
    suspend fun getSessionById(sessionId: String): RecordingSessionEntity?

    @Query("""
        SELECT * FROM recording_sessions 
        WHERE datasetName LIKE '%' || :query || '%' 
           OR actionLabel LIKE '%' || :query || '%' 
           OR description LIKE '%' || :query || '%'
        ORDER BY createdAtTimestamp DESC
    """)
    fun searchSessions(query: String): Flow<List<RecordingSessionEntity>>

    @Query("UPDATE recording_sessions SET datasetName = :newName WHERE sessionId = :sessionId")
    suspend fun renameSession(sessionId: String, newName: String)

    @Query("DELETE FROM recording_sessions WHERE sessionId = :sessionId")
    suspend fun deleteSession(sessionId: String)

    @Query("SELECT COUNT(*) FROM recording_sessions")
    fun getSessionCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(durationMs), 0) FROM recording_sessions")
    fun getTotalDurationMs(): Flow<Long>

    @Query("SELECT COALESCE(SUM(totalSampleCount), 0) FROM recording_sessions")
    fun getTotalSampleCount(): Flow<Long>
}
