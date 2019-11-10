package me.danielschaefer.sensorwrangler.sensors

interface ConnectionChangeListener {
    fun onDisconnect(sensor: Sensor, reason: String?)
}
