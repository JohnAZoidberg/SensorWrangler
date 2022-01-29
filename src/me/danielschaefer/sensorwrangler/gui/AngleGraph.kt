package me.danielschaefer.sensorwrangler.gui

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.data.Chart
import me.danielschaefer.sensorwrangler.data.Measurement

class AngleGraph : Chart() {
    @JsonProperty("title")
    override lateinit var title: String

    @JsonProperty("axis")
    lateinit var axis: Measurement

    @JsonProperty("horizontal")
    var horizontal: Boolean = true
}
