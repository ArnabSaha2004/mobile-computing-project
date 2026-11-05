package com.motiondata.app.ui.screens.history

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.repository.MotionRepository
import com.motiondata.app.export.CsvExporter
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    private val repository: MotionRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val sessions: StateFlow<List<RecordingSessionEntity>> = _searchQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                repository.getAllSessions()
            } else {
                repository.searchSessions(query.trim())
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun renameSession(sessionId: String, newName: String) {
        if (newName.isBlank()) return
        viewModelScope.launch {
            repository.renameSession(sessionId, newName.trim())
        }
    }

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

    fun exportSaf(context: Context, session: RecordingSessionEntity, uri: Uri) {
        viewModelScope.launch {
            val samples = repository.getSamplesForSessionSync(session.sessionId)
            context.contentResolver.openOutputStream(uri)?.use { os ->
                CsvExporter.writeCsv(samples, os)
            }
        }
    }
}
