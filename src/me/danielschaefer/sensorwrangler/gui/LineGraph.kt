package me.danielschaefer.sensorwrangler.gui

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import javafx.collections.ObservableList
import javafx.scene.chart.XYChart
import me.danielschaefer.sensorwrangler.Measurement


class LineGraph: AxisGraph() {
    @JsonIgnore
    override var mappedLists: MutableList<ObservableList<XYChart.Data<String, Number>>> = mutableListOf()

    @JsonProperty("title")
    override lateinit var title: String

    @JsonProperty("axisNames")
    lateinit override var axisNames: Array<String>

    @JsonProperty("yAxes")
    lateinit override var yAxes: List<Measurement>

    @JsonProperty("windowSize")
    override var windowSize: Int = 25
    @JsonProperty("lowerBound")
    override var lowerBound: Double = -10.0
    @JsonProperty("upperBound")
    override var upperBound: Double = 10.0
    @JsonProperty("tickSpacing")
    override var tickSpacing: Double = 1.0
}