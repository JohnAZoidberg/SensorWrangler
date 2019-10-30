package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Application
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.gui.Graph
import me.danielschaefer.sensorwrangler.sensors.RandomSensor

class App: Application() {
    private val wrangler = SensorWrangler()

    override fun start(primaryStage: Stage) {
        val dummyPowerSensor = RandomSensor()
        wrangler.addSensor(dummyPowerSensor)

        wrangler.addChart(Graph("Foobar", arrayOf("Time", "Power"), dummyPowerSensor.measurements.get(0)).apply {
            windowSize = 50
            shown = true
        })
        wrangler.addChart(Graph("BarFoo", arrayOf("Time", "Force")).apply {
            windowSize = 50
            shown = true
        })
        wrangler.addChart(Graph("BarFoo2", arrayOf("Time", "Force")).apply {
            windowSize = 50
            shown = true
        })
        wrangler.addChart(Graph("BarFoo3", arrayOf("Time", "Force")).apply {
            windowSize = 50
            shown = true
        })
        wrangler.addChart(Graph("BarFoo4", arrayOf("Time", "Force")).apply {
            windowSize = 50
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