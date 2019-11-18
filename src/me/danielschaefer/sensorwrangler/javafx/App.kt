package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.Settings
import me.danielschaefer.sensorwrangler.gui.LineGraph
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
        val dummyPowerSensor = RandomSensor().apply {
            updateInterval = 1000
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

        wrangler.charts.add(LineGraph("Foobar", arrayOf("Time", "Power"), listOf(dummyPowerSensor.measurements[0])).apply {
            windowSize = 15
            lowerBound = 0.0
            upperBound = 11.0
        })
        wrangler.charts.add(LineGraph("BarFoo2", arrayOf("Time", "Force"), listOf(dummyWalker.measurements[0])).apply {
            windowSize = 20
            lowerBound = -30.0
            upperBound = 30.0
            tickSpacing = 5.0
        })
        wrangler.charts.add(LineGraph("BarFoo3", arrayOf("Time", "Acceleration"), listOf(dummyGyro.measurements[0])).apply {
            windowSize = 40
        })
        wrangler.charts.add(LineGraph("BarFoo4", arrayOf("Time", "Acceleration"), listOf(dummyGyro.measurements[0])).apply {
            windowSize = 40
            lowerBound = -10.0
            upperBound = 15.0
            tickSpacing = 5.0
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
        lateinit var instance: App
    }
}
