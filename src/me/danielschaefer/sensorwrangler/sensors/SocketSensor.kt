package me.danielschaefer.sensorwrangler.sensors

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.annotations.ConnectionProperty
import java.io.IOException
import java.net.ConnectException
import java.net.Socket
import kotlin.concurrent.thread
import kotlin.random.Random

class SocketSensor: Sensor() {
    override val title: String = "SocketSensor ${Random.nextInt(0, 100)}"

    private val measurement = Measurement(this, 0, Measurement.Unit.METER).apply{
        description = "HeartRate"
    }
    override val measurements: List<Measurement> = listOf(measurement)

    private var socket: Socket? = null
    private var thread: Thread? = null

    @JsonProperty("hostname")
    @ConnectionProperty(title = "Hostname")
    lateinit var hostname: String

    @JsonProperty("port")
    @ConnectionProperty(title = "Port")
    var port: Int = 0

    override fun disconnect(reason: String?) {
        connected = false
        socket?.close()

        super.disconnect(reason)
    }

    override fun connect() {
        try {
            socket = Socket(hostname, port).apply {
                val reader = inputStream.bufferedReader()
                thread = thread(start = true) {
                    while (socket?.isClosed == true) {
                        try {
                            val line = reader.readLine()

                            val value = line?.toDoubleOrNull()

                            println("Read $line from $hostname:$port")

                            if (value == null) {
                                println("Invalid number was read in $title: '$line'")
                                continue
                            }

                            measurement.addDataPoint(value)
                        } catch (e: IOException) {
                            disconnect("Socket of $title had an IOException: ${e.message}")
                        }
                    }
                    disconnect("Socket of $title was closed")
                }
            }

            connected = true

            super.connect()
        } catch (e: ConnectException) {
            disconnect("Socket of $title failed to connect: ${e.message}")
        }
    }

}