package me.danielschaefer.sensorwrangler.gui

import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import me.danielschaefer.sensorwrangler.Measurement

abstract class AxisGraph(title: String, val axisNames: Array<String>, val yAxis: Measurement? = null): Chart(title) {
    abstract var windowSize: Int
    abstract var lowerBound: Double
    abstract var upperBound: Double
    abstract var tickSpacing: Double

    abstract var mappedList: ObservableList<XYChart.Data<String, Number>>?
}