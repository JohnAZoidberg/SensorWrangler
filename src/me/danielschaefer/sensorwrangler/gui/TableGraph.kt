package me.danielschaefer.sensorwrangler.gui

import com.fasterxml.jackson.annotation.JsonProperty
import me.danielschaefer.sensorwrangler.Measurement

class TableGraph : Chart() {
    @JsonProperty("title")
    override lateinit var title: String

    @JsonProperty("axes")
    lateinit var axes: List<Measurement>

    /**
     * Unit: Milliseconds
     */
    var lastNAvgWindow = 60_000
}
