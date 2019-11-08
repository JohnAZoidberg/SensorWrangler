package me.danielschaefer.sensorwrangler.javafx

import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.popups.TodoAlert

class SensorTab(parentStage: Stage): Tab("Sensors") {
    init {
        content = HBox().apply {
            val sensorDetail = VBox().apply {
                HBox.setHgrow(this, Priority.SOMETIMES)
            }

            val sensorList = ListView<Text>().apply {
                val sensors: ObservableList<Text> = FXCollections.observableList(mutableListOf())

                for (sensor in App.instance!!.wrangler.sensors) {
                    sensors.add(Text(sensor.title))
                }

                items = sensors

                // TODO: Cache these for better performance
                selectionModel.selectedItemProperty().addListener(ChangeListener { x, oldValue, newValue ->
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

                            items.setAll(
                                TableRow("Title", sensor.title),
                                TableRow("Measurements", sensor.measurements.size.toString())
                            )
                            items.addAll(sensor.measurements.map {
                                TableRow("", it.description )
                            })
                        }

                        val disconnectButton = Button("Disconnect").apply {
                            setOnAction {
                                TodoAlert(parentStage)
                            }
                        }

                        sensorDetail.children.setAll(sensorDetailTable, disconnectButton)
                    }
                })
            }

            val addSensorButton = Button("Add Sensor").apply {
                setOnAction {
                    TodoAlert(parentStage)
                }
            }
            val sensorListSidebar = VBox(sensorList, addSensorButton)
            sensorListSidebar.children.add(sensorDetail)

            val separator = Separator().apply {
                orientation = Orientation.VERTICAL
                padding = Insets(10.0)
            }
            children.addAll(sensorListSidebar, separator, sensorDetail)
        }
    }
}