package me.danielschaefer.sensorwrangler.recording

import me.danielschaefer.sensorwrangler.Measurement

interface Recorder {
    fun recordValue(timestamp: String, measurement: Measurement, value: Double)
    fun close()
}
