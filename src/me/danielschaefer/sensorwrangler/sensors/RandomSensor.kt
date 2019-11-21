package me.danielschaefer.sensorwrangler.sensors

import com.fasterxml.jackson.annotation.JsonProperty
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.annotations.SensorProperty
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RandomSensor: Sensor() {
    override val title: String = "RandomSensor" + Random.nextInt(0, 100)

    @JsonProperty("updateInterval")
    @SensorProperty(title = "Update Interval")
    var updateInterval: Long = 250

    @JsonProperty("minValue")
    @SensorProperty(title = "Minimum value")
    var minValue = -10;

    @JsonProperty("maxValue")
    @SensorProperty(title = "Maximum value")
    var maxValue = 10;

    override val measurements: List<Measurement> = listOf(Measurement(this, 0, Measurement.Unit.METER).apply{
        description = "Random measurement " + Random.nextInt(0, 100)
        startDate = LocalTime.now()
    })

    private var updater: ScheduledExecutorService? = null

    override fun connect() {
        // TODO: Tie the lifetime of this to the window
        updater = Executors.newSingleThreadScheduledExecutor().apply {
            scheduleAtFixedRate({
                Platform.runLater {
                    connected = true
                    val random = ThreadLocalRandom.current().nextInt(minValue, maxValue + 1)
                    measurements[0].values.add(random.toDouble())
                }
            }, 0, updateInterval, TimeUnit.MILLISECONDS)
        }

        super.connect()
    }

    override fun disconnect(reason: String?) {
        connected = false
        updater?.shutdown()

        super.disconnect(reason)
    }
}