package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.stage.Stage
import javafx.util.StringConverter
import me.danielschaefer.sensorwrangler.javafx.popups.Alert
import me.danielschaefer.sensorwrangler.sensors.ConnectionChangeListener
import kotlin.reflect.KClass

object JavaFXUtil {
    @JvmStatic
    fun createConnectionChangeListener(parentStage: Stage): ConnectionChangeListener {
        return ConnectionChangeListener { sensor, connected, reason ->
            // No alerts necessary for a connected sensor
            if (connected)
                return@ConnectionChangeListener

            // Could be called from a non-UI thread
            Platform.runLater {
                val alertText = if (reason == null)
                    "Sensor ${sensor.title} was disconnected"
                else
                    "Sensor ${sensor.title} was disconnected because of:\n$reason"

                Alert(parentStage, "Sensor disconnected", alertText)
            }
        }
    }

    @JvmStatic
    fun <T : Any> createSimpleClassStringConverter(): StringConverter<KClass<out T>> {
        return object : StringConverter<KClass<out T>>() {
            override fun toString(value: KClass<out T>?): String? {
                return value?.simpleName
            }

            override fun fromString(string: String?): KClass<out T>? {
                // TODO: Is this really necessary?
                return null
            }
        }
    }
}
