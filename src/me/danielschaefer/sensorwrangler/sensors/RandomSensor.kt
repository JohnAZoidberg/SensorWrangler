package me.danielschaefer.sensorwrangler.sensors

import javafx.application.Platform
import me.danielschaefer.sensorwrangler.Measurement
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class RandomSensor(val updateInterval: Long = 250) : Sensor() {
    var minValue = -10;
    var maxValue = 10;

    override val title: String = "RandomSensor" + Random.nextInt(0, 100)

    override val measurements: List<Measurement> = listOf(Measurement(this, Measurement.Unit.METER).apply{
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