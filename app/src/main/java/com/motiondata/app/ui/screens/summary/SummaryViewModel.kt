package com.motiondata.app.ui.screens.summary

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.local.entity.SensorSampleEntity
import com.motiondata.app.data.repository.MotionRepository
import com.motiondata.app.export.CsvExporter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SummaryUiState(
    val session: RecordingSessionEntity? = null,
    val previewSamples: List<SensorSampleEntity> = emptyList(),
    val isExporting: Boolean = false,
    val exportSuccessMessage: String? = null,
    val errorMessage: String? = null
)

class SummaryViewModel(
    private val repository: MotionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SummaryUiState())
    val uiState: StateFlow<SummaryUiState> = _uiState.asStateFlow()

    fun loadSession(sessionId: String) {
        viewModelScope.launch {
            val session = repository.getSessionById(sessionId)
            val previews = repository.getPreviewSamples(sessionId, limit = 30)
            _uiState.value = _uiState.value.copy(
                session = session,
                previewSamples = previews
            )
        }
    }

    fun exportToSafUri(context: Context, uri: Uri) {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val samples = repository.getSamplesForSessionSync(session.sessionId)
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    CsvExporter.writeCsv(samples, os)
                }
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    exportSuccessMessage = "Dataset successfully exported to file."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isExporting = false,
                    errorMessage = "Export failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun shareCsv(context: Context) {
        viewModelScope.launch {
            val session = _uiState.value.session ?: return@launch
            try {
                val samples = repository.getSamplesForSessionSync(session.sessionId)
                val uri = CsvExporter.createShareableFile(context, session, samples)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/csv"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_SUBJECT, "MotionData Dataset - ${session.datasetName}")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                context.startActivity(Intent.createChooser(shareIntent, "Share Motion Dataset"))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    errorMessage = "Share failed: ${e.localizedMessage}"
                )
            }
        }
    }

    fun deleteSession(onDeleted: () -> Unit) {
        val session = _uiState.value.session ?: return
        viewModelScope.launch {
            repository.deleteSession(session.sessionId)
            onDeleted()
        }
    }

    fun clearMessages() {
        _uiState.value = _uiState.value.copy(
            exportSuccessMessage = null,
            errorMessage = null
        )
    }
}
