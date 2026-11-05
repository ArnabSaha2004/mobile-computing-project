package com.motiondata.app.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WatchLater
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
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
import com.motiondata.app.data.model.MotionAction
import com.motiondata.app.data.model.SamplingFrequency
import com.motiondata.app.ui.components.RoboticsTopBar
import com.motiondata.app.ui.theme.AxisXColor
import com.motiondata.app.ui.theme.BlueElectric
import com.motiondata.app.ui.theme.CharcoalBackground
import com.motiondata.app.ui.theme.CharcoalCard
import com.motiondata.app.ui.theme.CharcoalCardBorder
import com.motiondata.app.ui.theme.CharcoalSurfaceVariant
import com.motiondata.app.ui.theme.CyanNeon
import com.motiondata.app.ui.theme.StatusGreen
import com.motiondata.app.ui.theme.TextPrimary
import com.motiondata.app.ui.theme.TextSecondary
import com.motiondata.app.ui.theme.TextTertiary

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SetupScreen(
    viewModel: SetupViewModel,
    onBackClick: () -> Unit,
    onStartRecording: (sessionId: String, name: String, label: String, hz: Int, limit: Int) -> Unit
) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        RoboticsTopBar(
            title = "Recording Setup",
            subtitle = "Configure Sensor Parameters",
            onBackClick = onBackClick
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 10.dp, bottom = 40.dp)
        ) {
            // Section 1: Dataset Name
            item {
                Text(
                    text = "DATASET IDENTIFIER",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.datasetName,
                    onValueChange = { viewModel.updateDatasetName(it) },
                    modifier = Modifier.fillMaxWidth(),
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
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 2: Action Label Selector
            item {
                Text(
                    text = "ROBOTIC ACTION LABEL",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    MotionAction.PRESETS.forEach { action ->
                        val isSelected = state.selectedAction.id == action.id
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.selectAction(action) },
                            label = { Text(action.displayName) },
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

                // If Custom Action selected, show text field
                if (state.selectedAction.isCustom) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = state.customActionText,
                        onValueChange = { viewModel.updateCustomActionText(it) },
                        placeholder = { Text("Enter custom action (e.g. unscrew_cap, press_switch)") },
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
                }

                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 3: Description (Optional)
            item {
                Text(
                    text = "SESSION DESCRIPTION (OPTIONAL)",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedTextField(
                    value = state.description,
                    onValueChange = { viewModel.updateDescription(it) },
                    placeholder = { Text("Notes on subject, arm placement, or experimental conditions") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CyanNeon,
                        unfocusedBorderColor = CharcoalCardBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        focusedContainerColor = CharcoalCard,
                        unfocusedContainerColor = CharcoalCard
                    ),
                    maxLines = 2
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 4: Target Sampling Frequency
            item {
                Text(
                    text = "SAMPLING FREQUENCY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    SamplingFrequency.entries.forEach { freq ->
                        val isSelected = state.samplingFrequency == freq
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) CyanNeon.copy(alpha = 0.15f) else CharcoalCard)
                                .border(
                                    1.dp,
                                    if (isSelected) CyanNeon else CharcoalCardBorder,
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.updateSamplingFrequency(freq) }
                                .padding(vertical = 12.dp, horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "${freq.hz} Hz",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = if (isSelected) CyanNeon else TextPrimary
                                )
                                Text(
                                    text = "${freq.periodMicros / 1000} ms",
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = TextTertiary
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = state.samplingFrequency.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextTertiary,
                    fontSize = 12.sp
                )
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Section 5: Recording Duration Limit
            item {
                Text(
                    text = "RECORDING DURATION LIMIT",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextTertiary,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(8.dp))

                val limits = listOf(0 to "Manual Stop", 5 to "5s", 10 to "10s", 30 to "30s", 60 to "60s")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    limits.forEach { (sec, label) ->
                        val isSelected = state.durationLimitSeconds == sec
                        FilterChip(
                            selected = isSelected,
                            onClick = { viewModel.updateDurationLimit(sec) },
                            label = { Text(label, fontSize = 11.sp) },
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

            // Section 6: Hardware Sensor Availability & Calibration
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Sensors,
                                    contentDescription = null,
                                    tint = CyanNeon,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "HARDWARE SENSORS CHECK",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            TextButton(onClick = { viewModel.startCalibration() }) {
                                Icon(Icons.Default.Build, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Calibrate", color = CyanNeon, fontSize = 12.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        SensorStatusRow(
                            name = "Accelerometer",
                            available = state.sensorsStatus.accelerometer.isAvailable,
                            detail = if (state.sensorsStatus.accelerometer.isAvailable)
                                "${state.sensorsStatus.accelerometer.name} (Max ${state.sensorsStatus.accelerometer.maxRange.toInt()} m/s²)"
                            else "Sensor not detected"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SensorStatusRow(
                            name = "Gyroscope",
                            available = state.sensorsStatus.gyroscope.isAvailable,
                            detail = if (state.sensorsStatus.gyroscope.isAvailable)
                                "${state.sensorsStatus.gyroscope.name} (Max ${state.sensorsStatus.gyroscope.maxRange.toInt()} rad/s)"
                            else "Sensor not detected"
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        SensorStatusRow(
                            name = "Rotation Vector",
                            available = state.sensorsStatus.rotationVector.isAvailable,
                            detail = if (state.sensorsStatus.rotationVector.isAvailable)
                                "Quaternion orientation fusion available"
                            else "Optional: Not detected (Fused orientation inactive)"
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            // Section 7: Arm & Wrist Mounting Guide
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CharcoalSurfaceVariant)
                        .border(1.dp, CharcoalCardBorder, RoundedCornerShape(12.dp))
                        .padding(14.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyanNeon,
                            modifier = Modifier.size(20.dp)
                        )
                        Column {
                            Text(
                                text = "Wrist Mount & Safety Protocol",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "1. Securely strap phone to forearm or wrist with screen facing outward.\n" +
                                        "2. Maintain consistent phone orientation relative to your arm during the motion.\n" +
                                        "3. Avoid sudden jerking of mount straps to preserve rigid kinematic coupling.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 17.sp
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))
            }

            // Launch Button
            item {
                Button(
                    onClick = {
                        val effectiveLabel = viewModel.getEffectiveActionLabel()
                        onStartRecording(
                            state.sessionId,
                            state.datasetName,
                            effectiveLabel,
                            state.samplingFrequency.hz,
                            state.durationLimitSeconds
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyanNeon,
                        contentColor = CharcoalBackground
                    )
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "START RECORDING SESSION",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }

    // Calibration Dialog
    if (state.isCalibrating || state.calibrationResult != null) {
        AlertDialog(
            onDismissRequest = { viewModel.dismissCalibration() },
            title = {
                Text(
                    text = if (state.isCalibrating) "Calibrating Sensors..." else "Calibration Report",
                    color = TextPrimary
                )
            },
            text = {
                Column {
                    if (state.isCalibrating) {
                        Text(
                            text = "Please lay the smartphone completely flat and motionless on a stable table surface.",
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LinearProgressIndicator(
                            progress = { state.calibrationProgress },
                            modifier = Modifier.fillMaxWidth(),
                            color = CyanNeon
                        )
                    } else state.calibrationResult?.let { res ->
                        Text(
                            text = if (res.isStaticAndStable)
                                "Sensors are stable and ready for capture."
                            else
                                "High sensor noise detected. Ensure device is kept completely stationary.",
                            color = if (res.isStaticAndStable) StatusGreen else AxisXColor,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "Samples Evaluated: ${res.samplesCollected}\n" +
                                    "Accel Bias: [${String.format(java.util.Locale.US, "%.2f, %.2f, %.2f", res.accelBiasX, res.accelBiasY, res.accelBiasZ)}] m/s²\n" +
                                    "Gyro Bias: [${String.format(java.util.Locale.US, "%.3f, %.3f, %.3f", res.gyroBiasX, res.gyroBiasY, res.gyroBiasZ)}] rad/s\n" +
                                    "Gyro Noise σ: ${String.format(java.util.Locale.US, "%.4f", res.gyroNoiseStdDev)} rad/s",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.dismissCalibration() },
                    colors = ButtonDefaults.buttonColors(containerColor = CyanNeon, contentColor = CharcoalBackground)
                ) {
                    Text("OK")
                }
            },
            containerColor = CharcoalCard,
            shape = RoundedCornerShape(14.dp)
        )
    }
}

@Composable
fun SensorStatusRow(
    name: String,
    available: Boolean,
    detail: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = if (available) Icons.Default.CheckCircle else Icons.Default.Warning,
            contentDescription = null,
            tint = if (available) StatusGreen else AxisXColor,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Column {
            Text(
                text = name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}
