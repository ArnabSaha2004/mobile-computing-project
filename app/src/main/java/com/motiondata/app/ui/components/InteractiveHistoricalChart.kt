package com.motiondata.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motiondata.app.data.local.entity.SensorSampleEntity
import com.motiondata.app.ui.theme.AxisXColor
import com.motiondata.app.ui.theme.AxisYColor
import com.motiondata.app.ui.theme.AxisZColor
import com.motiondata.app.ui.theme.CharcoalCard
import com.motiondata.app.ui.theme.CharcoalCardBorder
import com.motiondata.app.ui.theme.ChartGridLine
import com.motiondata.app.ui.theme.ChartZeroLine
import com.motiondata.app.ui.theme.CyanNeon
import com.motiondata.app.ui.theme.TextPrimary
import com.motiondata.app.ui.theme.TextSecondary
import com.motiondata.app.ui.theme.TextTertiary
import com.motiondata.app.ui.utils.Formatters
import kotlin.math.abs
import kotlin.math.max

enum class SensorTypeToDisplay {
    ACCELEROMETER,
    GYROSCOPE,
    ROTATION_VECTOR
}

@Composable
fun InteractiveHistoricalChart(
    samples: List<SensorSampleEntity>,
    sensorType: SensorTypeToDisplay,
    selectedTimestampSec: Double?,
    onTimestampSelected: (Double) -> Unit,
    showX: Boolean,
    showY: Boolean,
    showZ: Boolean,
    showW: Boolean = false,
    onToggleX: () -> Unit,
    onToggleY: () -> Unit,
    onToggleZ: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (samples.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(CharcoalCard)
                .border(1.dp, CharcoalCardBorder, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text("No sensor data recorded in this session", color = TextSecondary)
        }
        return
    }

    val totalDurationSec = samples.last().relativeTimestampSec.coerceAtLeast(0.001)

    // Find the sample closest to selected timestamp or default to first
    val currentSample = remember(samples, selectedTimestampSec) {
        if (selectedTimestampSec == null) samples.firstOrNull()
        else samples.minByOrNull { abs(it.relativeTimestampSec - selectedTimestampSec) } ?: samples.firstOrNull()
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalCard)
            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        // Axis Filter Chips & Tooltip readout
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                FilterChip(
                    selected = showX,
                    onClick = onToggleX,
                    label = { Text("Axis X") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AxisXColor.copy(alpha = 0.2f),
                        selectedLabelColor = AxisXColor,
                        labelColor = TextTertiary
                    )
                )
                FilterChip(
                    selected = showY,
                    onClick = onToggleY,
                    label = { Text("Axis Y") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AxisYColor.copy(alpha = 0.2f),
                        selectedLabelColor = AxisYColor,
                        labelColor = TextTertiary
                    )
                )
                FilterChip(
                    selected = showZ,
                    onClick = onToggleZ,
                    label = { Text("Axis Z") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AxisZColor.copy(alpha = 0.2f),
                        selectedLabelColor = AxisZColor,
                        labelColor = TextTertiary
                    )
                )
            }

            // Scrubber Time Indicator
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "t = ${String.format(java.util.Locale.US, "%.3f s", currentSample?.relativeTimestampSec ?: 0.0)}",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = CyanNeon
                )
                Text(
                    text = "/ ${String.format(java.util.Locale.US, "%.2f s", totalDurationSec)}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = TextTertiary
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Tooltip Values bar for current cursor
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFF090D14))
                .padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            val valX: Float?
            val valY: Float?
            val valZ: Float?
            val unitStr: String

            when (sensorType) {
                SensorTypeToDisplay.ACCELEROMETER -> {
                    valX = currentSample?.accelX
                    valY = currentSample?.accelY
                    valZ = currentSample?.accelZ
                    unitStr = "m/s²"
                }
                SensorTypeToDisplay.GYROSCOPE -> {
                    valX = currentSample?.gyroX
                    valY = currentSample?.gyroY
                    valZ = currentSample?.gyroZ
                    unitStr = "rad/s"
                }
                SensorTypeToDisplay.ROTATION_VECTOR -> {
                    valX = currentSample?.rotX
                    valY = currentSample?.rotY
                    valZ = currentSample?.rotZ
                    unitStr = "quat"
                }
            }

            AxisLegendBadge(name = "X", color = AxisXColor, value = valX)
            AxisLegendBadge(name = "Y", color = AxisYColor, value = valY)
            AxisLegendBadge(name = "Z", color = AxisZColor, value = valZ)
            Text(
                text = unitStr,
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp,
                color = TextTertiary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Interactive Canvas Chart with Scrubber Drag & Tap
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF070A0F))
                .pointerInput(samples) {
                    detectTapGestures { offset ->
                        val ratio = (offset.x / size.width).coerceIn(0f, 1f)
                        val targetTime = ratio * totalDurationSec
                        onTimestampSelected(targetTime)
                    }
                }
                .pointerInput(samples) {
                    detectDragGestures { change, _ ->
                        val ratio = (change.position.x / size.width).coerceIn(0f, 1f)
                        val targetTime = ratio * totalDurationSec
                        onTimestampSelected(targetTime)
                    }
                }
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                // Grid lines
                val gridLines = 5
                for (i in 1 until gridLines) {
                    val gy = h * (i.toFloat() / gridLines)
                    drawLine(
                        color = ChartGridLine,
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f
                    )
                }

                // Zero line
                drawLine(
                    color = ChartZeroLine,
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.5f
                )

                // Compute max absolute amplitude for scaling
                var maxAmplitude = 5f
                for (s in samples) {
                    when (sensorType) {
                        SensorTypeToDisplay.ACCELEROMETER -> {
                            s.accelX?.let { maxAmplitude = max(maxAmplitude, abs(it)) }
                            s.accelY?.let { maxAmplitude = max(maxAmplitude, abs(it)) }
                            s.accelZ?.let { maxAmplitude = max(maxAmplitude, abs(it)) }
                        }
                        SensorTypeToDisplay.GYROSCOPE -> {
                            s.gyroX?.let { maxAmplitude = max(maxAmplitude, abs(it)) }
                            s.gyroY?.let { maxAmplitude = max(maxAmplitude, abs(it)) }
                            s.gyroZ?.let { maxAmplitude = max(maxAmplitude, abs(it)) }
                        }
                        SensorTypeToDisplay.ROTATION_VECTOR -> {
                            maxAmplitude = 1.05f
                        }
                    }
                }
                maxAmplitude *= 1.15f

                val pathX = Path()
                val pathY = Path()
                val pathZ = Path()

                var startedX = false
                var startedY = false
                var startedZ = false

                samples.forEach { s ->
                    val px = ((s.relativeTimestampSec / totalDurationSec) * w).toFloat().coerceIn(0f, w)

                    val (vx, vy, vz) = when (sensorType) {
                        SensorTypeToDisplay.ACCELEROMETER -> Triple(s.accelX, s.accelY, s.accelZ)
                        SensorTypeToDisplay.GYROSCOPE -> Triple(s.gyroX, s.gyroY, s.gyroZ)
                        SensorTypeToDisplay.ROTATION_VECTOR -> Triple(s.rotX, s.rotY, s.rotZ)
                    }

                    if (vx != null && showX) {
                        val py = midY - (vx / maxAmplitude) * (h / 2f)
                        if (!startedX) { pathX.moveTo(px, py); startedX = true } else pathX.lineTo(px, py)
                    }
                    if (vy != null && showY) {
                        val py = midY - (vy / maxAmplitude) * (h / 2f)
                        if (!startedY) { pathY.moveTo(px, py); startedY = true } else pathY.lineTo(px, py)
                    }
                    if (vz != null && showZ) {
                        val py = midY - (vz / maxAmplitude) * (h / 2f)
                        if (!startedZ) { pathZ.moveTo(px, py); startedZ = true } else pathZ.lineTo(px, py)
                    }
                }

                if (showX && startedX) drawPath(pathX, AxisXColor, style = Stroke(width = 2f))
                if (showY && startedY) drawPath(pathY, AxisYColor, style = Stroke(width = 2f))
                if (showZ && startedZ) drawPath(pathZ, AxisZColor, style = Stroke(width = 2f))

                // Draw Scrubber / Playback Cursor Line
                val curTime = currentSample?.relativeTimestampSec ?: 0.0
                val scrubberX = ((curTime / totalDurationSec) * w).toFloat().coerceIn(0f, w)

                drawLine(
                    color = CyanNeon,
                    start = Offset(scrubberX, 0f),
                    end = Offset(scrubberX, h),
                    strokeWidth = 2.5f
                )

                // Scrubber Marker Dot
                drawCircle(
                    color = CyanNeon,
                    radius = 5.dp.toPx(),
                    center = Offset(scrubberX, midY)
                )
            }
        }
    }
}
