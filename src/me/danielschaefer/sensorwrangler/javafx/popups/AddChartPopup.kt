package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.text.Text
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.gui.LineGraph
import me.danielschaefer.sensorwrangler.gui.ScatterGraph
import me.danielschaefer.sensorwrangler.javafx.App

class AddChartPopup(val parentStage: Stage): Stage() {
    init {
        initOwner(parentStage)

        val formGrid = GridPane().apply {
            padding = Insets(25.0)
            hgap = 10.0
            vgap = 10.0

            val typeDropdown = ComboBox<String>().apply{
                items.addAll(App.instance!!.settings.supportedCharts.map { it.simpleName })
            }

            val chartNameField = TextField()
            val xAxisNameField = TextField()
            val yAxisNameField = TextField()
            val windowSizeField = TextField("25")
            val lowerBoundField = TextField("-10.0")
            val upperBoundField = TextField("10.0")
            val tickSpacingField = TextField("1")


            val yAxisMeasurement = ComboBox<String>()
            val yAxisSensor = ComboBox<String>().apply{
                items.setAll(App.instance!!.wrangler.sensors.map { it.title })
                valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                    if (newValue == null)
                        return@ChangeListener

                    val sensor = App.instance!!.wrangler.findSensorByTitle(newValue)
                    if (sensor == null)
                        return@ChangeListener

                    yAxisMeasurement.items.setAll(sensor.measurements.map { it.description })
                })
            }


            add(Text("Chart Type"), 0, 0)
            add(typeDropdown, 1, 0)

            add(Text("Chart Name"), 0, 1)
            add(chartNameField, 1, 1)

            // add(Empty)
            add(Text("Label"), 1, 2)
            add(Text("Measurement"), 2, 2)

            add(Text("X-Axis"), 0, 3)
            add(xAxisNameField, 1, 3)
            add(Text("Time"), 2, 3)

            add(Text("Y-Axis"), 0, 4)
            add(yAxisNameField, 1, 4)
            add(yAxisSensor, 2, 4)
            add(yAxisMeasurement, 3, 4)

            add(Text("Window Size"), 0, 5)
            add(windowSizeField, 1, 5)

            add(Text("Lower Bound"), 0, 6)
            add(lowerBoundField, 1, 6)

            add(Text("Upper Bound"), 0, 7)
            add(upperBoundField, 1, 7)

            add(Text("Tick spacing"), 0, 8)
            add(tickSpacingField, 1, 8)

            // TODO: Chart type specifics
            // Which Measurement on which axis
            // Axis labels

            val addButton = Button("Add").apply {
                onAction = EventHandler {
                    val selectedSensor = App.instance!!.wrangler.findSensorByTitle(yAxisSensor.value)
                    val selectedMeasurement = selectedSensor?.measurements?.filter { it.description == yAxisMeasurement.value }
                    if (selectedMeasurement == null)
                        return@EventHandler

                    val axisNames = arrayOf(xAxisNameField.text, yAxisNameField.text)
                    when (typeDropdown.value) {
                        "LineGraph" -> LineGraph(chartNameField.text, axisNames, selectedMeasurement[0]).apply {
                            windowSize = windowSizeField.text.toInt()
                            lowerBound = lowerBoundField.text.toDouble()
                            upperBound = upperBoundField.text.toDouble()
                            tickSpacing = tickSpacingField.text.toDouble()
                            App.instance!!.wrangler.charts.add(this)
                        }
                        "ScatterChart" -> ScatterGraph(chartNameField.text, axisNames, selectedMeasurement[0]).apply {
                            windowSize = windowSizeField.text.toInt()
                            lowerBound = lowerBoundField.text.toDouble()
                            upperBound = upperBoundField.text.toDouble()
                            tickSpacing = tickSpacingField.text.toDouble()
                            App.instance!!.wrangler.charts.add(this)
                        }
                    }
                    close()
                }
            }
            add(addButton, 0, 9)
        }

        scene = Scene(formGrid)
        title = "Add Chart"

        sizeToScene()
        show()
    }
}