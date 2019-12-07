package me.danielschaefer.sensorwrangler.gui

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.Measurement

class CurrentValueGraph: Chart() {
    @JsonProperty("title")
    override lateinit var title: String

    @JsonProperty("axes")
    lateinit var axes: List<Measurement>
}
