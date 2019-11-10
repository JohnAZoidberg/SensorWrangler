package me.danielschaefer.sensorwrangler.sensors

import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RandomWalkSensor(val updateInterval: Long = 250) : Sensor() {
    override val title: String = "RandomWalkSensor " + Random.nextInt(0, 100)
    var minValue = -25
    var maxValue = 25
    var maxStep = 5

    override val measurements: List<Measurement> = listOf(Measurement(Measurement.Unit.METER).apply{
        description = "Random walk " + Random.nextInt(0, 100)
        startDate = LocalTime.now()
        values.add(0.0)
    }, Measurement(Measurement.Unit.METER).apply{
        description = "Random walk " + Random.nextInt(0, 100)
        startDate = LocalTime.now()
        values.add(0.0)
    })

    private var updaters: MutableList<ScheduledExecutorService> = mutableListOf()

    override fun disconnect(reason: String?) {
        for (updater in updaters)
            updater.shutdown()

        super.disconnect(reason)
    }

    override fun connect() {
        for (measurement in measurements) {
            // TODO: Tie the lifetime of this to the window
            val updater = Executors.newSingleThreadScheduledExecutor()
            updaters.add(updater)
            updater.scheduleAtFixedRate({
                Platform.runLater {
                    val currentPos = measurement.values.last()
                    var newPos: Double
                    do {
                        val random = ThreadLocalRandom.current().nextInt(-maxStep, maxStep + 1)
                        newPos = currentPos + random.toDouble()
                    } while (newPos < minValue || newPos > maxValue)
                    measurement.values.add(newPos)
                }
            }, 0, updateInterval, TimeUnit.MILLISECONDS)
        }
    }

}
