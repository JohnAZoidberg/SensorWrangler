package me.danielschaefer.sensorwrangler.base

import me.danielschaefer.sensorwrangler.gui.AngleGraph
import me.danielschaefer.sensorwrangler.gui.BarDistributionGraph
import me.danielschaefer.sensorwrangler.gui.BarGraph
import me.danielschaefer.sensorwrangler.gui.LineGraph
import me.danielschaefer.sensorwrangler.gui.PieDistributionGraph
import me.danielschaefer.sensorwrangler.gui.ScatterGraph
import me.danielschaefer.sensorwrangler.gui.TableGraph
import me.danielschaefer.sensorwrangler.recording.CsvRecorder
import me.danielschaefer.sensorwrangler.recording.DatabaseRecorder
import me.danielschaefer.sensorwrangler.recording.SocketRecorder
import me.danielschaefer.sensorwrangler.sensors.AntCadenceSensor
import me.danielschaefer.sensorwrangler.sensors.AntHeartRateSensor
import me.danielschaefer.sensorwrangler.sensors.AntPowerSensor
import me.danielschaefer.sensorwrangler.sensors.AntSpeedSensor
import me.danielschaefer.sensorwrangler.sensors.AntStationaryBike
import me.danielschaefer.sensorwrangler.sensors.FileSensor
import me.danielschaefer.sensorwrangler.sensors.RandomSensor
import me.danielschaefer.sensorwrangler.sensors.RandomWalkSensor
import me.danielschaefer.sensorwrangler.sensors.Sensor
import me.danielschaefer.sensorwrangler.sensors.SocketSensor
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class App {
    val wrangler = SensorWrangler()
    val settings = Settings()

    init {
        settings.supportedSensors.add(AntCadenceSensor::class)
        settings.supportedSensors.add(AntHeartRateSensor::class)
        settings.supportedSensors.add(AntPowerSensor::class)
        settings.supportedSensors.add(AntSpeedSensor::class)
        settings.supportedSensors.add(AntStationaryBike::class)
        settings.supportedSensors.add(FileSensor::class)
        settings.supportedSensors.add(RandomSensor::class)
        settings.supportedSensors.add(RandomWalkSensor::class)
        settings.supportedSensors.add(Sensor::class)
        settings.supportedSensors.add(SocketSensor::class)

        settings.supportedRecorders.add(CsvRecorder::class)
        settings.supportedRecorders.add(DatabaseRecorder::class)
        settings.supportedRecorders.add(SocketRecorder::class)

        settings.supportedCharts.add(AngleGraph::class)
        settings.supportedCharts.add(BarDistributionGraph::class)
        settings.supportedCharts.add(BarGraph::class)
        settings.supportedCharts.add(LineGraph::class)
        settings.supportedCharts.add(PieDistributionGraph::class)
        settings.supportedCharts.add(ScatterGraph::class)
        settings.supportedCharts.add(TableGraph::class)
    }

    companion object {
        val instance = App()
    }
}
