package me.danielschaefer.sensorwrangler

import java.time.format.DateTimeFormatter

/**
 * General Settings for the entire program
 */
open class Settings {
    val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")
    val version = "0.0.1"

    var recordingDirectory: String? = null
}