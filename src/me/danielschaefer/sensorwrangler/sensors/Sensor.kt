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
     * Note: Make sure to call super.disconnect or inform listeners when overriding.
     * TODO: How can we ensure that overriders of this class don't forget it?
     *       Probably connected should be a watch-only property
     *
     * @param reason the reason for disconnection
     */
    open fun disconnect(reason: String? = null) {
        connected = false
        for (listener in connectionListeners)
            listener.onDisconnect(this, reason)
    }
    fun addConnectionChangeListener(listener: ConnectionChangeListener) {
        connectionListeners.add(listener)
    }

    open fun connect() {
        for (listener in connectionListeners)
            listener.onConnect()
        connected = true
    }
}