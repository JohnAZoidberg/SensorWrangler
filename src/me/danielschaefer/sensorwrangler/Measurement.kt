package me.danielschaefer.sensorwrangler

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.time.LocalTime

class Measurement(unit: Unit) {
    var description: String? = null
    val unit: Unit = unit
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
