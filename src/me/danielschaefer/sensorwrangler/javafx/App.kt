package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.Settings
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
