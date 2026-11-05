package com.motiondata.app.ui.screens.visualization

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motiondata.app.ui.components.InteractiveHistoricalChart
import com.motiondata.app.ui.components.RoboticsTopBar
import com.motiondata.app.ui.components.SensorTypeToDisplay
import com.motiondata.app.ui.theme.AxisXColor
import com.motiondata.app.ui.theme.AxisYColor
import com.motiondata.app.ui.theme.AxisZColor
import com.motiondata.app.ui.theme.BlueElectric
import com.motiondata.app.ui.theme.CharcoalBackground
import com.motiondata.app.ui.theme.CharcoalCard
import com.motiondata.app.ui.theme.CharcoalCardBorder
import com.motiondata.app.ui.theme.CharcoalSurfaceVariant
import com.motiondata.app.ui.theme.CyanNeon
import com.motiondata.app.ui.theme.TextPrimary
import com.motiondata.app.ui.theme.TextSecondary
import com.motiondata.app.ui.theme.TextTertiary
import com.motiondata.app.ui.utils.Formatters

@Composable
fun VisualizationScreen(
    viewModel: VisualizationViewModel,
    sessionId: String,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionId) {
        viewModel.loadSession(sessionId)
    }

    val totalDurationSec = state.samples.lastOrNull()?.relativeTimestampSec ?: 0.0

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        RoboticsTopBar(
            title = state.session?.datasetName ?: "Dataset Review",
            subtitle = "Time-Series Motion Analysis",
            onBackClick = onBackClick
        )

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = CyanNeon)
            }
        } else if (state.samples.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No recorded samples found for this session.", color = TextSecondary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
            ) {
                // Sensor Selection Tabs: Accelerometer | Gyroscope | Rotation Vector
                item {
                    val tabs = listOf(
                        SensorTypeToDisplay.ACCELEROMETER to "Accelerometer",
                        SensorTypeToDisplay.GYROSCOPE to "Gyroscope",
                        SensorTypeToDisplay.ROTATION_VECTOR to "Rotation Vector"
                    )

                    TabRow(
                        selectedTabIndex = tabs.indexOfFirst { it.first == state.sensorType },
                        containerColor = CharcoalCard,
                        contentColor = CyanNeon,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[tabs.indexOfFirst { it.first == state.sensorType }]),
                                color = CyanNeon
                            )
                        },
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp))
                    ) {
                        tabs.forEach { (type, title) ->
                            val isSelected = state.sensorType == type
                            Tab(
                                selected = isSelected,
                                onClick = { viewModel.setSensorType(type) },
                                text = {
                                    Text(
                                        text = title,
                                        fontSize = 12.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) CyanNeon else TextSecondary
                                    )
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))
                }

                // Main Interactive Canvas Oscilloscope Chart
                item {
                    InteractiveHistoricalChart(
                        samples = state.samples,
                        sensorType = state.sensorType,
                        selectedTimestampSec = state.selectedTimestampSec,
                        onTimestampSelected = { viewModel.setTimestamp(it) },
                        showX = state.showX,
                        showY = state.showY,
                        showZ = state.showZ,
                        onToggleX = { viewModel.toggleX() },
                        onToggleY = { viewModel.toggleY() },
                        onToggleZ = { viewModel.toggleZ() }
                    )

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Playback Controller Card
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(14.dp))
                            .background(CharcoalCard)
                            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(14.dp))
                            .padding(14.dp)
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "MOTION PLAYBACK SEQUENCER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    letterSpacing = 1.sp
                                )

                                // Speed selectors: 0.5x, 1x, 2x
                                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                    listOf(0.5f, 1.0f, 2.0f).forEach { speed ->
                                        val isSelected = state.playbackSpeed == speed
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(if (isSelected) CyanNeon.copy(alpha = 0.2f) else CharcoalSurfaceVariant)
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "${speed}x",
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 11.sp,
                                                color = if (isSelected) CyanNeon else TextSecondary
                                            )
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            // Interactive Timeline Scrubber Slider
                            Slider(
                                value = state.selectedTimestampSec.toFloat(),
                                onValueChange = { viewModel.setTimestamp(it.toDouble()) },
                                valueRange = 0f..totalDurationSec.toFloat().coerceAtLeast(0.01f),
                                colors = SliderDefaults.colors(
                                    thumbColor = CyanNeon,
                                    activeTrackColor = CyanNeon,
                                    inactiveTrackColor = CharcoalCardBorder
                                )
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "0.00 s",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                                Text(
                                    text = String.format(java.util.Locale.US, "%.2f s", totalDurationSec),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextTertiary
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            // Play/Pause and Reset Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                IconButton(
                                    onClick = { viewModel.setTimestamp(0.0) },
                                    modifier = Modifier
                                        .size(42.dp)
                                        .background(CharcoalSurfaceVariant, CircleShape)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Replay,
                                        contentDescription = "Rewind",
                                        tint = TextPrimary
                                    )
                                }

                                Spacer(modifier = Modifier.width(16.dp))

                                Button(
                                    onClick = { viewModel.togglePlayPause() },
                                    shape = CircleShape,
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CyanNeon,
                                        contentColor = CharcoalBackground
                                    ),
                                    modifier = Modifier.size(54.dp)
                                ) {
                                    Icon(
                                        imageVector = if (state.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                        contentDescription = if (state.isPlaying) "Pause" else "Play",
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Session Metadata Card
                item {
                    state.session?.let { s ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CharcoalCard)
                                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp))
                                .padding(14.dp)
                        ) {
                            Column {
                                Text(
                                    text = "SESSION METADATA",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    letterSpacing = 1.sp
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Label: ${s.actionLabel.uppercase()} • Samples: ${Formatters.formatNumber(s.totalSampleCount)} • Target: ${s.targetSamplingRateHz} Hz",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 11.sp,
                                    color = TextSecondary
                                )
                                if (s.description.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Description: ${s.description}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextTertiary
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
