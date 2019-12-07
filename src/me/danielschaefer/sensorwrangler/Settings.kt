package me.danielschaefer.sensorwrangler

import me.danielschaefer.sensorwrangler.gui.*
import me.danielschaefer.sensorwrangler.recording.CsvRecorder
import me.danielschaefer.sensorwrangler.recording.DatabaseRecorder
import me.danielschaefer.sensorwrangler.recording.Recorder
import me.danielschaefer.sensorwrangler.recording.SocketRecorder
import me.danielschaefer.sensorwrangler.sensors.*
import java.nio.file.Paths
import java.time.format.DateTimeFormatter
import kotlin.reflect.KClass

/**
 * General Settings for the entire program
 */
open class Settings {
    val chartUpdatePeriod: Double = 40.0
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
    val version = "0.3.0"

    val supportedSensors: MutableList<KClass<out Sensor>> = mutableListOf(
        AntCadenceSensor::class,
        AntHeartRateSensor::class,
        AntStationaryBike::class,
        AntPowerSensor::class,
        AntSpeedSensor::class,
        RandomSensor::class,
        RandomWalkSensor::class,
        FileSensor::class,
        SocketSensor::class
    )

    val supportedFormulas: MutableList<KClass<Any>> = mutableListOf()

    val supportedRecorders: MutableList<KClass<out Recorder>> = mutableListOf(
        CsvRecorder::class,
        DatabaseRecorder::class,
        SocketRecorder::class
    )

    val supportedCharts: MutableList<KClass<out Chart>> = mutableListOf(
        CurrentValueGraph::class,
        DistributionGraph::class,
        LineGraph::class,
        ScatterGraph::class,
        BarGraph::class
    )

    // TODO: Think about how to have preferences of sensors of plugins
    @Preference("Default directory of file sensor",
        explanation = "Which directory the dialog to create a FileSensor opens, to choose a file from",
        picker = Picker.FileOpen)
    var defaultFileSensorDirectory: String? = null

    // Default: Current working directory
    @Preference("Default recording directory",
        explanation = "Which directory, recordings are saved into, by default.",
        picker=Picker.Directory)
    var recordingDirectory: String = Paths.get("").toAbsolutePath().toString()

    @Preference("Rows in chart grid",
        explanation = "How many rows of charts the grid has")
    var chartGridRows: Int = 2

    @Preference("Columns in chart grid",
        explanation = "How many columns of charts the grid has")
    var chartGridCols: Int = 2

    // TODO: Make overridable by cmdline param or environment variable
    val configPath: String = "wrangler.settings"
}
