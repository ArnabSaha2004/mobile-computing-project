package com.motiondata.app.ui.screens.history

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.ui.components.RoboticsTopBar
import com.motiondata.app.ui.screens.home.MetricChip
import com.motiondata.app.ui.theme.AxisXColor
import com.motiondata.app.ui.theme.CharcoalBackground
import com.motiondata.app.ui.theme.CharcoalCard
import com.motiondata.app.ui.theme.CharcoalCardBorder
import com.motiondata.app.ui.theme.CyanNeon
import com.motiondata.app.ui.theme.TextPrimary
import com.motiondata.app.ui.theme.TextSecondary
import com.motiondata.app.ui.theme.TextTertiary
import com.motiondata.app.ui.utils.Formatters

@Composable
fun HistoryScreen(
    viewModel: HistoryViewModel,
    onBackClick: () -> Unit,
    onNavigateToVisualization: (String) -> Unit
) {
    val context = LocalContext.current
    val searchQuery by viewModel.searchQuery.collectAsState()
    val sessions by viewModel.sessions.collectAsState()

    var sessionToRename by remember { mutableStateOf<RecordingSessionEntity?>(null) }
    var renameText by remember { mutableStateOf("") }

    var sessionToDelete by remember { mutableStateOf<RecordingSessionEntity?>(null) }
    var sessionToExport by remember { mutableStateOf<RecordingSessionEntity?>(null) }

    // SAF file creation launcher
    val safExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        if (uri != null && sessionToExport != null) {
            viewModel.exportSaf(context, sessionToExport!!, uri)
            sessionToExport = null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        RoboticsTopBar(
            title = "Dataset History",
            subtitle = "${sessions.size} Recorded Demonstrations",
            onBackClick = onBackClick
        )

        // Search Bar
        Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                placeholder = { Text("Search datasets by name or action label...") },
                modifier = Modifier.fillMaxWidth(),
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = TextTertiary
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "Clear", tint = TextTertiary)
                        }
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = CyanNeon,
                    unfocusedBorderColor = CharcoalCardBorder,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedContainerColor = CharcoalCard,
                    unfocusedContainerColor = CharcoalCard
                ),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (sessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (searchQuery.isNotBlank()) "No datasets matching \"$searchQuery\"" else "No recordings in history",
                    color = TextSecondary
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 4.dp, bottom = 40.dp)
            ) {
                items(sessions, key = { it.sessionId }) { session ->
                    HistorySessionCard(
                        session = session,
                        onInspect = { onNavigateToVisualization(session.sessionId) },
                        onRename = {
                            sessionToRename = session
                            renameText = session.datasetName
                        },
                        onShare = { viewModel.shareSession(context, session) },
                        onExportSaf = {
                            sessionToExport = session
                            safExportLauncher.launch("${session.datasetName}.csv")
                        },
                        onDelete = { sessionToDelete = session }
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }

    // Rename Dialog
    sessionToRename?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToRename = null },
            title = { Text("Rename Dataset", color = TextPrimary) },
            text = {
                OutlinedTextField(
                    value = renameText,
                    onValueChange = { renameText = it },
                    label = { Text("Dataset Name") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = CharcoalCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CharcoalCard,
                        unfocusedContainerColor = CharcoalCard
                    ),
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.renameSession(session.sessionId, renameText)
                        sessionToRename = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = CharcoalBackground)
                ) {
                    Text("Rename")
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToRename = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CharcoalCard,
            shape = RoundedCornerShape(14.dp)
        )
    }

    // Delete Confirmation Dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            title = { Text("Delete Dataset?", color = TextPrimary) },
            text = { Text("Permanently delete \"${session.datasetName}\"? All samples will be deleted from the database.", color = TextSecondary) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteSession(session.sessionId)
                        sessionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = AxisXColor)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CharcoalCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
fun HistorySessionCard(
    session: RecordingSessionEntity,
    onInspect: () -> Unit,
    onRename: () -> Unit,
    onShare: () -> Unit,
    onExportSaf: () -> Unit,
    onDelete: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CharcoalCard)
            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onInspect)
            .padding(14.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = session.datasetName,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = Formatters.formatDate(session.createdAtTimestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(CyanNeon.copy(alpha = 0.12f))
                        .border(1.dp, CyanNeon.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = session.actionLabel.uppercase(),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = CyanNeon
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Metrics row: Duration, Samples, Rate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                MetricChip(label = "Duration", value = Formatters.formatDurationHuman(session.durationMs))
                MetricChip(label = "Samples", value = Formatters.formatNumber(session.totalSampleCount))
                MetricChip(label = "Rate", value = Formatters.formatFrequency(session.actualSamplingRateHz))

                Spacer(modifier = Modifier.weight(1f))

                IconButton(onClick = onRename, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Edit, contentDescription = "Rename", tint = TextSecondary, modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onExportSaf, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Download, contentDescription = "Export CSV", tint = TextSecondary, modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onShare, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondary, modifier = Modifier.size(17.dp))
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(30.dp)) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = AxisXColor.copy(alpha = 0.8f), modifier = Modifier.size(17.dp))
                }
            }
        }
    }
}
