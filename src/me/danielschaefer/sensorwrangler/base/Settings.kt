package me.danielschaefer.sensorwrangler.base

import me.danielschaefer.sensorwrangler.data.Chart
import me.danielschaefer.sensorwrangler.data.Picker
import me.danielschaefer.sensorwrangler.data.Preference
import me.danielschaefer.sensorwrangler.data.Recorder
import me.danielschaefer.sensorwrangler.data.VirtualSensor
import java.nio.file.Paths
import kotlin.reflect.KClass

/**
 * General Settings for the entire program
 */
open class Settings {
    /**
     * How fast time should move, in percent.
     */
    var chartUpdateMultiplier: Int = 100
    val chartUpdatePeriod: Double = 40.0

    val version = "0.5.0"

    val supportedSensors: MutableList<KClass<out VirtualSensor>> = mutableListOf()

    val supportedRecorders: MutableList<KClass<out Recorder>> = mutableListOf()

    val supportedCharts: MutableList<KClass<out Chart>> = mutableListOf()

    // TODO: Think about how to have preferences of sensors of plugins
    @Preference(
        "Default directory of file sensor",
        explanation = "Which directory the dialog to create a FileSensor opens, to choose a file from",
        picker = Picker.FileOpen
    )
    var defaultFileSensorDirectory: String? = null

    // Default: Current working directory
    @Preference(
        "Default recording directory",
        explanation = "Which directory, recordings are saved into, by default.",
        picker = Picker.Directory
    )
    var recordingDirectory: String = Paths.get("").toAbsolutePath().toString()

    @Preference(
        "Rows in chart grid",
        explanation = "How many rows of charts the grid has. Takes effect only after app restart."
    )
    var chartGridRows: Int = 2

    @Preference(
        "Columns in chart grid",
        explanation = "How many columns of charts the grid has. Takes effect only after app restart."
    )
    var chartGridCols: Int = 2

    @Preference(
        "Pixel width of a new window",
        explanation = "How pixels the main window is wide on app launch. Takes effect only after app restart."
    )
    var defaultWindowWidth: Int = 800

    @Preference(
        "Columns in chart grid",
        explanation = "How pixels the main window is high on app launch. Takes effect only after app restart."
    )
    var defaultWindowHeight: Int = 600

    // TODO: Make overridable by cmdline param or environment variable
    val configPath: String = "wrangler.settings"
}
