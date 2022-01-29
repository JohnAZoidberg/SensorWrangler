package me.danielschaefer.sensorwrangler

import javafx.application.Application
import me.danielschaefer.sensorwrangler.javafx.App
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
class Main {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            logger.debug { "Launching Application" }
            Application.launch(App::class.java, *args)
        }
    }
}
