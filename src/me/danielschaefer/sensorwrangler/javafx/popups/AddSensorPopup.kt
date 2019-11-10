package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Separator
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.gui.Graph
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.sensors.*

class AddSensorPopup(val parentStage: Stage): Stage() {
    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parentStage)

        scene = Scene(constructContent())
        title = "Add Sensor"

        sizeToScene()
        show()
    }

   private fun constructContent(): Parent {
       val sensorConfiguration = VBox()
       val sensorTypeSelection = ComboBox<Text>().apply {
           val sensorTypes = listOf(Text("RandomWalkSensor"), Text("RandomSensor"), Text("FileSensor"))
           items.setAll(sensorTypes)
           valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
               if (newValue == null)
                   return@ChangeListener

               sensorConfiguration.children.setAll(when (newValue.text) {
                   "RandomWalkSensor" -> Text("RandomWalkSensor options")
                   "RandomSensor" -> Text("RandomSensor options")
                   "FileSensor" -> Text("FileSensor options")
                   else -> Text()
               })
               println("Changed sensor selection from ${oldValue?.text} to ${newValue.text}")
           })
       }
       val sensorBox = VBox().apply {
           spacing = 10.0
           padding = Insets(10.0)

           children.addAll(Text("Sensor"), sensorTypeSelection, sensorConfiguration)
       }
       val connectionBox = VBox().apply {
           spacing = 10.0
           padding = Insets(10.0)
           children.addAll(Text("Connection"))
       }

       val addSensorButton = Button("Add sensor").apply {
           setOnAction {
               println("Selected to create ${sensorTypeSelection.value}")

               // TODO: Use reflection
               val newSensor: Sensor = when (sensorTypeSelection.value.text) {
                   "RandomWalkSensor" -> RandomWalkSensor()
                   "RandomSensor" -> RandomSensor()
                   "FileSensor" -> FileSensor("/home/zoid/media/clone/active/openant/heartrate.log")
                   else -> null as Sensor
               }

               newSensor.addConnectionChangeListener(object: ConnectionChangeAdapter() {
                   override fun onDisconnect(sensor: Sensor, reason: String?) {
                       reason?.let {
                           Alert(parentStage, "Sensor disconnected",
                               "Sensor ${sensor.title} was disconnected because of:\n$reason")
                       }
                       if (reason == null)
                           Alert(parentStage, "Sensor disconnected",
                               "Sensor ${sensor.title} was disconnected")
                   }
               })
               App.instance!!.wrangler.sensors.add(newSensor)

               val heartRateChart = Graph("Heart Rate", arrayOf("Time", "BPM"), newSensor.measurements[0]).apply {
                   windowSize = 25
                   lowerBound = 70.0
                   upperBound = 100.0
                   tickSpacing = 5.0
               }
               App.instance!!.wrangler.charts.add(heartRateChart)

               close()
           }
       }

       val separator = { Separator().apply {
           orientation = Orientation.HORIZONTAL
           padding = Insets(10.0)
       }}
       return VBox(sensorBox, separator(), connectionBox, separator(), addSensorButton).apply {
           padding = Insets(25.0)
       }
   }
}