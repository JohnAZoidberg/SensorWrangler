package me.danielschaefer.sensorwrangler.sensors

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.collections.ObservableList

interface Scannable<T : ScanResult> {
    /**
     * Scan results with a title and the connection options
     */
    fun scan(foundChannels: ObservableList<T>)

    fun configureScan(config: T)

    fun getScanStatusProperty(): ReadOnlyBooleanProperty
}
