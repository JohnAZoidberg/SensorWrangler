package me.danielschaefer.sensorwrangler.gui

import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import me.danielschaefer.sensorwrangler.Measurement

abstract class AxisGraph: Chart() {
    abstract var axisNames: Array<String>
    abstract var yAxes: List<Measurement>

    abstract var windowSize: Int
    abstract var lowerBound: Double
    abstract var upperBound: Double
    abstract var tickSpacing: Double

    abstract var mappedLists: MutableList<ObservableList<XYChart.Data<String, Number>>>
}