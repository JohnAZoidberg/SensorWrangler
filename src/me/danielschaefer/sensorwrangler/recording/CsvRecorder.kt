package me.danielschaefer.sensorwrangler.recording

import me.danielschaefer.sensorwrangler.Measurement
import java.io.File
import java.io.FileWriter
import java.io.Writer

class CsvRecorder(file: File): Recorder {
    private val writer: Writer

    init {
        writer = FileWriter(file, true)

        // Header
        writer.write("Timestamp,Sensor,Measurement,Value\n")
    }

    override fun recordValue(timestamp: String, measurement: Measurement, value: Double) {
        writer.write("${measurement.sensor.title},${measurement.description},$timestamp,${value}\n")
        // Always flush, so that a consumer can follow the values live
        writer.flush()
    }

    override fun close() {
        writer.close()
    }
}
