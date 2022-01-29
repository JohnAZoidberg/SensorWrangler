package me.danielschaefer.sensorwrangler.data

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import java.util.Date

@JsonRootName(value = "Measurement")
class Measurement(val sensor: VirtualSensor, val indexInSensor: Int, val unit: Unit) {
    @JsonProperty("indexInSensor")
    val _indexInSensor = indexInSensor
    @JsonProperty("sensorUuid")
    var sensorUuid = sensor.uuid

    // TODO: Add description to constructor and think about what to do with Unit
    // TODO: Deserialize description
    // TODO: Why/when would it be null?
    var description: String? = null
    var startDate: Long? = null

    // TODO: Create something like the ReadonlyBooleanproperty a ImmutableObversableList
    val dataPoints: ObservableList<DataPoint> = FXCollections.observableList(mutableListOf())

    private fun addDataPoint(point: DataPoint) {
        if (startDate == null)
            startDate = point.timestamp

        dataPoints.add(point)
    }

    private fun addDataPoint(datetime: Long, value: Double) {
        addDataPoint(DataPoint(datetime, value))
    }

    fun addDataPoint(value: Double) {
        addDataPoint(Date().time, value)
    }

    override fun toString(): String {
        return description ?: super.toString()
    }

    enum class Unit {
        BPM,
        DEGREE,
        METER,
        METER_PER_SECOND,
        PERCENTAGE,
        RPM,
        UNITLESS,
        WATT;

        override fun toString(): String {
            return when (this) {
                BPM -> "BPM"
                DEGREE -> "Degree"
                METER -> "Meter"
                METER_PER_SECOND -> "Meter per second"
                PERCENTAGE -> "Percentage"
                RPM -> "RPM"
                UNITLESS -> "Unitless"
                WATT -> "Watt"
            }
        }

        val abbreviation: String
            get() = when (this) {
                BPM -> "BPM"
                DEGREE -> "°"
                METER -> "m"
                METER_PER_SECOND -> "m/s"
                PERCENTAGE -> "%"
                RPM -> "RPM"
                UNITLESS -> ""
                WATT -> "W"
            }

        val unitAppendix: String
            get() = when (this) {
                UNITLESS -> ""
                else -> " [ $abbreviation ]"
            }
    }
}
