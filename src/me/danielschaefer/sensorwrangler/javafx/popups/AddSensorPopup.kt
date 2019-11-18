package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.annotations.ConnectionProperty
import me.danielschaefer.sensorwrangler.annotations.SensorProperty
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.javafx.SensorTab
import me.danielschaefer.sensorwrangler.sensors.ConnectionChangeAdapter
import me.danielschaefer.sensorwrangler.sensors.ConnectionChangeListener
import me.danielschaefer.sensorwrangler.sensors.Sensor
import java.io.File
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf


class AddSensorPopup(val parentStage: Stage, val sensorTab: SensorTab? = null): Stage() {
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

       val sensorTypeSelection = ComboBox<String>().apply {
           for (supportedSensor in App.instance.settings.supportedSensors) {
               // FIXME: The names seem to be disappearing when selecting something else
               items.add(supportedSensor.simpleName)

               valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                   if (!newValue.equals(supportedSensor.simpleName))
                       return@ChangeListener

                   sensorConfiguration.children.clear()
                   connectionConfiguration.children.clear()

                   val mutableProperties = supportedSensor.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
                   val propertyMap: MutableMap<KMutableProperty<*>, () -> Any?> = mutableMapOf()
                   for (property in mutableProperties) {
                       for (annotation in property.annotations) {
                           val input: Node = when {
                               property.returnType.isSubtypeOf(String::class.createType()) -> {
                                   TextField().apply {
                                       propertyMap[property] = { text }
                                   }
                               }
                               property.returnType.isSubtypeOf(Long::class.createType()) -> {
                                   TextField().apply {
                                       propertyMap[property] = { text.toLong() }
                                   }
                               }
                               property.returnType.isSubtypeOf(Double::class.createType()) -> {
                                   TextField().apply {
                                       propertyMap[property] = { text.toDouble() }
                                   }
                               }
                               property.returnType.isSubtypeOf(Integer::class.createType()) -> {
                                   TextField().apply {
                                       propertyMap[property] = { text.toInt() }
                                   }
                               }
                               property.returnType.isSubtypeOf(Boolean::class.createType()) -> {
                                   CheckBox().apply {
                                       propertyMap[property] = { isSelected }
                                   }
                               }
                               property.returnType.isSubtypeOf(File::class.createType()) -> {
                                   HBox(10.0).apply {
                                       val fileLabel = Label()
                                       val fileButton = Button("Choose file").apply {
                                           setOnAction {
                                               val fileChooser = FileChooser()
                                               App.instance.settings.defaultFileSensorPath?.let {
                                                   fileChooser.initialDirectory = File(it)
                                               }
                                               fileChooser.showOpenDialog(this@AddSensorPopup)?.absolutePath?.let {
                                                   fileLabel.text = it
                                               }
                                           }
                                           propertyMap[property] = { File(fileLabel.text) }
                                       }
                                       children.addAll(fileLabel, fileButton)
                                   }
                               }
                               else -> TODO("Don't know how to handle this type")
                           }
                           when (annotation) {
                               is SensorProperty -> {
                                   val label = Label(annotation.title)
                                   sensorConfiguration.children.add(HBox(10.0, label, input))
                               }
                               is ConnectionProperty -> {
                                   val label = Label(annotation.title)
                                   connectionConfiguration.children.add(HBox(10.0, label, input))
                               }
                           }
                       }
                   }
                   sizeToScene()
                   addSensorButton.setOnAction {
                       val newSensor = supportedSensor.createInstance()
                       for ((property, getValue) in propertyMap) {
                           property.setter.call(newSensor, getValue())
                       }
                       App.instance.wrangler.sensors.add(newSensor)
                       sensorTab?.sensorList?.selectionModel?.select(sensorTab.sensorList.items.last())

                       close()
                   }
               })
           }
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