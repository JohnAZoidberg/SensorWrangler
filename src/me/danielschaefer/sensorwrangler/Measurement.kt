package me.danielschaefer.sensorwrangler

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.sensors.Sensor
import java.time.LocalTime
import java.util.*


@JsonRootName(value = "Measurement")
class Measurement(val sensor: Sensor, val indexInSensor: Int, val unit: Unit) {
    @JsonProperty("indexInSensor")
    val _indexInSensor = indexInSensor
    @JsonProperty("sensorUuid")
    val sensorUuid = sensor.uuid

    // TODO: Add description to constructor and think about what to do with Unit
    var description: String? = null
    var startDate: LocalTime? = null
    /**
     * How much time lies in between each measured value
     */
    var period: Long = 1
    val values: ObservableList<Double> = FXCollections.observableList(mutableListOf())

    // TODO: Actually use it
    enum class Unit {
        METER, SECOND
    }
}

class MeasurementDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<Measurement>(vc) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Measurement {
        val myMap: HashMap<*, *>? = ctxt?.readValue(jp, HashMap::class.java)
        val indexInSensor = myMap?.get("indexInSensor") as Int
        val sensorUuid = myMap?.get("sensorUuid") as String
        val sensor = App.instance.wrangler.sensors.find { it.uuid == sensorUuid } as Sensor
        return sensor.measurements[indexInSensor]
    }
}