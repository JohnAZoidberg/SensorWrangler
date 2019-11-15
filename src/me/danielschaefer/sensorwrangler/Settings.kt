package me.danielschaefer.sensorwrangler

import java.time.format.DateTimeFormatter

/**
 * General Settings for the entire program
 */
open class Settings {
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
    val version = "0.0.1"

    // TODO: Think about how to have preferences of sensors of plugins
    @Preference("Default path of file sensor", picker=Picker.FileOpen)
    var defaultFileSensorPath: String? = null

    @Preference("Default recording directory", picker=Picker.Directory)
    var recordingDirectory: String? = null
}