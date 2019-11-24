package me.danielschaefer.sensorwrangler

import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonRootName
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.sensors.VirtualSensor
import java.util.*

// TODO: Maybe use Instant instead of Long
data class DataPoint(val timestamp: Long, val value: Double)

@JsonRootName(value = "Measurement")
class Measurement(val sensor: VirtualSensor, val indexInSensor: Int, val unit: Unit) {
    @JsonProperty("indexInSensor")
    val _indexInSensor = indexInSensor
    @JsonProperty("sensorUuid")
    var sensorUuid = sensor.uuid

    // TODO: Add description to constructor and think about what to do with Unit
    // TODO: Deserialize description
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
        val sensor = App.instance.wrangler.sensors.find { it.uuid == sensorUuid } as VirtualSensor

        // TODO: Why do I have to do it here and not in the Sensor class?
        sensor.measurements.forEach { it.sensorUuid = sensorUuid}
        return sensor.measurements[indexInSensor]
    }
}