package me.danielschaefer.sensorwrangler.sensors

import com.fasterxml.jackson.annotation.JsonProperty
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.annotations.ConnectionProperty
import me.danielschaefer.sensorwrangler.annotations.SensorProperty
import me.danielschaefer.sensorwrangler.data.Measurement
import mu.KotlinLogging
import org.apache.commons.io.input.Tailer
import org.apache.commons.io.input.TailerListenerAdapter
import java.io.File
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class FileSensor : Sensor() {
    override val title: String = "FileSensor ${Random.nextInt(0, 100)}"

    private val measurement = Measurement(this, 0, Measurement.Unit.BPM).apply {
        description = "HeartRate"
    }
    override val measurements: List<Measurement> = listOf(measurement)

    private var tailer: Tailer? = null

    @JsonProperty("filePath")
    @ConnectionProperty(title = "File path", default = "")
    lateinit var filePath: File

    @JsonProperty("tail")
    @SensorProperty(title = "Tail?")
    var tail: Boolean = false

    override fun specificDisconnect(reason: String?) {
        tailer?.stop()
    }

    override fun specificConnect() {
        // NOTE: Maybe Apache Commons for this is overkill, look at
        // https://crunchify.com/log-file-tailer-tail-f-implementation-in-java-best-way-to-tail-any-file-programmatically/
        val tailerListener = object : TailerListenerAdapter() {
            override fun handle(ex: Exception?) {
                disconnect()
                super.handle(ex)
            }

            override fun handle(line: String?) {
                logger.debug { "Read $line from $filePath" }

                if (line == null)
                    return

                Platform.runLater {
                    measurement.addDataPoint(line.toDouble())
                }
            }
        }
        tailer = Tailer.create(filePath, tailerListener, 1000)
    }
}
