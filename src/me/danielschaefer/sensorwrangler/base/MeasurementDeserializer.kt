package me.danielschaefer.sensorwrangler.base

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.deser.std.StdDeserializer
import me.danielschaefer.sensorwrangler.data.Measurement
import me.danielschaefer.sensorwrangler.data.VirtualSensor
import me.danielschaefer.sensorwrangler.sensors.Averager

class MeasurementDeserializer @JvmOverloads constructor(vc: Class<*>? = null) : StdDeserializer<Measurement>(vc) {
    override fun deserialize(jp: JsonParser, ctxt: DeserializationContext?): Measurement {
        val myMap: HashMap<*, *>? = ctxt?.readValue(jp, HashMap::class.java)
        val indexInSensor = myMap?.get("indexInSensor") as Int
        val sensorUuid = myMap.get("sensorUuid") as String
        val sensor = App.instance.wrangler.sensors.find { it.uuid == sensorUuid } as VirtualSensor

        if (sensor is Averager) {
            // This assumes that the sensors have been deserialized before this measurement
            sensor.connect()
        }

        // TODO: Why do I have to do it here and not in the Sensor class?
        sensor.measurements.forEach { it.sensorUuid = sensorUuid }
        return sensor.measurements[indexInSensor]
    }
}
