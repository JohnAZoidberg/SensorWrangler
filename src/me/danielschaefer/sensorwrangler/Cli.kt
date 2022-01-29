package me.danielschaefer.sensorwrangler

import mu.KotlinLogging

private val logger = KotlinLogging.logger {}
class Cli {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            logger.debug { "Launching CLI" }
            println("Hello World!")
        }
    }
}
