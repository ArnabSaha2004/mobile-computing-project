package com.motiondata.app

import com.motiondata.app.data.local.entity.SensorSampleEntity
import com.motiondata.app.export.CsvExporter
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayOutputStream

class CsvExporterTest {

    @Test
    fun testCsvHeaderAndFormatting() = runBlocking {
        val samples = listOf(
            SensorSampleEntity(
                id = 1,
                sessionId = "session_001",
                relativeTimestampSec = 0.000,
                accelX = 0.12f,
                accelY = 0.05f,
                accelZ = 9.78f,
                gyroX = 0.01f,
                gyroY = 0.02f,
                gyroZ = 0.00f,
                rotX = 0.01f,
                rotY = 0.02f,
                rotZ = 0.00f,
                rotW = 1.00f,
                label = "pick_object"
            ),
            SensorSampleEntity(
                id = 2,
                sessionId = "session_001",
                relativeTimestampSec = 0.010,
                accelX = 0.18f,
                accelY = 0.09f,
                accelZ = 9.65f,
                gyroX = 0.04f,
                gyroY = 0.03f,
                gyroZ = 0.01f,
                rotX = 0.02f,
                rotY = 0.03f,
                rotZ = 0.00f,
                rotW = 1.00f,
                label = "pick_object"
            )
        )

        val outputStream = ByteArrayOutputStream()
        CsvExporter.writeCsv(samples, outputStream, timeDecimals = 3, valDecimals = 2)
        val csvContent = outputStream.toString(Charsets.UTF_8.name())

        val lines = csvContent.trim().split("\r\n", "\n")
        assertEquals(3, lines.size)

        // Header Check
        assertEquals(
            "timestamp,accel_x,accel_y,accel_z,gyro_x,gyro_y,gyro_z,rotation_x,rotation_y,rotation_z,rotation_w,label,session_id",
            lines[0]
        )

        // Row 1 Check
        assertEquals(
            "0.000,0.12,0.05,9.78,0.01,0.02,0.00,0.01,0.02,0.00,1.00,pick_object,session_001",
            lines[1]
        )

        // Row 2 Check
        assertEquals(
            "0.010,0.18,0.09,9.65,0.04,0.03,0.01,0.02,0.03,0.00,1.00,pick_object,session_001",
            lines[2]
        )
    }

    @Test
    fun testCsvEscapingWithSpecialCharacters() = runBlocking {
        val samples = listOf(
            SensorSampleEntity(
                id = 1,
                sessionId = "sess,001",
                relativeTimestampSec = 1.234,
                accelX = null,
                accelY = null,
                accelZ = null,
                gyroX = null,
                gyroY = null,
                gyroZ = null,
                rotX = null,
                rotY = null,
                rotZ = null,
                rotW = null,
                label = "Pick, \"Special\" Object"
            )
        )

        val outputStream = ByteArrayOutputStream()
        CsvExporter.writeCsv(samples, outputStream)
        val csvContent = outputStream.toString(Charsets.UTF_8.name())
        val lines = csvContent.trim().split("\r\n", "\n")

        val row = lines[1]
        // Check quotes and escaped double quotes
        assertTrue(row.contains("\"Pick, \"\"Special\"\" Object\""))
        assertTrue(row.contains("\"sess,001\""))
    }
}
