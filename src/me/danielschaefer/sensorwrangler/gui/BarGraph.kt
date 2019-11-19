package me.danielschaefer.sensorwrangler.gui

import me.danielschaefer.sensorwrangler.Measurement

class BarGraph(title: String, val axisNames: Array<String>, val yAxes: List<Measurement>): Chart(title) {
    var lowerBound: Double = -10.0
    var upperBound: Double = 10.0
}