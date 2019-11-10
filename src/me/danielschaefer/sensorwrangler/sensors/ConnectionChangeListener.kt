package me.danielschaefer.sensorwrangler.sensors

interface ConnectionChangeListener {
    fun onConnect()
    fun onDisconnect(sensor: Sensor, reason: String?)
}
