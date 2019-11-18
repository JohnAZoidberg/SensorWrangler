package me.danielschaefer.sensorwrangler

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.gui.LineGraph
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.sensors.Sensor
import java.io.FileWriter


class SensorWrangler() {
    val isRecording: ReadOnlyBooleanProperty
        get() = recording.readOnlyProperty

    val sensors: ObservableList<Sensor> = FXCollections.observableList(mutableListOf())
    val charts: ObservableList<Chart> = FXCollections.observableList(mutableListOf())

    private var recordingWriter: FileWriter? = null
    private var recordingListeners: MutableList<ListChangeListener<Double>> = mutableListOf()
    private var recording: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)

    fun startRecording() {
        recording.value  = true
        recordingWriter = FileWriter("${App.instance.settings.recordingDirectory}/wrangler.log", true)

        // CSV header
        recordingWriter?.write("Sensor,Measurement,Value\n")

        // Values
        for (sensor in sensors) {
            for (measurement in sensor.measurements) {
                val recordingListener = ListChangeListener<Double> {
                    // If the listener called when we're not recording, something is wrong
                    assert(recording.value)

                    it.next()
                    // Assumption is that it's an append only operation
                    for (newValue in it.addedSubList) {
                        recordingWriter?.write("${sensor.title},${measurement.description},$newValue\n")
                    }
                    recordingWriter?.flush()
                }
                recordingListeners.add(recordingListener)
                measurement.values.addListener(recordingListener)
            }
        }
    }

    fun stopRecording() {
        recording.value = false
        for (sensor in sensors)
            for (measurement in sensor.measurements)
                for (listener in recordingListeners)
                    measurement.values.removeListener(listener)

        recordingWriter?.close()
    }

    // TODO: Do we want charts to be a map indexed by the title?
    fun findChartByTitle(title: String): Chart? {
        return charts.filter { it.title == title }[0]
    }

    // TODO: Do we want sensors to be a map indexed by the title?
    fun findSensorByTitle(title: String): Sensor? {
        return sensors.filter { it.title == title }[0]
    }

    /**
     * Remove a sensor and charts associated with its measurements
     */
    fun removeSensor(sensor: Sensor) {
        for (measurement in sensor.measurements) {
            charts.removeIf { chart -> chart is LineGraph && chart.yAxis == measurement }
        }
        sensors.remove(sensor)
    }
}