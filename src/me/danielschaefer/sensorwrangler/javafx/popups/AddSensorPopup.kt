package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.annotations.ConnectionProperty
import me.danielschaefer.sensorwrangler.annotations.SensorProperty
import me.danielschaefer.sensorwrangler.base.App
import me.danielschaefer.sensorwrangler.data.Measurement
import me.danielschaefer.sensorwrangler.data.VirtualSensor
import me.danielschaefer.sensorwrangler.javafx.JavaFXUtil
import me.danielschaefer.sensorwrangler.javafx.SensorTab
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubtypeOf

class AddSensorPopup(val parentStage: Stage, val sensorTab: SensorTab? = null) : Stage() {
    init {
        initOwner(parentStage)

        scene = Scene(constructContent())
        title = "Add Sensor"

        sizeToScene()
        show()
    }

    private fun constructContent(): Parent {
        val addSensorButton = Button("Add sensor")

        // TODO: Maybe we can have both in a single table, so the columns are all one size
        val sensorConfiguration = GridPane().apply {
            padding = Insets(25.0)
            hgap = 10.0
            vgap = 10.0
        }

        val connectionConfiguration = GridPane().apply {
            padding = Insets(25.0)
            hgap = 10.0
            vgap = 10.0
        }

        val sensorTypeSelection = ComboBox<KClass<out VirtualSensor>>().apply {
            items.addAll(App.instance.settings.supportedSensors)
            converter = JavaFXUtil.createSimpleClassStringConverter<VirtualSensor>()

            valueProperty().addListener(
                ChangeListener { _, _, newChart ->
                    sensorConfiguration.children.clear()
                    var sensorRow = 0
                    connectionConfiguration.children.clear()
                    var connectionRow = 0

                    val mutableProperties = newChart.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
                    val propertyMap: MutableMap<KMutableProperty<*>, () -> Any?> = mutableMapOf()
                    for (property in mutableProperties) {
                        for (annotation in property.annotations) {
                            val input: Node = when {
                                property.returnType.isSubtypeOf(String::class.createType()) -> {
                                    TextField().apply {
                                        propertyMap[property] = {
                                            if (text.isEmpty()) null else text
                                        }
                                    }
                                }
                                property.returnType.isSubtypeOf(Long::class.createType()) -> {
                                    TextField().apply {
                                        propertyMap[property] = { text.toLongOrNull() }
                                    }
                                }
                                property.returnType.isSubtypeOf(Double::class.createType()) -> {
                                    TextField().apply {
                                        propertyMap[property] = { text.toDoubleOrNull() }
                                    }
                                }
                                property.returnType.isSubtypeOf(Int::class.createType()) -> {
                                    TextField().apply {
                                        propertyMap[property] = { text.toIntOrNull() }
                                    }
                                }
                                property.returnType.isSubtypeOf(Boolean::class.createType()) -> {
                                    CheckBox().apply {
                                        propertyMap[property] = { isSelected }
                                    }
                                }
                                property.returnType.isSubtypeOf(Measurement.Unit::class.createType()) -> {
                                    ComboBox<Measurement.Unit>().apply {
                                        items.addAll(Measurement.Unit.values())
                                        propertyMap[property] = { value }
                                    }
                                }
                                property.returnType.isSubtypeOf(File::class.createType()) -> {
                                    HBox(10.0).apply {
                                        val fileLabel = Label()
                                        val fileButton = Button("Choose file").apply {
                                            setOnAction {
                                                val fileChooser = FileChooser()
                                                App.instance.settings.defaultFileSensorDirectory?.let {
                                                    val file = File(it)
                                                    fileChooser.initialDirectory = file.parentFile
                                                    fileChooser.initialFileName = file.name
                                                }
                                                fileChooser.showOpenDialog(this@AddSensorPopup)?.absolutePath?.let {
                                                    fileLabel.text = it
                                                }
                                            }
                                            propertyMap[property] = { fileLabel.text?.let { File(it) } }
                                        }
                                        children.addAll(fileLabel, fileButton)
                                    }
                                }
                                else -> TODO("Don't know how to handle this type")
                            }
                            when (annotation) {
                                is SensorProperty -> {
                                    val label = Label(annotation.title)
                                    sensorConfiguration.add(label, 0, sensorRow)
                                    sensorConfiguration.add(input, 1, sensorRow)
                                    sensorRow++
                                }
                                is ConnectionProperty -> {
                                    val label = Label(annotation.title)

                                    if (input is TextField)
                                        input.text = annotation.default

                                    connectionConfiguration.add(label, 0, connectionRow)
                                    connectionConfiguration.add(input, 1, connectionRow)
                                    connectionRow++
                                }
                            }
                        }
                    }
                    addSensorButton.setOnAction {
                        val newSensor = newChart.createInstance()
                        for ((property, getValue) in propertyMap) {
                            val propertyValue = getValue()
                            if (propertyValue == null) {
                                // TODO: Show what the actual problem is
                                Alert(parentStage, "Form invalid", "The form isn't properly filled.")
                                return@setOnAction
                            }

                            property.setter.call(newSensor, propertyValue)
                        }
                        App.instance.wrangler.sensors.add(newSensor)

                        // Select the new sensor if SensorTab is currently shown
                        sensorTab?.sensorList?.selectionModel?.select(sensorTab.sensorList.items.last())

                        close()
                    }
                    sizeToScene()
                }
            )
        }
        val sensorBox = VBox().apply {
            spacing = 10.0
            padding = Insets(10.0)

            children.addAll(Text("Sensor options"), sensorConfiguration)
        }
        val connectionBox = VBox().apply {
            spacing = 10.0
            padding = Insets(10.0)
            children.addAll(Text("Connection options"), connectionConfiguration)
        }

        return VBox(10.0, sensorTypeSelection, sensorBox, connectionBox, addSensorButton).apply {
            padding = Insets(25.0)
        }
    }
}
