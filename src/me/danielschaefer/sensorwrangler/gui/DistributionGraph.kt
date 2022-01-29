package me.danielschaefer.sensorwrangler.gui

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.data.Chart
import me.danielschaefer.sensorwrangler.data.Measurement

abstract class DistributionGraph : Chart() {
    @JsonProperty("title")
    override lateinit var title: String

    @JsonProperty("axis")
    lateinit var axis: Measurement
}
