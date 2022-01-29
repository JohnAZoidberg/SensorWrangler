package me.danielschaefer.sensorwrangler.data

interface Recorder {
    fun recordValue(timestamp: String, measurement: Measurement, value: Double)
    fun close()
}
