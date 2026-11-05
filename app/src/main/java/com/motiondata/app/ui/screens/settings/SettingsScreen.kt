package com.motiondata.app.ui.screens.settings

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PrecisionManufacturing
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motiondata.app.data.model.SamplingFrequency
import com.motiondata.app.data.model.SensorSpec
import com.motiondata.app.ui.components.RoboticsTopBar
import com.motiondata.app.ui.screens.setup.SensorStatusRow
import com.motiondata.app.ui.theme.AxisXColor
import com.motiondata.app.ui.theme.BlueElectric
import com.motiondata.app.ui.theme.CharcoalBackground
import com.motiondata.app.ui.theme.CharcoalCard
import com.motiondata.app.ui.theme.CharcoalCardBorder
import com.motiondata.app.ui.theme.CharcoalSurfaceVariant
import com.motiondata.app.ui.theme.CyanNeon
import com.motiondata.app.ui.theme.TextPrimary
import com.motiondata.app.ui.theme.TextSecondary
import com.motiondata.app.ui.theme.TextTertiary

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        RoboticsTopBar(
            title = "Settings & Diagnostics",
            subtitle = "Sensors & Export Configuration",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
        ) {
            // Section 1: Default Sensor Frequency
            item {
                SectionHeader("DEFAULT SAMPLING FREQUENCY")
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SamplingFrequency.entries.forEach { freq ->
                        val isSelected = state.defaultFrequency == freq
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateDefaultFrequency(freq) },
                            label = { Text("${freq.hz} Hz", fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                                selectedLabelColor = CyanNeon,
                                containerColor = CharcoalCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CharcoalCardBorder,
                                selectedBorderColor = CyanNeon
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 2: Real-time Graph Update Rate
            item {
                SectionHeader("GRAPH OSCILLOSCOPE REFRESH RATE")
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(30 to "30 FPS (Battery Saver)", 60 to "60 FPS (Ultra Smooth)").forEach { (fps, label) ->
                        val isSelected = state.graphFps == fps
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateGraphFps(fps) },
                            label = { Text(label, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = BlueElectric.copy(alpha = 0.2f),
                                selectedLabelColor = BlueElectric,
                                containerColor = CharcoalCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CharcoalCardBorder,
                                selectedBorderColor = BlueElectric
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 3: CSV Export Preferences
            item {
                SectionHeader("CSV EXPORT DECIMAL PRECISION")
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(3 to "3 Decimals (Standard)", 4 to "4 Decimals (Recommended)", 6 to "6 Decimals (High Precision)").forEach { (decimals, label) ->
                        val isSelected = state.csvDecimals == decimals
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateCsvDecimals(decimals) },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = CyanNeon.copy(alpha = 0.2f),
                                selectedLabelColor = CyanNeon,
                                containerColor = CharcoalCard,
                                labelColor = TextSecondary
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = CharcoalCardBorder,
                                selectedBorderColor = CyanNeon
                            ),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section 4: Deep Hardware Sensor Specifications
            item {
                SectionHeader("PHYSICAL SENSORS HARDWARE DIAGNOSTICS")
                Spacer(modifier = Modifier.height(8.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        SensorHardwareDetailItem(
                            title = "Linear Accelerometer",
                            spec = state.sensorsStatus.accelerometer,
                            unit = "m/s²"
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        SensorHardwareDetailItem(
                            title = "Angular Gyroscope",
                            spec = state.sensorsStatus.gyroscope,
                            unit = "rad/s"
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        SensorHardwareDetailItem(
                            title = "Rotation Vector (Orientation)",
                            spec = state.sensorsStatus.rotationVector,
                            unit = "quaternion"
                        )
                    }
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // Section 5: Technical Limitations & Robotics Imitation Learning Note
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalSurfaceVariant)
                        .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PrecisionManufacturing,
                                contentDescription = null,
                                tint = CyanNeon,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "Robotics Imitation Learning & IMU Physics",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "• Raw Acceleration vs Position: The accelerometer measures specific force including gravity (9.81 m/s²). Double integration to position creates quadratic drift without external SLAM/optical reference.\n\n" +
                                    "• Angular Velocity: Gyroscope measures rotational speed in rad/s. Integrated orientation is subject to sensor bias drift.\n\n" +
                                    "• Imitation Learning: These motion time-series datasets are designed to be ingested into behavioral cloning (BC), diffusion policies, or reinforcement learning pipelines on robotic manipulators.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 18.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section 6: About App
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "MotionData v1.0.0",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextTertiary
                    )
                    Text(
                        text = "Built for Robotics Research & Imitation Learning",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextTertiary,
                        fontSize = 10.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = TextTertiary,
        letterSpacing = 1.sp,
        fontWeight = FontWeight.Bold
    )
}

@Composable
fun SensorHardwareDetailItem(
    title: String,
    spec: SensorSpec,
    unit: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Text(
                text = if (spec.isAvailable) "ONLINE" else "NOT FOUND",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = if (spec.isAvailable) CyanNeon else AxisXColor,
                fontWeight = FontWeight.Bold
            )
        }

        if (spec.isAvailable) {
            Text(
                text = "${spec.vendor} • ${spec.name}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 11.sp
            )
            Text(
                text = "Max Range: ${spec.maxRange} $unit • Resolution: ${spec.resolution} • Min Delay: ${spec.minDelayUs} µs (${String.format(java.util.Locale.US, "%.0f", spec.maxSupportedFrequencyHz)} Hz max)",
                fontFamily = FontFamily.Monospace,
                fontSize = 10.sp,
                color = TextTertiary
            )
        }
    }
}
