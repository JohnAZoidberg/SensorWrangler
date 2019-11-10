package me.danielschaefer.sensorwrangler.sensors

open class ConnectionChangeAdapter: ConnectionChangeListener {
    override fun onConnect() {
    }
    override fun onDisconnect(sensor: Sensor, reason: String?) {
    }
}