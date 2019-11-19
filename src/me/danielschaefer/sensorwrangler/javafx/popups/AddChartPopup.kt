package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.TextField
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.gui.LineGraph
import me.danielschaefer.sensorwrangler.gui.ScatterGraph
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.javafx.ChartTab

class AddChartPopup(val parentStage: Stage, chartTab: ChartTab? = null): Stage() {
    private val yAxisMeasurements: MutableList<ComboBox<String>> = mutableListOf()
    private val yAxisSensors: MutableList<ComboBox<String>> = mutableListOf()
    private val formGrid: GridPane

    private fun addYAxis() {
        val newAxisIndex = yAxisSensors.size

        yAxisMeasurements.add(ComboBox<String>())
        yAxisSensors.add(ComboBox<String>().apply{
            items.setAll(App.instance.wrangler.sensors.map { it.title })
            valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                if (newValue == null)
                    return@ChangeListener

                val sensor = App.instance.wrangler.findSensorByTitle(newValue)
                if (sensor == null)
                    return@ChangeListener

                yAxisMeasurements[newAxisIndex].items.setAll(sensor.measurements.map { it.description })
            })
        })

        formGrid.add(Text("Y-Axis ${newAxisIndex + 1}"), 0, newAxisIndex + 10)
        formGrid.add(yAxisSensors[newAxisIndex], 2, newAxisIndex + 10)
        formGrid.add(yAxisMeasurements[newAxisIndex], 3, newAxisIndex + 10)
        sizeToScene()
    }

    init {
        initOwner(parentStage)

        val typeDropdown = ComboBox<String>().apply{
            items.addAll(App.instance.settings.supportedCharts.map { it.simpleName })
        }

        val chartNameField = TextField()
        val xAxisNameField = TextField()
        val yAxisNameField = TextField()
        val windowSizeField = TextField("25")
        val lowerBoundField = TextField("-10.0")
        val upperBoundField = TextField("10.0")
        val tickSpacingField = TextField("1")

        formGrid = GridPane().apply {
            padding = Insets(25.0)
            hgap = 10.0
            vgap = 10.0

            add(Text("Chart Type"), 0, 0)
            add(typeDropdown, 1, 0)

            add(Text("Chart Name"), 0, 1)
            add(chartNameField, 1, 1)

            add(Text("Window Size"), 0, 2)
            add(windowSizeField, 1, 2)

            add(Text("Lower Bound"), 0, 3)
            add(lowerBoundField, 1, 3)

            add(Text("Upper Bound"), 0, 4)
            add(upperBoundField, 1, 4)

            add(Text("Tick spacing"), 0, 5)
            add(tickSpacingField, 1, 5)

            // add(Empty)
            add(Text("Label"), 1, 6)
            add(Text("Measurement"), 2, 6)

            add(Text("X-Axis"), 0, 7)
            add(xAxisNameField, 1, 7)
            add(Text("Time"), 2, 7)

            add(Text("Y-Axis"), 0, 8)
            add(yAxisNameField, 1, 8)

            val addMeasurementButton = Button("Add y-axis measurement").apply {
                setOnAction {
                    addYAxis()
                }
            }
            add(addMeasurementButton, 2, 9)
        }

        val addButton = Button("Add chart").apply {
            onAction = EventHandler {
                val selectedMeasurements: MutableList<Measurement> = mutableListOf()
                for (i in 0 until yAxisSensors.size) {
                    val selectedSensor = App.instance.wrangler.findSensorByTitle(yAxisSensors[i].value)
                    selectedSensor?.measurements?.filter { it.description == yAxisMeasurements[i].value }?.let {
                        selectedMeasurements.add(it[0])
                    }
                }

                if (selectedMeasurements.isEmpty())
                    return@EventHandler

                val axisNames = arrayOf(xAxisNameField.text, yAxisNameField.text)
                val newChart = when (typeDropdown.value) {
                    "LineGraph" -> LineGraph(chartNameField.text, axisNames, selectedMeasurements).apply {
                        windowSize = windowSizeField.text.toInt()
                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                        tickSpacing = tickSpacingField.text.toDouble()
                    }
                    "ScatterChart" -> ScatterGraph(chartNameField.text, axisNames, selectedMeasurements).apply {
                        windowSize = windowSizeField.text.toInt()
                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                        tickSpacing = tickSpacingField.text.toDouble()
                    }
                    else -> TODO("This chart is not recognized.")
                }

                App.instance.wrangler.charts.add(newChart)
                chartTab?.chartList?.selectionModel?.select(chartTab.chartList.items.last())
                close()
            }
        }

        val contentBox = VBox(10.0, formGrid, addButton).apply {
            padding = Insets(25.0)
            alignment = Pos.CENTER;
        }
        scene = Scene(contentBox)
        title = "Add Chart"

        sizeToScene()
        show()
    }
}