package me.danielschaefer.sensorwrangler.sensors

import me.danielschaefer.sensorwrangler.Measurement

abstract class Sensor() {
    abstract val title: String
    abstract val measurements: List<Measurement>
    //abstract protected val connected: Boolean

    abstract fun disconnect()
}