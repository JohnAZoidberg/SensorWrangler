package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.popups.Alert
import me.danielschaefer.sensorwrangler.sensors.ConnectionChangeAdapter
import me.danielschaefer.sensorwrangler.sensors.ConnectionChangeListener
import me.danielschaefer.sensorwrangler.sensors.Sensor

object JavaFXUtil {
    @JvmStatic
    fun createConnectionChangeListener(parentStage: Stage): ConnectionChangeListener {
        return object : ConnectionChangeAdapter() {
            override fun onDisconnect(sensor: Sensor, reason: String?) {
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
    }
}
