package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.Settings
import me.danielschaefer.sensorwrangler.gui.Graph
import me.danielschaefer.sensorwrangler.sensors.RandomSensor
import me.danielschaefer.sensorwrangler.sensors.RandomWalkSensor
import kotlin.system.exitProcess

class App: Application() {
    val wrangler = SensorWrangler()
    val settings = Settings()

    override fun start(primaryStage: Stage) {
        instance = this

        // Wrap in try/catch and print everything, to see the proper stacktrace
        // even in non-debugging environments.
        // This is especially useful if it crashes right after launching.
        try {
            runMainWindow(primaryStage)
        } catch (e: Throwable) {
            println("Exception: $e occured")
            println("With message ${e.message}")
            println("And cause: ${e.cause}")
            println("Stacktrace:")
            for (x in e.stackTrace)
                println(x)
        }
    }

    private fun runMainWindow(primaryStage: Stage) {
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
        wrangler.sensors.add(dummyWalker)

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

    override fun stop() {
        Platform.exit()
        // TODO: Properly close all stages
        // TODO: Stop all Executors
        exitProcess(0);
    }

    companion object {
        var instance: App? = null;
    }
}