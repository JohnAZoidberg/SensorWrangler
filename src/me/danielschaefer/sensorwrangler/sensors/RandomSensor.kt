package me.danielschaefer.sensorwrangler.sensors

import com.fasterxml.jackson.annotation.JsonProperty
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.annotations.SensorProperty
import me.danielschaefer.sensorwrangler.data.Measurement
import me.danielschaefer.sensorwrangler.util.NamedThreadFactory
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RandomSensor : Sensor() {
    override val title: String = "RandomSensor" + Random.nextInt(0, 100)

    @JsonProperty("updateInterval")
    @SensorProperty(title = "Update Interval [ms]")
    var updateInterval: Long = 250

    @JsonProperty("minValue")
    @SensorProperty(title = "Minimum value")
    var minValue = -10

    @JsonProperty("maxValue")
    @SensorProperty(title = "Maximum value")
    var maxValue = 10

    private val measurement = Measurement(this, 0, Measurement.Unit.UNITLESS).apply {
        description = "Random measurement " + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(measurement)

    private var updater: ScheduledExecutorService? = null

    override fun specificConnect() {
        // TODO: Tie the lifetime of this to the window
        updater = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update $title values")).apply {
            scheduleAtFixedRate(
                {
                    Platform.runLater {
                        connected = true
                        val random = ThreadLocalRandom.current().nextInt(minValue, maxValue + 1)
                        measurement.addDataPoint(random.toDouble())
                    }
                },
                0, updateInterval, TimeUnit.MILLISECONDS
            )
        }
    }

    override fun specificDisconnect(reason: String?) {
        updater?.shutdown()
    }
}
