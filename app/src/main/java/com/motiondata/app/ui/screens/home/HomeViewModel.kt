package com.motiondata.app.ui.screens.home

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.repository.MotionRepository
import com.motiondata.app.export.CsvExporter
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val repository: MotionRepository
) : ViewModel() {

    val sessionCount: StateFlow<Int> = repository.getSessionCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDurationMs: StateFlow<Long> = repository.getTotalDurationMs()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val totalSamples: StateFlow<Long> = repository.getTotalSampleCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val recentSessions: StateFlow<List<RecordingSessionEntity>> = repository.getRecentSessions(5)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            repository.deleteSession(sessionId)
        }
    }

    fun shareSession(context: Context, session: RecordingSessionEntity) {
        viewModelScope.launch {
            val samples = repository.getSamplesForSessionSync(session.sessionId)
            if (samples.isNotEmpty()) {
                val uri = CsvExporter.createShareableFile(context, session, samples)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "MotionData Dataset - ${session.datasetName}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Motion Dataset"))
            }
        }
    }
}
