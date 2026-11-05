package com.motiondata.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.motiondata.app.ui.theme.AxisXColor
import com.motiondata.app.ui.theme.AxisYColor
import com.motiondata.app.ui.theme.AxisZColor
import com.motiondata.app.ui.theme.CharcoalCard
import com.motiondata.app.ui.theme.CharcoalCardBorder
import com.motiondata.app.ui.theme.ChartGridLine
import com.motiondata.app.ui.theme.ChartZeroLine
import com.motiondata.app.ui.theme.TextPrimary
import com.motiondata.app.ui.theme.TextSecondary
import com.motiondata.app.ui.utils.Formatters
import kotlin.math.abs
import kotlin.math.max

data class ChartPoint(
    val x: Float,
    val y: Float,
    val z: Float
)

@Composable
fun SensorRealtimeChart(
    title: String,
    unit: String,
    points: List<ChartPoint>,
    currentX: Float?,
    currentY: Float?,
    currentZ: Float?,
    modifier: Modifier = Modifier,
    fixedYRange: Float? = null // e.g. 20f for accel, 10f for gyro
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CharcoalCard)
            .border(1.dp, CharcoalCardBorder, RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        // Chart Header with Title and Dynamic Real-Time Values Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = title.uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = unit,
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
            }

            // Real-Time Axis Badges Legend
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                AxisLegendBadge(name = "X", color = AxisXColor, value = currentX)
                AxisLegendBadge(name = "Y", color = AxisYColor, value = currentY)
                AxisLegendBadge(name = "Z", color = AxisZColor, value = currentZ)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // High-Performance Real-Time Canvas Oscilloscope
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF070A0F))
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val midY = h / 2f

                // Draw Grid Lines
                val gridLines = 4
                for (i in 1 until gridLines) {
                    val gy = h * (i.toFloat() / gridLines)
                    drawLine(
                        color = ChartGridLine,
                        start = Offset(0f, gy),
                        end = Offset(w, gy),
                        strokeWidth = 1f
                    )
                }

                // Draw Zero Baseline
                drawLine(
                    color = ChartZeroLine,
                    start = Offset(0f, midY),
                    end = Offset(w, midY),
                    strokeWidth = 1.5f
                )

                if (points.size < 2) return@Canvas

                // Determine dynamic or fixed Y range
                var maxVal = fixedYRange ?: 10f
                if (fixedYRange == null) {
                    for (p in points) {
                        maxVal = max(maxVal, max(abs(p.x), max(abs(p.y), abs(p.z))))
                    }
                    maxVal = (maxVal * 1.15f).coerceAtLeast(4f)
                }

                val pathX = Path()
                val pathY = Path()
                val pathZ = Path()

                val step = w / (points.size - 1).coerceAtLeast(1)

                points.forEachIndexed { i, pt ->
                    val px = i * step
                    val pyX = midY - (pt.x / maxVal) * (h / 2f)
                    val pyY = midY - (pt.y / maxVal) * (h / 2f)
                    val pyZ = midY - (pt.z / maxVal) * (h / 2f)

                    if (i == 0) {
                        pathX.moveTo(px, pyX)
                        pathY.moveTo(px, pyY)
                        pathZ.moveTo(px, pyZ)
                    } else {
                        pathX.lineTo(px, pyX)
                        pathY.lineTo(px, pyY)
                        pathZ.lineTo(px, pyZ)
                    }
                }

                // Draw 3-axis waveforms with crisp contrast
                drawPath(pathX, AxisXColor, style = Stroke(width = 2.2f))
                drawPath(pathY, AxisYColor, style = Stroke(width = 2.2f))
                drawPath(pathZ, AxisZColor, style = Stroke(width = 2.2f))
            }
        }
    }
}

@Composable
fun AxisLegendBadge(
    name: String,
    color: Color,
    value: Float?
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .background(color, CircleShape)
        )
        Text(
            text = "$name: ${Formatters.formatSensorValue(value, 2)}",
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}
