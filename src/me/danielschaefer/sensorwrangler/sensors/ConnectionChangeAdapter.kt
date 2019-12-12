package me.danielschaefer.sensorwrangler.sensors

open class ConnectionChangeAdapter: ConnectionChangeListener {
    override fun onChanged(sensor: Sensor, connected: Boolean, reason: String?) {
    }
}
