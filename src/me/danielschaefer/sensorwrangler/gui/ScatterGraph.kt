package me.danielschaefer.sensorwrangler.gui

import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import me.danielschaefer.sensorwrangler.Measurement

class ScatterGraph(title: String, axisNames: Array<String>, yAxes: List<Measurement>) : AxisGraph(title, axisNames, yAxes) {
        override var mappedLists: MutableList<ObservableList<XYChart.Data<String, Number>>> = mutableListOf()

        override var windowSize: Int = 25
        override var lowerBound: Double = -10.0
        override var upperBound: Double = 10.0
        override var tickSpacing: Double = 1.0
}