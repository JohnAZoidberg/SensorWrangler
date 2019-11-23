package me.danielschaefer.sensorwrangler

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.module.SimpleModule
import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.collections.ObservableList
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.gui.LineGraph
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.sensors.Sensor
import me.danielschaefer.sensorwrangler.sensors.VirtualSensor
import java.io.BufferedReader
import java.io.FileReader
import java.io.FileWriter
import java.util.*


class SensorWrangler {
    val isRecording: ReadOnlyBooleanProperty
        get() = recording.readOnlyProperty

    val sensors: ObservableList<VirtualSensor> = FXCollections.observableList(mutableListOf())
    val charts: ObservableList<Chart> = FXCollections.observableList(mutableListOf())

    private var recordingWriter: FileWriter? = null
    private var recordingListeners: MutableList<ListChangeListener<Double>> = mutableListOf()
    private var recording: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)

    private val objectMapper = ObjectMapper().apply {
        disable(
            MapperFeature.AUTO_DETECT_CREATORS,
            MapperFeature.AUTO_DETECT_FIELDS,
            MapperFeature.AUTO_DETECT_GETTERS,
            MapperFeature.AUTO_DETECT_IS_GETTERS
        )
        enable(
            SerializationFeature.WRAP_ROOT_VALUE
        )
        enable(
            DeserializationFeature.UNWRAP_ROOT_VALUE
        )

        val module = SimpleModule()
        module.addDeserializer(Measurement::class.java, MeasurementDeserializer())
        registerModule(module)
    }

    fun startRecording() {
        recording.value = true
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
        return charts.find { it.title == title }
    }

    // TODO: Do we want sensors to be a map indexed by the title?
    fun findVirtualSensorByTitle(title: String): VirtualSensor? {
        return sensors.find { it.title == title }
    }

    fun findSensorByTitle(title: String): Sensor? {
        return sensors.find { it.title == title && it is Sensor} as Sensor?
    }

    /**
     * Remove a sensor and charts associated with its measurements
     */
    fun removeSensor(sensor: Sensor) {
        for (measurement in sensor.measurements) {
            charts.removeIf { chart -> chart is LineGraph && chart.yAxes.contains(measurement) }
        }
        sensors.remove(sensor)
    }

    fun export(path: String) {
        val writer = FileWriter(path)

        for (sensor in sensors) {
            val sensorString: String = objectMapper.writeValueAsString(sensor)
            writer.write("Sensor@$sensorString\n")
        }

        for (chart in charts) {
            val chartString: String = objectMapper.writeValueAsString(chart)
            writer.write("Chart@$chartString\n")
        }

        writer.flush()
    }

    fun import(path: String) {
        val reader = BufferedReader(FileReader(path))
        while(reader.ready()) {
            val (prefix, jsonObject) = reader.readLine().split("@")

            val myMap: HashMap<*, *> = ObjectMapper().readValue(jsonObject, HashMap::class.java)
            val className = myMap.keys.first() as String

            when (prefix) {
                "Sensor" -> {
                    val newSensor: Sensor = objectMapper.readValue(jsonObject, Class.forName("me.danielschaefer.sensorwrangler.sensors.$className")) as Sensor
                    sensors.add(newSensor)
                }
                "Chart" -> {
                    val newChart: Chart = objectMapper.readValue(jsonObject, Class.forName("me.danielschaefer.sensorwrangler.gui.$className")) as Chart
                    charts.add(newChart)
                }
            }
        }
    }
}