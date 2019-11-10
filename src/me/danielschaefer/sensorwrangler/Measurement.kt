package me.danielschaefer.sensorwrangler

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import me.danielschaefer.sensorwrangler.sensors.Sensor
import java.time.LocalTime

class Measurement(val sensor: Sensor, val unit: Unit) {
    var description: String? = null
    var startDate: LocalTime? = null
    /**
     * How much time lies in between each measured value
     */
    var period: Long = 1
    val values: ObservableList<Double> = FXCollections.observableList(mutableListOf())

    enum class Unit {
        METER, SECOND
    }
}
