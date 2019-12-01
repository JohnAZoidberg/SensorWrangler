package me.danielschaefer.sensorwrangler.sensors

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.Measurement
import java.util.*

abstract class VirtualSensor {
    @JsonProperty("uuid")
    val uuid: String = UUID.randomUUID().toString()

    abstract val title: String
    abstract val measurements: List<Measurement>

    protected abstract var connected: Boolean
    val isConnected: Boolean
        get() = connected
}
