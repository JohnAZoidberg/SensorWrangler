package me.danielschaefer.sensorwrangler.sensors

import com.fasterxml.jackson.annotation.JsonProperty
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.NamedThreadFactory
import me.danielschaefer.sensorwrangler.annotations.SensorProperty
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RandomWalkSensor : Sensor() {
    override val title: String = "RandomWalkSensor ${Random.nextInt(0, 100)}"

    @JsonProperty("updateInterval")
    @SensorProperty(title = "Update Interval [ms]")
    var updateInterval: Long = 250

    @JsonProperty("minValue")
    @SensorProperty(title = "Minimum value")
    var minValue = -25

    @JsonProperty("maxValue")
    @SensorProperty(title = "Maximum value")
    var maxValue = 25

    @JsonProperty("maxStep")
    @SensorProperty(title = "Maximum step")
    var maxStep = 5

    // TODO: Allow this to be set by the user
    var unit: Measurement.Unit = Measurement.Unit.UNITLESS

    private val measurement: Measurement = Measurement(this, 0, unit).apply {
        description = "Random walk " + Random.nextInt(0, 100)
    }
    override val measurements: List<Measurement> = listOf(measurement)

    private var updaters: MutableList<ScheduledExecutorService> = mutableListOf()

    override fun specificDisconnect(reason: String?) {
        updaters.forEach { it.shutdown() }
    }

    override fun specificConnect() {
        // TODO: Tie the lifetime of this to the window
        updaters.add(
            Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update $title values")).apply {
                scheduleAtFixedRate(
                    {
                        Platform.runLater {
                            val currentPos = if (measurement.dataPoints.isEmpty())
                                ThreadLocalRandom.current().nextInt(-maxStep, maxStep + 1).toDouble()
                            else
                                measurement.dataPoints.last().value

                            var newPos: Double
                            do {
                                val random = ThreadLocalRandom.current().nextInt(-maxStep, maxStep + 1).toDouble()
                                newPos = currentPos + random
                            } while (newPos < minValue || newPos > maxValue)

                            measurement.addDataPoint(newPos)
                        }
                    },
                    0, updateInterval, TimeUnit.MILLISECONDS
                )
            }
        )
    }
}
