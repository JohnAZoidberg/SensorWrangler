package me.danielschaefer.sensorwrangler.sensors

import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class RandomWalkSensor(val updateInterval: Long = 250) : Sensor("RandomSensor") {
    var minValue = -25
    var maxValue = 25
    var maxStep = 5

    override val measurements: List<Measurement> = listOf(Measurement(Measurement.Unit.METER).apply{
        description = "Random walk"
        startDate = LocalTime.now()
        values.add(0.0)
    })

    init {
        // TODO: Tie the lifetime of this to the window
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            Platform.runLater {
                val currentPos = measurements[0].values.last()
                var newPos = 0.0
                do {
                    val random = ThreadLocalRandom.current().nextInt(-maxStep, maxStep + 1)
                    newPos = currentPos + random.toDouble()
                } while (newPos < minValue || newPos > maxValue)
                println("$title measured: $newPos")
                measurements[0].values.add(newPos)
            }
        }, 0, updateInterval, TimeUnit.MILLISECONDS)
    }
}
