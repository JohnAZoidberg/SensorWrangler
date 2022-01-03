package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.CheckBox
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.control.ListView
import javafx.scene.control.ProgressIndicator
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.annotations.ConnectionProperty
import me.danielschaefer.sensorwrangler.annotations.SensorProperty
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.javafx.JavaFXUtil
import me.danielschaefer.sensorwrangler.javafx.SensorTab
import me.danielschaefer.sensorwrangler.sensors.AntPlusSensor
import me.danielschaefer.sensorwrangler.sensors.AntScanResult
import me.danielschaefer.sensorwrangler.sensors.ScanResult
import me.danielschaefer.sensorwrangler.sensors.Scannable
import me.danielschaefer.sensorwrangler.sensors.Sensor
import java.io.File
import kotlin.reflect.KClass
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.createInstance
import kotlin.reflect.full.createType
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.full.isSubclassOf
import kotlin.reflect.full.isSubtypeOf

class AddSensorPopup(val parentStage: Stage, val sensorTab: SensorTab? = null) : Stage() {
    init {
        initOwner(parentStage)

        scene = Scene(constructContent())
        title = "Add Sensor"

        sizeToScene()
        show()
    }

    private lateinit var scanResultList: ListView<ScanResult>

    private lateinit var scanButton: Button
    private lateinit var scanBox: VBox
    private var currentSensor: KClass<out Sensor>? = null
    private lateinit var useScanToggle: CheckBox
    private lateinit var scanningIndicator: ProgressIndicator

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

        val sensorTypeSelection = ComboBox<KClass<out Sensor>>().apply {
            items.addAll(App.instance.settings.supportedSensors)
            converter = JavaFXUtil.createSimpleClassStringConverter<Sensor>()

            valueProperty().addListener(
                ChangeListener { _, _, selectedSensor ->
                    currentSensor = selectedSensor

                    sensorConfiguration.children.clear()
                    var sensorRow = 0
                    connectionConfiguration.children.clear()
                    var connectionRow = 0

                    val isScannable = selectedSensor.isSubclassOf(Scannable::class)
                    scanBox.isVisible = isScannable
                    scanButton.isVisible = isScannable
                    scanningIndicator.isVisible = isScannable

                    // TODO: Scan only for selected sensor type
                    // scanResultList.items.clear()

                    val mutableProperties = selectedSensor.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>()
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
                        if (addSensor(selectedSensor, propertyMap))
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

        scanResultList = ListView<ScanResult>().apply {
            prefHeight = 24.0 * 3
        }

        useScanToggle = CheckBox()
        scanBox = VBox().apply {
            spacing = 10.0
            padding = Insets(10.0)

            children.addAll(scanResultList)
            isVisible = false
        }

        scanningIndicator = ProgressIndicator().apply {
            progress = 0.0
            isVisible = false
        }
        val buttonBox = HBox().apply {
            spacing = 10.0
            padding = Insets(10.0)
            scanButton = Button("Scan").apply {
                isVisible = false
                setOnAction {
                    currentSensor?.let { cls ->
                        val scanner = (cls.createInstance() as Scannable<ScanResult>)
                        scanner.getScanStatusProperty().addListener { _, oldScanning, newScanning ->
                            Platform.runLater {
                                if (!oldScanning && newScanning) {
                                    scanningIndicator.progress = -1.0
                                    scanButton.isDisable = true
                                }

                                if (oldScanning && !newScanning) {
                                    scanningIndicator.progress = 1.0
                                    scanButton.isDisable = false
                                }
                            }
                        }
                        scanner.scan(scanResultList.items)
                    }
                    sizeToScene()
                }
            }
            children.addAll(addSensorButton, scanButton, scanningIndicator)
        }

        return VBox(10.0, sensorTypeSelection, sensorBox, connectionBox, scanBox, buttonBox).apply {
            padding = Insets(25.0)
        }
    }

    private fun addSensor(selectedSensor: KClass<out Sensor>, propertyMap: MutableMap<KMutableProperty<*>, () -> Any?>): Boolean {
        val newSensor = selectedSensor.createInstance()
        for ((property, getValue) in propertyMap) {
            val propertyValue = getValue()
            if (propertyValue == null) {
                // TODO: Show what the actual problem is
                Alert(parentStage, "Form invalid", "The form isn't properly filled.")
                return false
            }

            property.setter.call(newSensor, propertyValue)
        }

        if (newSensor is Scannable<*>) {
            val selectedScanResult = scanResultList.selectionModel.selectedItem
            if (selectedScanResult == null) {
                Alert(parentStage, "Form invalid", "Scanning must be performed and a sensor selected")
                return false
            }

            if (selectedScanResult is AntScanResult && newSensor is AntPlusSensor<*> &&
                selectedScanResult.channelId.deviceType != newSensor.deviceType
            ) {
                Alert(parentStage, "Form invalid", "Device type must match sensor type")
                return false
            }

            (newSensor as Scannable<ScanResult>).configureScan(selectedScanResult)
        }

        App.instance.wrangler.sensors.add(newSensor)

        // Select the new sensor if SensorTab is currently shown
        sensorTab?.sensorList?.selectionModel?.select(sensorTab.sensorList.items.last())

        return true
    }
}
