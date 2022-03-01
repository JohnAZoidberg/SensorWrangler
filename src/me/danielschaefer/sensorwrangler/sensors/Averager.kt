package me.danielschaefer.sensorwrangler.sensors

import com.fasterxml.jackson.annotation.JsonProperty
import javafx.application.Platform
import me.danielschaefer.sensorwrangler.data.Measurement
import me.danielschaefer.sensorwrangler.data.VirtualSensor
import me.danielschaefer.sensorwrangler.util.NamedThreadFactory
import mu.KotlinLogging
import java.util.Date
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import kotlin.random.Random

private val logger = KotlinLogging.logger {}

class Averager : VirtualSensor() {
    @JsonProperty("sourceMeasurements")
    var sourceMeasurements: List<Measurement> = listOf()

    private var measurement: Measurement? = null
    override var measurements: MutableList<Measurement> = mutableListOf()
    override var connected: Boolean = true

    override val title: String = "Averager ${Random.nextInt(0, 100)}"

    /**
     * How much time lies between each new average and
     * over which interval to average the individual measurements
     *
     * Unit: Milliseconds
     * TODO: Maybe there is a type for seconds and milliseconds
     */
    private val period: Long = 1_000

    private var updater: ScheduledExecutorService? = null

    fun connect() {
        if (measurement != null)
            return

        if (sourceMeasurements.all { it.unit != sourceMeasurements.first().unit }) {
            logger.error { "Cannot average measurements with different units" }
            return
        }

        measurement = Measurement(this, 0, sourceMeasurements.first().unit).apply {
            description = "Average of\n" + sourceMeasurements.joinToString(separator = ",\n") {
                it.description ?: ""
            }

            measurements.clear()
            measurements.add(this)
        }

        if (updater != null)
            return

        updater = Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update $title values")).apply {
            scheduleAtFixedRate(
                {
                    Platform.runLater {
                        val connectedMeasurements = sourceMeasurements.filter { it.sensor.isConnected }
                        val summedNewVals = connectedMeasurements.fold(0.0) {
                                acc, m ->
                            // Average data points of this measurement during the last $period milliseconds
                            val dataPoints = m.dataPoints.filter {
                                it.timestamp > Date().time.toDouble() - period.toDouble()
                            }
                            acc + dataPoints.sumOf { it.value } / dataPoints.size
                        }

                        if (connectedMeasurements.isNotEmpty())
                            measurement?.addDataPoint(summedNewVals / connectedMeasurements.size)
                    }
                },
                0, period, TimeUnit.MILLISECONDS
            ) // 40ms = 25FPS
        }
    }
}
