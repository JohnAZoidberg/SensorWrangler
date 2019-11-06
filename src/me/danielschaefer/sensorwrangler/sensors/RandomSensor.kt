package me.danielschaefer.sensorwrangler.sensors

import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RandomSensor(val updateInterval: Long = 250) : Sensor() {
    var minValue = -10;
    var maxValue = 10;

    override val title: String = "RandomSensor" + Random.nextInt(0, 100)

    override val measurements: List<Measurement> = listOf(Measurement(Measurement.Unit.METER).apply{
        description = "Random measurement " + Random.nextInt(0, 100)
        startDate = LocalTime.now()
    })

    init {
        // TODO: Tie the lifetime of this to the window
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate({
            Platform.runLater {
                val random = ThreadLocalRandom.current().nextInt(minValue, maxValue + 1)
                measurements[0].values.add(random.toDouble())
            }
        }, 0, updateInterval, TimeUnit.MILLISECONDS)
    }
}