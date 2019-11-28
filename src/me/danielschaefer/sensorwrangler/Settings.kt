package me.danielschaefer.sensorwrangler

import me.danielschaefer.sensorwrangler.gui.BarGraph
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.gui.LineGraph
import me.danielschaefer.sensorwrangler.gui.ScatterGraph
import me.danielschaefer.sensorwrangler.sensors.*
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

/**
 * General Settings for the entire program
 */
open class Settings {
    val chartUpdatePeriod: Double = 40.0
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
    val version = "0.1.0"

    val supportedSensors: MutableList<KClass<out Sensor>> = mutableListOf(
        RandomSensor::class,
        RandomWalkSensor::class,
        FileSensor::class,
        SocketSensor::class
    )

    val supportedFormulas: MutableList<KClass<Any>> = mutableListOf()

    val supportedCharts: MutableList<KClass<out Chart>> = mutableListOf(
        LineGraph::class,
        ScatterGraph::class,
        BarGraph::class
    )

    // TODO: Think about how to have preferences of sensors of plugins
    @Preference("Default path of file sensor",
        explanation = "Which file the FileSensor tries to read from, by default.",
        picker = Picker.FileOpen)
    var defaultFileSensorPath: String? = null

    @Preference("Default recording directory",
        explanation = "Which directory, recordings are saved into, by default.",
        picker=Picker.Directory)
    var recordingDirectory: String? = null

    // TODO: Make overridable by cmdline param or environment variable
    val configPath: String = "wrangler.settings"
}