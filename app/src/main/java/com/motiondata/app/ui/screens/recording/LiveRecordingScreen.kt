package com.motiondata.app.ui.screens.recording

import android.app.Activity
import android.view.WindowManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.Sensors
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motiondata.app.ui.components.SensorRealtimeChart
import com.motiondata.app.ui.theme.AxisXColor
import com.motiondata.app.ui.theme.BlueElectric
import com.motiondata.app.ui.theme.CharcoalBackground
import com.motiondata.app.ui.theme.CharcoalCard
import com.motiondata.app.ui.theme.CharcoalCardBorder
import com.motiondata.app.ui.theme.CyanNeon
import com.motiondata.app.ui.theme.StatusAmber
import com.motiondata.app.ui.theme.StatusGreen
import com.motiondata.app.ui.theme.TextPrimary
import com.motiondata.app.ui.theme.TextSecondary
import com.motiondata.app.ui.theme.TextTertiary
import com.motiondata.app.ui.utils.Formatters

@Composable
fun LiveRecordingScreen(
    viewModel: LiveRecordingViewModel,
    sessionId: String,
    datasetName: String,
    actionLabel: String,
    targetHz: Int,
    durationLimitSec: Int,
    onRecordingCompleted: (String) -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.uiState.collectAsState()
    val accelPoints by viewModel.accelPoints.collectAsState()
    val gyroPoints by viewModel.gyroPoints.collectAsState()

    // Keep screen awake while recording
    DisposableEffect(Unit) {
        val window = (context as? Activity)?.window
        window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    // Launch recording on initial composition
    LaunchedEffect(sessionId) {
        viewModel.startSession(sessionId, datasetName, actionLabel, targetHz, durationLimitSec)
    }

    // Handle auto-finish
    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onRecordingCompleted(sessionId)
        }
    }

    // Blinking recording dot animation
    val infiniteTransition = rememberInfiniteTransition(label = "BlinkRec")
    val recAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "RecAlpha"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(CharcoalBackground)
    ) {
        // Live Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = state.datasetName,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(CyanNeon.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = state.actionLabel.uppercase(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = CyanNeon
                        )
                    }
                    if (state.durationLimitSec > 0) {
                        Text(
                            text = "Limit: ${state.durationLimitSec}s",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = TextTertiary
                        )
                    }
                }
            }

            // Blinking REC Status Pill
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .background(if (state.isPaused) StatusAmber.copy(alpha = 0.15f) else AxisXColor.copy(alpha = 0.15f))
                    .border(
                        1.dp,
                        if (state.isPaused) StatusAmber else AxisXColor,
                        RoundedCornerShape(20.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 5.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .alpha(if (state.isPaused) 1f else recAlpha)
                            .background(if (state.isPaused) StatusAmber else AxisXColor, CircleShape)
                    )
                    Text(
                        text = if (state.isPaused) "PAUSED" else "REC",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (state.isPaused) StatusAmber else AxisXColor
                    )
                }
            }
        }

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .weight(1f)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(top = 4.dp, bottom = 16.dp)
        ) {
            // Digital Stopwatch Timer & Metrics Dashboard
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(CharcoalCard)
                        .border(1.dp, CharcoalCardBorder, RoundedCornerShape(16.dp))
                        .padding(16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = Formatters.formatDurationStopwatch(state.durationMs),
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 42.sp,
                            color = TextPrimary,
                            letterSpacing = 2.sp
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SAMPLING RATE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "${Formatters.formatFrequency(state.achievedHz)} / ${state.targetHz} Hz",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = CyanNeon
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "SAMPLES LOGGED",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = Formatters.formatNumber(state.sampleCount),
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = StatusGreen
                                )
                            }

                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "STORAGE BUFFER",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextTertiary,
                                    fontSize = 9.sp
                                )
                                Text(
                                    text = "Persisting",
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = BlueElectric
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Real-Time Oscilloscope 1: Accelerometer (3 Axes)
            item {
                SensorRealtimeChart(
                    title = "Accelerometer",
                    unit = "Linear Acceleration (m/s²)",
                    points = accelPoints,
                    currentX = state.currentFrame.accelX,
                    currentY = state.currentFrame.accelY,
                    currentZ = state.currentFrame.accelZ,
                    fixedYRange = 25f
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Real-Time Oscilloscope 2: Gyroscope (3 Axes)
            item {
                SensorRealtimeChart(
                    title = "Gyroscope",
                    unit = "Angular Velocity (rad/s)",
                    points = gyroPoints,
                    currentX = state.currentFrame.gyroX,
                    currentY = state.currentFrame.gyroY,
                    currentZ = state.currentFrame.gyroZ,
                    fixedYRange = 12f
                )

                Spacer(modifier = Modifier.height(14.dp))
            }

            // Live Magnitudes readout
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CharcoalCard)
                            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("ACCEL MAGNITUDE |a|", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = "${Formatters.formatSensorValue(state.currentFrame.accelMagnitude)} m/s²",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(CharcoalCard)
                            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(10.dp))
                            .padding(10.dp)
                    ) {
                        Column {
                            Text("GYRO MAGNITUDE |ω|", style = MaterialTheme.typography.labelSmall, color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = "${Formatters.formatSensorValue(state.currentFrame.gyroMagnitude)} rad/s",
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Live Controls Bottom Bar (Pause/Resume & Stop)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(CharcoalCard)
                .border(1.dp, CharcoalCardBorder)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Pause / Resume Button
            OutlinedButton(
                onClick = { viewModel.togglePauseResume() },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = if (state.isPaused) StatusGreen else StatusAmber
                ),
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (state.isPaused) StatusGreen else StatusAmber
                )
            ) {
                Icon(
                    imageVector = if (state.isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (state.isPaused) "RESUME" else "PAUSE",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }

            // Stop Recording Button
            Button(
                onClick = {
                    viewModel.stopSession { stoppedId ->
                        onRecordingCompleted(stoppedId)
                    }
                },
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AxisXColor,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Stop,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "STOP & SAVE",
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 13.sp
                )
            }
        }
    }
}
