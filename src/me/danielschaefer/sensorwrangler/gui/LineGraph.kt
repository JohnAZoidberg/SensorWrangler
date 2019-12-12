package me.danielschaefer.sensorwrangler.gui

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.Measurement


class LineGraph: AxisGraph() {
    @JsonProperty("title")
    override lateinit var title: String

    @JsonProperty("yAxisLabel")
    override lateinit var yAxisLabel: String

    @JsonProperty("yAxes")
    override lateinit var yAxes: List<Measurement>

    @JsonProperty("windowSize")
    override var windowSize: Int = 25
    @JsonProperty("lowerBound")
    override var lowerBound: Double = -10.0
    @JsonProperty("upperBound")
    override var upperBound: Double = 10.0
    @JsonProperty("tickSpacing")
    override var tickSpacing: Double = 1.0
    @JsonProperty("withDots")
    var withDots: Boolean = false
}
