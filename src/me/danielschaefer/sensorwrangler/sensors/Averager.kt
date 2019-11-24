package me.danielschaefer.sensorwrangler.sensors

import javafx.collections.ListChangeListener
import me.danielschaefer.sensorwrangler.DataPoint
import me.danielschaefer.sensorwrangler.Measurement
import kotlin.random.Random

class Averager(sourceMeasurements: List<Measurement>) : VirtualSensor() {
    private val measurement: Measurement = Measurement(this, 0, Measurement.Unit.METER).apply{
        description = "Averaged measurement of " + sourceMeasurements.fold( "") {
                acc, m -> "$acc, ${m.description}"
        }
    }
    override val measurements: List<Measurement> = listOf(measurement)

    override var connected: Boolean = true
    override val title: String = "Averager ${Random.nextInt(0, 100)}"

    init {
        sourceMeasurements[0].dataPoints.addListener(ListChangeListener {
            it.next()

            val newValSum: Double = sourceMeasurements.subList(1, sourceMeasurements.size).fold(it.addedSubList.first().value) {
                // FIXME: Why do I have to force it as a double?
                acc, m -> acc + m.dataPoints.last().value
            }

            val averagedNewVal: Double = newValSum / sourceMeasurements.size
            measurement.dataPoints.add(DataPoint(it.addedSubList.first().timestamp, averagedNewVal))
        })

        connected = true
    }
}