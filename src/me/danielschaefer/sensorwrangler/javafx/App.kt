package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Application
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.gui.Graph
import me.danielschaefer.sensorwrangler.sensors.RandomSensor
import me.danielschaefer.sensorwrangler.sensors.RandomWalkSensor

class App: Application() {
    private val wrangler = SensorWrangler()

    override fun start(primaryStage: Stage) {
        val dummyPowerSensor = RandomSensor(1000).apply {
            minValue = 0
            maxValue = 10
        }
        wrangler.sensors.add(dummyPowerSensor)

        val dummyGyro = RandomSensor().apply {
            minValue = -5
            maxValue = 10
        }
        wrangler.sensors.add(dummyGyro)

        val dummyWalker = RandomWalkSensor()
        wrangler.sensors.add(dummyGyro)

        wrangler.charts.add(Graph("Foobar", arrayOf("Time", "Power"), dummyPowerSensor.measurements[0]).apply {
            windowSize = 15
            shown = true
        })
        wrangler.charts.add(Graph("BarFoo", arrayOf("Time", "Force")).apply {
            windowSize = 50
            shown = true
        })
        wrangler.charts.add(Graph("BarFoo2", arrayOf("Time", "Force"), dummyWalker.measurements[0]).apply {
            windowSize = 20
            shown = true
        })
        wrangler.charts.add(Graph("BarFoo3", arrayOf("Time", "Acceleration"), dummyGyro.measurements[0]).apply {
            windowSize = 40
            shown = true
        })
        wrangler.charts.add(Graph("BarFoo4", arrayOf("Time", "Acceleration"), dummyGyro.measurements[0]).apply {
            windowSize = 40
            shown = true
        })

        MainWindow(primaryStage, wrangler)
    }

    @Throws(Exception::class)
    override fun stop() {
        super.stop()
        // TODO: Think about whether we need to do this,
        //       as the program is going to exit anyways
        //scheduledExecutorService.shutdownNow()
    }
}