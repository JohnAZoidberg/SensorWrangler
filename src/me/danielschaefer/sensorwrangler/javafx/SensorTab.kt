package me.danielschaefer.sensorwrangler.javafx

import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.popups.AddSensorPopup
import me.danielschaefer.sensorwrangler.javafx.popups.Alert
import me.danielschaefer.sensorwrangler.sensors.*

class SensorTab(parentStage: Stage): Tab("Sensors") {
    init {
        content = HBox().apply {
            val sensorDetail = VBox(10.0).apply {
                HBox.setHgrow(this, Priority.SOMETIMES)
            }

            val sensorList = ListView<Text>().apply {
                items = FXCollections.observableList(mutableListOf())
                items.setAll(App.instance!!.wrangler.sensors.map { Text(it.title) })
                App.instance!!.wrangler.sensors.addListener(ListChangeListener {
                    items.setAll(it.list.map { Text(it.title) })
                })

                // TODO: Cache these for better performance
                selectionModel.selectedItemProperty().addListener(ChangeListener { x, oldValue, newValue ->
                    if (newValue == null)
                        return@ChangeListener

                    // TODO: Pass Sensor object to avoid searching and possible failure
                    val sensor = App.instance!!.wrangler.findSensorByTitle(newValue.text)
                    sensor?.let {
                        val sensorDetailTable = TableView<TableRow>().apply {
                            // Have columns expand to fill all available space
                            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

                            val firstCol = TableColumn<TableRow, Text>().apply {
                                cellValueFactory = PropertyValueFactory("firstName")
                                isSortable = false
                                isReorderable = false
                            }
                            val secondCol = TableColumn<TableRow, Text>().apply {
                                cellValueFactory = PropertyValueFactory("lastName")
                                isSortable = false
                                isReorderable = false
                            }
                            columns.setAll(firstCol, secondCol)

                            items.clear()
                            items.add(TableRow("Title", sensor.title))

                            // TODO: Figure out how to generate from the class definition
                            //       Can/should it be done without reflection?
                            // Information about a specific type of sensor
                            when (sensor) {
                                is FileSensor -> {
                                    items.add(TableRow("File path", sensor.filePath))
                                }
                                is RandomSensor -> {
                                    items.add(TableRow("Update interval [ms]", sensor.updateInterval.toString()))
                                    items.add(TableRow("Minimum value", sensor.minValue.toString()))
                                    items.add(TableRow("Maximum value", sensor.maxValue.toString()))
                                }
                                is RandomWalkSensor -> {
                                    items.add(TableRow("Update interval [ms]", sensor.updateInterval.toString()))
                                    items.add(TableRow("Minimum value", sensor.minValue.toString()))
                                    items.add(TableRow("Maximum value", sensor.maxValue.toString()))
                                    items.add(TableRow("Maximum step distance", sensor.maxStep.toString()))
                                }
                            }

                            items.add(TableRow("Measurements", sensor.measurements.size.toString()))
                            items.addAll(sensor.measurements.map {
                                TableRow("", it.description )
                            })
                        }

                        // TODO: Handle the default case better
                        val connectButton = Button("Connect").apply {
                            setOnAction {
                                sensor.connect()
                            }
                        }
                        sensor.addConnectionChangeListener(object : ConnectionChangeListener {
                            override fun onConnect() {
                                connectButton.text = "Disconnect"
                                connectButton.setOnAction {
                                    // TODO: Add popup for connection dialog
                                    sensor.disconnect()
                                }
                            }

                            override fun onDisconnect(sensor: Sensor, reason: String?) {
                                connectButton.text = "Connect"
                                connectButton.setOnAction {
                                    sensor.connect()
                                }
                            }

                        })

                        val removeSensorButton = Button("Remove Sensor").apply {
                            setOnAction {
                                if (sensor.isConnected)
                                    Alert(parentStage, "Sensor is connected",
                                        "Please disconnect the sensor before removing it.")
                                else
                                    App.instance!!.wrangler.sensors.remove(sensor)
                            }
                        }

                        val sensorButtons = HBox(10.0, connectButton, removeSensorButton)
                        sensorDetail.children.setAll(sensorDetailTable, sensorButtons)
                    }
                })
            }

            val addSensorButton = Button("Add Sensor").apply {
                setOnAction {
                    AddSensorPopup(parentStage)
                }
            }
            val sensorListSidebar = VBox(10.0, sensorList, addSensorButton)
            sensorListSidebar.children.add(sensorDetail)

            val separator = Separator().apply {
                orientation = Orientation.VERTICAL
                padding = Insets(10.0)
            }
            children.addAll(sensorListSidebar, separator, sensorDetail)
        }
    }
}
