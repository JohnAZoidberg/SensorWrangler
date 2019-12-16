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
import me.danielschaefer.sensorwrangler.recording.Recorder
import me.danielschaefer.sensorwrangler.sensors.ConnectionChangeListener
import me.danielschaefer.sensorwrangler.sensors.Sensor
import me.danielschaefer.sensorwrangler.sensors.VirtualSensor
import java.io.BufferedReader
import java.io.FileNotFoundException
import java.io.FileReader
import java.io.FileWriter
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.jvm.javaType


class SensorWrangler {
    val isRecording: ReadOnlyBooleanProperty
        get() = recording.readOnlyProperty

    val sensors: ObservableList<VirtualSensor> = FXCollections.observableList(mutableListOf<VirtualSensor>()).apply {
        // Add all listeners to new sensors
        addListener(ListChangeListener {
            it.next()
            it.addedSubList.filterIsInstance<Sensor>().forEach { sensor ->
                sensorConnectionListeners.forEach { listener -> sensor.addConnectionChangeListener(listener) }
            }
        })
    }
    val charts: ObservableList<Chart> = FXCollections.observableList(mutableListOf<Chart>()).apply {
        addListener(ListChangeListener {
            it.next()
            // Remove chart from UI, when it's removed
            it.removed.forEach { chart -> chart.hideAll() }
        })
    }

    private val recorders: MutableList<Recorder> = mutableListOf()
    private var recordingListeners: MutableList<ListChangeListener<DataPoint>> = mutableListOf()
    private var recording: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)

    private val sensorConnectionListeners: ObservableList<ConnectionChangeListener> = FXCollections.observableArrayList(mutableListOf<ConnectionChangeListener>()).apply {
        // Add/remove new listener to/from all sensors
        addListener(ListChangeListener {
            it.next()
            it.addedSubList.forEach { listener ->
                sensors.filterIsInstance<Sensor>().forEach { sensor -> sensor.addConnectionChangeListener(listener) }
            }
            it.removed.forEach { listener ->
                sensors.filterIsInstance<Sensor>().forEach { sensor -> sensor.removeConnectionChangeListener(listener) }
            }
        })
    }

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

    private fun startRecording() {
        // If already recording, the listeners don't have to be set up again
        if (recording.value)
            return

        recording.value = true

        // TODO: Add new sensors
        for (sensor in sensors) {
            for (measurement in sensor.measurements) {
                val recordingListener = ListChangeListener<DataPoint> { listChange ->
                    // If the listener called when we're not recording, something is wrong
                    assert(recording.value)

                    listChange.next()
                    // We assume that the measurements list is only ever appended
                    for (newValue in listChange.addedSubList) {
                        val formatter = DateTimeFormatter.ofPattern("HH:mm:ss.SSS")
                        val newDateTime = Instant.ofEpochMilli(newValue.timestamp)
                            .atZone(ZoneId.of("GMT+1"))  // TODO: Think about how to deal with TZs
                            .format(formatter)
                        recorders.forEach { it.recordValue(newDateTime, measurement, newValue.value) }
                    }
                }
                recordingListeners.add(recordingListener)
                measurement.dataPoints.addListener(recordingListener)
            }
        }
    }

    fun stopRecording() {
        recording.value = false
        for (sensor in sensors)
            for (measurement in sensor.measurements)
                for (listener in recordingListeners)
                    measurement.dataPoints.removeListener(listener)

        recordingListeners.clear()

        recorders.forEach { it.close() }
        recorders.clear()
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
     *
     * TODO: For charts with multiple measurements, don't remove the entire chart,
     * only the measurements of this sensor.
     */
    fun removeSensor(sensor: VirtualSensor) {
        for (measurement in sensor.measurements) {
            charts.removeIf { chart -> chart is LineGraph && chart.yAxes.contains(measurement) }
        }
        sensors.remove(sensor)
    }

    // FIXME: Throws ConcurrentModificationException
    // TODO: Use pure JSON
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

        for (property in Settings::class.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()) {
            if (property.annotations.filterIsInstance<Preference>().isEmpty())
                continue

            val propString: String = objectMapper.writeValueAsString(property.getter.call(App.instance.settings))
            val propName = property.name
            writer.write("Preference.$propName@$propString\n")
        }

        writer.flush()
    }

    fun import(path: String): Boolean {
        try {
            val reader = BufferedReader(FileReader(path))
            while(reader.ready()) {
                val (prefix, jsonObject) = reader.readLine().split("@")

                val myMap: HashMap<*, *> = ObjectMapper().readValue(jsonObject, HashMap::class.java)
                val className = myMap.keys.first() as String

                when (prefix) {
                    "Sensor" -> {
                        val newSensor = objectMapper.readValue(jsonObject, Class.forName("me.danielschaefer.sensorwrangler.sensors.$className")) as VirtualSensor
                        sensors.add(newSensor)
                    }
                    "Chart" -> {
                        val newChart: Chart = objectMapper.readValue(jsonObject, Class.forName("me.danielschaefer.sensorwrangler.gui.$className")) as Chart
                        charts.add(newChart)
                    }
                    else -> importOther(prefix, jsonObject)
                }
            }
            return true
        } catch (e: FileNotFoundException) {
            return false
        }
    }

    private fun importOther(prefix: String, jsonObject: String) {
        val (prefixType, propName) = prefix.split(".")
        when (prefixType) {
            "Preference" -> {
                // TODO: Catch wrong property name
                //  E.g. if the user manually changed the settings file or it is from a different Wrangler version
                val property = Settings::class.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>().first { it.name == propName }
                val type = property.returnType.javaType

                // Is always true, because Class<T> is a subtype of Type
                // TODO: Find something to do away with the if-check (e.g. a safe cast of some sort)
                if (type is Class<*>)
                    property.setter.call(App.instance.settings, objectMapper.readValue(jsonObject, type))
            }
        }
    }

    fun addRecorder(recorder: Recorder) {
        recorders.add(recorder)
        startRecording()
    }

    fun addSensorConnectionListener(listener: ConnectionChangeListener ) {
        sensorConnectionListeners.add(listener)
    }
}
