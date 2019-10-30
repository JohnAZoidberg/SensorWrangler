package me.danielschaefer.sensorwrangler.gui

import me.danielschaefer.sensorwrangler.Measurement


class Graph(title: String, val axisNames: Array<String>, val yAxis: Measurement? = null) : Chart(title) {
    var windowSize: Int = 50

    init {
        if (yAxis != null) {
            println("Graph $title was initialized with ${yAxis.values}")
        } else {
            println("Graph $title wasn't initialized")
        }
    }
}