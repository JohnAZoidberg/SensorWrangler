package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.sensors.*
import java.io.File


class AddSensorPopup(val parentStage: Stage): Stage() {
    init {
        initOwner(parentStage)

        scene = Scene(constructContent())
        title = "Add Sensor"

        sizeToScene()
        show()
    }

   private fun constructContent(): Parent {
       val addSensorButton = Button("Add sensor")
       val sensorConfiguration = VBox(10.0)
       val connectionConfiguration = VBox(10.0)
       val sensorTypeSelection = ComboBox<Text>().apply {
           // FIXME: The names seem to be disappearing when selecting something else
           val sensorTypes = listOf(Text("RandomWalkSensor"), Text("RandomSensor"), Text("FileSensor"))
           items.setAll(sensorTypes)
           valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
               if (newValue == null)
                   return@ChangeListener

               when (newValue.text) {
                   "RandomWalkSensor" -> {
                       val updateIntervalField = TextField("250")
                       sensorConfiguration.children.setAll(Text("RandomWalkSensor options"), updateIntervalField)
                       sizeToScene()
                       addSensorButton.setOnAction {
                           val newSensor = RandomWalkSensor(updateIntervalField.text.toLong())
                           newSensor.addConnectionChangeListener(createConnectionChangeListener())
                           App.instance!!.wrangler.sensors.add(newSensor)
                           close()
                       }
                   }
                   "RandomSensor" -> {
                       val updateIntervalField = TextField("250")
                       sensorConfiguration.children.setAll(Text("RandomSensor options"), updateIntervalField)
                       sizeToScene()
                       addSensorButton.setOnAction {
                           val newSensor = RandomSensor(updateIntervalField.text.toLong())
                           newSensor.addConnectionChangeListener(createConnectionChangeListener())
                           App.instance!!.wrangler.sensors.add(newSensor)
                           close()
                       }
                   }
                   "FileSensor" -> {
                       val fileLabel = Label("")
                       val fileChooser = FileChooser()
                       App.instance!!.settings.defaultFileSensorPath?.let {
                           fileChooser.initialDirectory = File(it)
                       }
                       val fileChooserButton = Button("Choose File").apply {
                           setOnAction {
                               fileChooser.showOpenDialog(this@AddSensorPopup)?.absolutePath?.let {
                                   fileLabel.text = it
                               }
                           }
                       }

                       sensorConfiguration.children.setAll(Text("FileSensor options"))
                       connectionConfiguration.children.setAll(fileLabel, fileChooserButton)
                       sizeToScene()
                       addSensorButton.setOnAction {
                           val newSensor = FileSensor(fileLabel.text)
                           newSensor.addConnectionChangeListener(createConnectionChangeListener())
                           App.instance!!.wrangler.sensors.add(newSensor)
                           close()
                       }
                   }
               }
               println("Changed sensor selection from ${oldValue?.text} to ${newValue.text}")
           })
       }
       val sensorBox = VBox().apply {
           spacing = 10.0
           padding = Insets(10.0)

           children.addAll(Text("Sensor options"), sensorTypeSelection, sensorConfiguration)
       }
       val connectionBox = VBox().apply {
           spacing = 10.0
           padding = Insets(10.0)
           children.addAll(Text("Connection options"), connectionConfiguration)
       }

       val separator = { Separator().apply {
           orientation = Orientation.HORIZONTAL
           padding = Insets(10.0)
       }}
       return VBox(sensorBox, separator(), connectionBox, separator(), addSensorButton).apply {
           padding = Insets(25.0)
       }
   }

    private fun createConnectionChangeListener(): ConnectionChangeListener {
        return object: ConnectionChangeAdapter() {
            override fun onDisconnect(sensor: Sensor, reason: String?) {
                Platform.runLater {
                    reason?.let {
                        Alert(
                            parentStage, "Sensor disconnected",
                            "Sensor ${sensor.title} was disconnected because of:\n$reason"
                        )
                    }
                    if (reason == null)
                        Alert(
                            parentStage, "Sensor disconnected",
                            "Sensor ${sensor.title} was disconnected"
                        )
                }
            }
        }
    }
}