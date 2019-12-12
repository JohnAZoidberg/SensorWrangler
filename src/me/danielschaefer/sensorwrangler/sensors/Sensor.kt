package me.danielschaefer.sensorwrangler.sensors


/**
 * Abstract Sensor
 *
 * Subclasses must have a default (no-args) constructor!
 */
abstract class Sensor : VirtualSensor() {
    private val connectionListeners: MutableList<ConnectionChangeListener> = mutableListOf()
    override var connected = false

    /**
     * Disconnect the sensor from its data source
     *
     * @param reason the reason for disconnection
     */
    fun disconnect(reason: String? = null) {
        // Disconnecting always succeeds and we don't want to disconnect twice -> set it to false, first thing
        connected = false

        specificDisconnect(reason)
        for (listener in connectionListeners)
            listener.onChanged(this, false, reason)
    }

    fun addConnectionChangeListener(listener: ConnectionChangeListener) {
        connectionListeners.add(listener)
    }

    fun removeConnectionChangeListener(listener: ConnectionChangeListener) {
        connectionListeners.remove(listener)
    }

    fun connect() {
        // Don't connect twice
        if (connected)
            return

        specificConnect()
        connected = true
        for (listener in connectionListeners)
            listener.onChanged(this, true, null)
    }

    /**
     * Sensor specific connect
     *
     * TODO: Return whether connection was successful
     */
    abstract fun specificConnect()
    /**
     * Sensor specific disconnect
     */
    abstract fun specificDisconnect(reason: String?)
}
