package me.danielschaefer.sensorwrangler.sensors

import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

class RandomSensor() : Sensor("RandomSensor") {
    override val measurements: List<Measurement> = listOf(Measurement(Measurement.Unit.METER).apply{
        description = "Random measurement"
        startDate = LocalTime.now()
        values.add(1.0)
    })

    init {
        // TODO: Tie the lifetime of this to the window
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            Platform.runLater {
                val random = ThreadLocalRandom.current().nextInt(20) - 10
                println("$title measured: $random")
                measurements[0].values.add(random.toDouble())
            }
        }, 0, 1, TimeUnit.SECONDS)
    }
}