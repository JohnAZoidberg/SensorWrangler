package me.danielschaefer.sensorwrangler.sensors

import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import java.io.BufferedReader
import java.io.FileReader
import java.io.IOException
import java.lang.Thread.sleep
import java.time.LocalTime
import kotlin.random.Random

class FileSensor(val filePath: String): Sensor() {
    override val title: String = "FileSensor" + Random.nextInt(0, 100)
    override val measurements: List<Measurement>

    private val reader = BufferedReader(FileReader(filePath))
    private var thread: Thread? = null

    override fun disconnect(reason: String?) {
        connected = false
        reader.close()

        super.disconnect(reason)
    }

    override fun connect() {
        measurements[0].startDate = LocalTime.now()
        thread = Thread {
            while (connected) {
                // TODO: Add exception handling for when the file is closed
                try {
                    val newValue = reader.readLine()
                    Platform.runLater {
                        measurements[0].values.add(newValue.toDouble())
                    }

                    println(newValue)
                    sleep(1000)
                } catch (e: IOException ) {
                    disconnect("IOException: ${e.message}")
                }
            }
        }.apply {
            start()
        }

        super.connect()
    }

    init {
        measurements = listOf(Measurement(Measurement.Unit.METER).apply{
            description = "HeartRate"
        })
    }
}
