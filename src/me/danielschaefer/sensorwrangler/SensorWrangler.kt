package me.danielschaefer.sensorwrangler

import javafx.collections.ListChangeListener
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.sensors.Sensor
import java.io.FileWriter


class SensorWrangler() {
    val sensors: MutableList<Sensor> = mutableListOf()
    val charts: MutableList<Chart> = mutableListOf()

    fun startRecording(logPath: String) {
        val fstream = FileWriter(logPath, true)
        fstream.write("Sensor,Measurement,Value\n")
        for (sensor in sensors) {
            for (measurement in sensor.measurements) {
                measurement.values.addListener(ListChangeListener {
                    it.next()
                    for (x in it.addedSubList) {
                        fstream.write("${sensor.title},${measurement.description},$x\n")
                    }
                    fstream.flush()
                })
            }
        }
    }
    fun stopRecording() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    // TODO: Do we want charts to be a map indexed by the title?
    fun findChartByTitle(title: String): Chart? {
        return charts.filter { it.title == title }[0]
    }
}