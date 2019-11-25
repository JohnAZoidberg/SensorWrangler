package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.gui.BarGraph
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

        yAxisSensors.add(ComboBox<String>().apply {
            items.setAll(App.instance.wrangler.sensors.map { it.title })
            valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                if (newValue == null)
                    return@ChangeListener

                val sensor = App.instance.wrangler.findVirtualSensorByTitle(newValue)
                if (sensor == null)
                    return@ChangeListener

                yAxisMeasurements[newAxisIndex].items.setAll(sensor.measurements.map { it.description })
                sizeToScene()
            })
        })
        yAxisMeasurements.add(ComboBox<String>())

        formGrid.add(Label("Y-Axis ${newAxisIndex + 1}"), 0, newAxisIndex + 10)
        formGrid.add(yAxisSensors[newAxisIndex], 2, newAxisIndex + 10)
        formGrid.add(yAxisMeasurements[newAxisIndex], 3, newAxisIndex + 10)
        sizeToScene()
    }

    init {
        initOwner(parentStage)

        val chartNameField = TextField()
        val xAxisNameField = TextField()
        val yAxisNameField = TextField()
        val lowerBoundField = TextField("-25.0")
        val upperBoundField = TextField("25.0")

        val tickSpacingLabel = Label("Tick spacing")
        val tickSpacingField = TextField("5")
        val windowSizeLabel = Label("Window Size [s]")
        val windowSizeField = TextField("10")
        val withDotsLabel = Label("With dots")
        val withDotsField = CheckBox()

        val typeDropdown = ComboBox<String>().apply{
            items.addAll(App.instance.settings.supportedCharts.map { it.simpleName })
            valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                when (newValue) {
                    BarGraph::class.simpleName -> {
                        withDotsLabel.isVisible = false
                        withDotsField.isVisible = false
                        tickSpacingField.isVisible = false
                        tickSpacingLabel.isVisible = false
                        windowSizeField.isVisible = false
                        windowSizeLabel.isVisible = false
                    }
                    LineGraph::class.simpleName -> {
                        withDotsLabel.isVisible = true
                        withDotsField.isVisible = true
                        tickSpacingField.isVisible = true
                        tickSpacingLabel.isVisible = true
                        windowSizeField.isVisible = true
                        windowSizeLabel.isVisible = true
                    }
                    ScatterGraph::class.simpleName -> {
                        withDotsLabel.isVisible = false
                        withDotsField.isVisible = false
                        tickSpacingField.isVisible = true
                        tickSpacingLabel.isVisible = true
                        windowSizeField.isVisible = true
                        windowSizeLabel.isVisible = true
                    }
                }
            })
        }

        formGrid = GridPane().apply {
            padding = Insets(25.0)
            hgap = 10.0
            vgap = 10.0

            var row = 0

            add(Label("Chart Type"), 0, row)
            add(typeDropdown, 1, row++)

            add(Label("Chart Name"), 0, row)
            add(chartNameField, 1, row++)

            add(windowSizeLabel, 0, row)
            add(windowSizeField, 1, row++)

            add(Label("Lower Bound"), 0, row)
            add(lowerBoundField, 1, row++)

            add(Label("Upper Bound"), 0, row)
            add(upperBoundField, 1, row++)

            add(tickSpacingLabel, 0, row)
            add(tickSpacingField, 1, row++)

            add(withDotsLabel, 0, row)
            add(withDotsField, 1, row++)

            // add(Empty)
            add(Label("Label"), 1, row)
            add(Label("Measurement"), 2, row++)

            add(Label("X-Axis"), 0, row)
            add(xAxisNameField, 1, row)
            add(Label("Time"), 2, row++)

            add(Label("Y-Axis"), 0, row)
            add(yAxisNameField, 1, row++)

            val addMeasurementButton = Button("Add y-axis measurement").apply {
                setOnAction {
                    addYAxis()
                }
            }
            add(addMeasurementButton, 2, row++)
        }

        // Already display one measurement input field
        addYAxis()

        val addButton = Button("Add chart").apply {
            onAction = EventHandler {
                val selectedMeasurements: MutableList<Measurement> = mutableListOf()
                for (i in 0 until yAxisSensors.size) {
                    val selectedSensor = App.instance.wrangler.findVirtualSensorByTitle(yAxisSensors[i].value)
                    selectedSensor?.measurements?.filter { it.description == yAxisMeasurements[i].value }?.let {
                        selectedMeasurements.add(it[0])
                    }
                }

                if (selectedMeasurements.isEmpty())
                    return@EventHandler

                val axisNames = arrayOf(xAxisNameField.text, yAxisNameField.text)
                val newChart = when (typeDropdown.value) {
                    LineGraph::class.simpleName -> LineGraph().apply {
                        title = chartNameField.text
                        this.axisNames = axisNames
                        yAxes = selectedMeasurements

                        windowSize = windowSizeField.text.toInt() * 1_000
                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                        tickSpacing = tickSpacingField.text.toDouble()
                        withDots = withDotsField.isSelected
                    }
                    ScatterGraph::class.simpleName -> ScatterGraph().apply {
                        title = chartNameField.text
                        this.axisNames = axisNames
                        yAxes = selectedMeasurements

                        windowSize = windowSizeField.text.toInt() * 1_000
                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                        tickSpacing = tickSpacingField.text.toDouble()
                    }
                    BarGraph::class.simpleName -> BarGraph().apply {
                        title = chartNameField.text
                        this.axisNames = axisNames
                        yAxes = selectedMeasurements

                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
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