package com.motiondata.app.export

import android.content.Context
import android.net.Uri
import androidx.core.content.FileProvider
import com.motiondata.app.data.local.entity.RecordingSessionEntity
import com.motiondata.app.data.local.entity.SensorSampleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.util.Locale

object CsvExporter {

    private const val CSV_HEADER =
        "timestamp,accel_x,accel_y,accel_z,gyro_x,gyro_y,gyro_z,rotation_x,rotation_y,rotation_z,rotation_w,label,session_id"

    /**
     * Writes CSV data directly into an output stream (e.g. from Storage Access Framework).
     */
    suspend fun writeCsv(
        samples: List<SensorSampleEntity>,
        outputStream: OutputStream,
        timeDecimals: Int = 3,
        valDecimals: Int = 4
    ) = withContext(Dispatchers.IO) {
        val writer = BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8))
        try {
            writer.write(CSV_HEADER)
            writer.newLine()

            val timeFormat = "%.${timeDecimals}f"
            val valFormat = "%.${valDecimals}f"

            for (sample in samples) {
                val tStr = String.format(Locale.US, timeFormat, sample.relativeTimestampSec)
                val axStr = sample.accelX?.let { String.format(Locale.US, valFormat, it) } ?: ""
                val ayStr = sample.accelY?.let { String.format(Locale.US, valFormat, it) } ?: ""
                val azStr = sample.accelZ?.let { String.format(Locale.US, valFormat, it) } ?: ""

                val gxStr = sample.gyroX?.let { String.format(Locale.US, valFormat, it) } ?: ""
                val gyStr = sample.gyroY?.let { String.format(Locale.US, valFormat, it) } ?: ""
                val gzStr = sample.gyroZ?.let { String.format(Locale.US, valFormat, it) } ?: ""

                val rxStr = sample.rotX?.let { String.format(Locale.US, valFormat, it) } ?: ""
                val ryStr = sample.rotY?.let { String.format(Locale.US, valFormat, it) } ?: ""
                val rzStr = sample.rotZ?.let { String.format(Locale.US, valFormat, it) } ?: ""
                val rwStr = sample.rotW?.let { String.format(Locale.US, valFormat, it) } ?: ""

                val labelEsc = escapeCsv(sample.label)
                val sessionEsc = escapeCsv(sample.sessionId)

                writer.write("$tStr,$axStr,$ayStr,$azStr,$gxStr,$gyStr,$gzStr,$rxStr,$ryStr,$rzStr,$rwStr,$labelEsc,$sessionEsc")
                writer.newLine()
            }
            writer.flush()
        } finally {
            writer.close()
        }
    }

    /**
     * Creates a temporary CSV file in application cache and returns a content Uri via FileProvider for native sharing.
     */
    suspend fun createShareableFile(
        context: Context,
        session: RecordingSessionEntity,
        samples: List<SensorSampleEntity>
    ): Uri = withContext(Dispatchers.IO) {
        val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
        val safeName = session.datasetName
            .replace("[^a-zA-Z0-9_\\-]".toRegex(), "_")
            .ifEmpty { "session_${session.sessionId.take(8)}" }
        val file = File(exportDir, "${safeName}_dataset.csv")

        FileOutputStream(file).use { fos ->
            writeCsv(samples, fos)
        }

        FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
    }

    private fun escapeCsv(value: String): String {
        return if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
