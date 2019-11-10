package me.danielschaefer.sensorwrangler.sensors

import me.danielschaefer.sensorwrangler.Measurement

abstract class Sensor() {
    abstract val title: String
    abstract val measurements: List<Measurement>
    protected val connectionListeners: MutableList<ConnectionChangeListener> = mutableListOf()
    //abstract protected val connected: Boolean

    /**
     * Disconnect the sensor from its data source
     *
     * Note: Make sure to call super.disconnect or inform listeners when overriding.
     * TODO: How can we ensure that overriders of this class don't forget it?
     *
     * @param reason the reason for disconnection
     */
    open fun disconnect(reason: String? = null) {
        for (listener in connectionListeners)
            listener.onDisconnect(this, reason)
    }
    fun addConnectionChangeListener(listener: ConnectionChangeListener) {
        connectionListeners.add(listener)
    }

    open fun connect() {
        for (listener in connectionListeners)
            listener.onConnect()
    }
}