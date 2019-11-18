package me.danielschaefer.sensorwrangler.gui

import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import me.danielschaefer.sensorwrangler.Measurement

abstract class AxisGraph(title: String, val axisNames: Array<String>, val yAxes: List<Measurement>): Chart(title) {
    abstract var windowSize: Int
    abstract var lowerBound: Double
    abstract var upperBound: Double
    abstract var tickSpacing: Double

    abstract var mappedLists: MutableList<ObservableList<XYChart.Data<String, Number>>>
}