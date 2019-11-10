package me.danielschaefer.sensorwrangler.sensors

import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import java.io.BufferedReader
import java.io.FileReader
import java.lang.Thread.sleep
import java.time.LocalTime

class FileSensor(val filePath: String): Sensor() {
    override val title: String = "FileSensor at $filePath"
    override val measurements: List<Measurement>

    private var connected = false
    private val reader = BufferedReader(FileReader(filePath))
    private var thread: Thread

    override fun disconnect() {
        connected = false
        reader.close()
    }

    init {
        measurements = listOf(Measurement(Measurement.Unit.METER).apply{
            description = "HeartRate"
            startDate = LocalTime.now()
        })

        connected = true
        thread = Thread {
            while (connected) {
                // TODO: Add exception handling for when the file is closed
                val newValue = reader.readLine()

                Platform.runLater {
                    measurements[0].values.add(newValue.toDouble())
                }


                println(newValue)
                sleep(1000)
            }
        }
        thread.start()
    }
}
