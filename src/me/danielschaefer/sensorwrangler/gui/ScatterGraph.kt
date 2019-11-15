package me.danielschaefer.sensorwrangler.gui

import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import me.danielschaefer.sensorwrangler.Measurement

class ScatterGraph(title: String, axisNames: Array<String>, yAxis: Measurement? = null) : AxisGraph(title, axisNames, yAxis) {
        override var mappedList: ObservableList<XYChart.Data<String, Number>>? = null

        override var windowSize: Int = 25
        override var lowerBound: Double = -10.0
        override var upperBound: Double = 10.0
        override var tickSpacing: Double = 1.0

        init {
            if (yAxis != null) {
                println("ScatterChart $title was initialized with ${yAxis.values}")
            } else {
                println("ScatterChart $title wasn't initialized")
            }
        }
}