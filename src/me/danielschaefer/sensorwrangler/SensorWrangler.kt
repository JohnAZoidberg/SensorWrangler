package me.danielschaefer.sensorwrangler

import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.sensors.Sensor

class SensorWrangler() {
    val sensors: MutableList<Sensor> = mutableListOf()
    val charts: MutableList<Chart> = mutableListOf()
}