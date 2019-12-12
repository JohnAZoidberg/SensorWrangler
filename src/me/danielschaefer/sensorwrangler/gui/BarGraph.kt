package me.danielschaefer.sensorwrangler.gui

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.Measurement

class BarGraph: Chart() {
    @JsonProperty("title")
    override lateinit var title: String

    @JsonProperty("yAxisLabel")
    lateinit var yAxisLabel: String

    @JsonProperty("yAxes")
    lateinit var yAxes: List<Measurement>

    @JsonProperty("lowerBound")
    var lowerBound: Double = -10.0
    @JsonProperty("upperBound")
    var upperBound: Double = 10.0
}
