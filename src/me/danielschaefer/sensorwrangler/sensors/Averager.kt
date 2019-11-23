package me.danielschaefer.sensorwrangler.sensors

import javafx.collections.ListChangeListener
import me.danielschaefer.sensorwrangler.Measurement
import java.time.LocalTime
import kotlin.random.Random

class Averager(sourceMeasurements: List<Measurement>) : VirtualSensor() {
    override val measurements: List<Measurement>
    private val measurement: Measurement
    override var connected: Boolean = true
    override val title: String = "Averager ${Random.nextInt(0, 100)}"

    init {
        measurement = Measurement(this, 0, Measurement.Unit.METER).apply{
            description = "Averaged measurement of " + sourceMeasurements.fold( "") {
                    acc, m -> "$acc, ${m.description}"
            }
            startDate = LocalTime.now()
        }
        measurements = listOf(measurement)
        sourceMeasurements[0].values.addListener(ListChangeListener {
            it.next()

            val newValSum: Double = sourceMeasurements.subList(1, sourceMeasurements.size).fold(it.addedSubList.first()) {
                    acc, m -> acc + m.values.last()
            }

            val averagedNewVal: Double = newValSum / sourceMeasurements.size
            measurement.values.add(averagedNewVal)
        })

        connected = true
    }
}