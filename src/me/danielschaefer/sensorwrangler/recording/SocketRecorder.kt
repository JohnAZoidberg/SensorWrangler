package me.danielschaefer.sensorwrangler.recording

import me.danielschaefer.sensorwrangler.data.Measurement
import me.danielschaefer.sensorwrangler.data.Recorder
import mu.KotlinLogging
import java.io.OutputStreamWriter
import java.net.ServerSocket
import java.net.Socket
import java.net.SocketException
import kotlin.concurrent.thread

private val logger = KotlinLogging.logger {}

class SocketRecorder(port: Int) : Recorder {
    private var socket: ServerSocket = ServerSocket(port)
    private var conn: Socket? = null
    private var ofStream: OutputStreamWriter? = null

    init {
        thread(start = true) {
            // TODO: Start listening again after a client disconnects
            if (conn == null)
                conn = socket.accept()

            if (ofStream == null)
                ofStream = conn!!.outputStream!!.writer()
        }
    }

    override fun recordValue(timestamp: String, measurement: Measurement, value: Double) {
        // TODO: Check socket.isConnected and socket.isClosed
        // Timestamp,Sensor,Measurement,Value\n
        try {
            ofStream?.write("${measurement.sensor.title},${measurement.description},$timestamp,${value}\n")
            ofStream?.flush()
        } catch (e: SocketException) {
            logger.info { "SocketRecorder disconnected because of ${e.message}" }
            // TODO: Maybe it should remove itself from the list of recorders?
            close()
        }
    }

    override fun close() {
        conn?.close()
        socket.close()
    }
}
