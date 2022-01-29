package me.danielschaefer.sensorwrangler.data

import com.fasterxml.jackson.annotation.JsonProperty
import java.util.UUID

abstract class VirtualSensor {
    @JsonProperty("uuid")
    val uuid: String = UUID.randomUUID().toString()

    abstract val title: String
    abstract val measurements: List<Measurement>

    protected abstract var connected: Boolean
    val isConnected: Boolean
        get() = connected

    override fun toString(): String {
        return title
    }
}
