package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Application
import javafx.application.Platform
import javafx.stage.Stage
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

class GuiApp : Application() {
    override fun start(primaryStage: Stage) {
        // Wrap in try/catch and print everything, to see the proper stacktrace
        // even in non-debugging environments.
        // This is especially useful if it crashes right after launching.
        try {
            runMainWindow(primaryStage)
        } catch (e: Throwable) {
            logger.error { "Exception: $e occured" }
            logger.error { "With message ${e.message}" }
            logger.error { "And cause: ${e.cause}" }
            logger.error { "Stacktrace:" }
            for (x in e.stackTrace)
                logger.error { x }
        }
    }

    private fun runMainWindow(primaryStage: Stage) {
        MainWindow(primaryStage)
    }

    override fun stop() {
        Platform.exit()
        // TODO: Properly close all stages
        // TODO: Stop all Executors
        exitProcess(0)
    }
}
