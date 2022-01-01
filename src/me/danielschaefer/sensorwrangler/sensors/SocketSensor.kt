package me.danielschaefer.sensorwrangler.sensors

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.annotations.ConnectionProperty
import java.io.IOException
import java.net.ConnectException
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.random.Random

class SocketSensor : Sensor() {
    override val title: String = "SocketSensor ${Random.nextInt(0, 100)}"

    private val measurement = Measurement(this, 0, Measurement.Unit.BPM).apply {
        description = "HeartRate"
    }
    override val measurements: List<Measurement> = listOf(measurement)

    private var socket: Socket? = null
    private var thread: Thread? = null

    @JsonProperty("hostname")
    @ConnectionProperty(title = "Hostname", default = "localhost")
    var hostname: String = "localhost"

    @JsonProperty("port")
    @ConnectionProperty(title = "Port", default = "8080")
    var port: Int = 8080

    override fun specificDisconnect(reason: String?) {
        socket?.close()
        socket = null
    }

    override fun specificConnect() {
        if (socket != null)
            return

        try {
            socket = Socket(hostname, port)
            val reader = socket?.inputStream?.bufferedReader()

            // TODO: Probably all sensors need to run on a thread, can we abstract this and start the thread by the parent class?
            thread = thread(start = true) {
                while (socket?.isClosed == false && socket?.isConnected == true) {
                    try {
                        val line = reader?.readLine()

                        val value = line?.toDoubleOrNull()

                        println("Read $line from $hostname:$port")

                        if (value == null) {
                            println("Invalid number was read in $title: '$line'")
                            continue
                        }

                        measurement.addDataPoint(value)
                    } catch (e: IOException) {
                        // If we're already disconnected, we don't care about the exception
                        if (connected)
                            disconnect("Socket had an IOException: ${e.message}")

                        return@thread
                    }
                }
                disconnect("Socket was closed/disconnected")
            }
        } catch (e: ConnectException) {
            disconnect("Socket failed to connect: ${e.message}")
        }
    }
}
