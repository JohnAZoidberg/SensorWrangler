package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.gui.*
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.javafx.ChartTab

class AddChartPopup(val parentStage: Stage, chartTab: ChartTab? = null): Stage() {
    private val yAxisMeasurements: MutableList<ComboBox<String>> = mutableListOf()
    private val yAxisSensors: MutableList<ComboBox<String>> = mutableListOf()
    private val yAxisUnits: MutableList<Text> = mutableListOf()
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
                yAxisUnits[newAxisIndex].text = ""
                sizeToScene()
            })
        })
        yAxisMeasurements.add(ComboBox<String>().apply {
            valueProperty().addListener(ChangeListener { _,  _, newValue ->
                if (newValue == null)
                    return@ChangeListener

                val sensor = App.instance.wrangler.findVirtualSensorByTitle(yAxisSensors[newAxisIndex].value)
                if (sensor == null)
                    return@ChangeListener

                yAxisUnits[newAxisIndex].text = sensor.measurements.first { it.description == newValue }.unit.toString()
                sizeToScene()
            })
        })
        yAxisUnits.add(Text())

        val newRowIndex = formGrid.rowCount
        formGrid.add(Label("Measurement ${newAxisIndex + 1}"), 0, newRowIndex)
        formGrid.add(yAxisSensors[newAxisIndex], 1, newRowIndex)
        formGrid.add(yAxisMeasurements[newAxisIndex], 2, newRowIndex)
        formGrid.add(yAxisUnits[newAxisIndex], 3, newRowIndex)
        sizeToScene()
    }

    init {
        initOwner(parentStage)

        val chartNameField = TextField()

        val yAxisNameField = TextField()
        val yAxisNameLabel = Label("Y-Axis label")
        val lowerBoundField = TextField("-25.0")
        val lowerBoundLabel = Label("Lower Bound")
        val upperBoundField = TextField("25.0")
        val upperBoundLabel = Label("Upper Bound")

        val axisSensorLabel = Label("Sensor")
        val axisMeasurementLabel = Label("Measurement")
        val axisUnitLabel = Label("Unit")

        val tickSpacingLabel = Label("Tick spacing")
        val tickSpacingField = TextField("5")
        val windowSizeLabel = Label("Window Size [s]")
        val windowSizeField = TextField("10")
        val withDotsLabel = Label("With dots")
        val withDotsField = CheckBox()

        val addMeasurementButton = Button("Add y-axis measurement").apply {
            setOnAction {
                addYAxis()
            }
        }

        val typeDropdown = ComboBox<String>().apply{
            items.addAll(App.instance.settings.supportedCharts.map { it.simpleName })
            valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                when (newValue) {
                    DistributionGraph::class.simpleName -> {
                        withDotsLabel.isVisible = false
                        withDotsField.isVisible = false
                        tickSpacingField.isVisible = false
                        tickSpacingLabel.isVisible = false
                        windowSizeField.isVisible = false
                        windowSizeLabel.isVisible = false
                        lowerBoundLabel.isVisible = false
                        upperBoundField.isVisible = false
                        upperBoundLabel.isVisible = false
                        lowerBoundField.isVisible = false

                        yAxisNameField.isVisible = false
                        yAxisNameLabel.isVisible = false
                        axisUnitLabel.isVisible = true
                        addMeasurementButton.isDisable = true
                    }
                    CurrentValueGraph::class.simpleName -> {
                        withDotsLabel.isVisible = false
                        withDotsField.isVisible = false
                        tickSpacingField.isVisible = false
                        tickSpacingLabel.isVisible = false
                        windowSizeField.isVisible = false
                        windowSizeLabel.isVisible = false
                        lowerBoundLabel.isVisible = false
                        upperBoundField.isVisible = false
                        upperBoundLabel.isVisible = false
                        lowerBoundField.isVisible = false

                        yAxisNameField.isVisible = false
                        yAxisNameLabel.isVisible = false
                        addMeasurementButton.isDisable = false
                    }
                    BarGraph::class.simpleName -> {
                        withDotsLabel.isVisible = false
                        withDotsField.isVisible = false
                        tickSpacingField.isVisible = false
                        tickSpacingLabel.isVisible = false
                        windowSizeField.isVisible = false
                        windowSizeLabel.isVisible = false
                        lowerBoundLabel.isVisible = true
                        upperBoundField.isVisible = true
                        upperBoundLabel.isVisible = true
                        lowerBoundField.isVisible = true

                        yAxisNameField.isVisible = false
                        yAxisNameLabel.isVisible = true
                        addMeasurementButton.isDisable = false
                    }
                    LineGraph::class.simpleName -> {
                        withDotsLabel.isVisible = true
                        withDotsField.isVisible = true
                        tickSpacingField.isVisible = true
                        tickSpacingLabel.isVisible = true
                        windowSizeField.isVisible = true
                        windowSizeLabel.isVisible = true
                        lowerBoundLabel.isVisible = true
                        upperBoundField.isVisible = true
                        upperBoundLabel.isVisible = true
                        lowerBoundField.isVisible = true

                        yAxisNameField.isVisible = false
                        yAxisNameLabel.isVisible = true
                        addMeasurementButton.isDisable = false
                    }
                    ScatterGraph::class.simpleName -> {
                        withDotsLabel.isVisible = false
                        withDotsField.isVisible = false
                        tickSpacingField.isVisible = true
                        tickSpacingLabel.isVisible = true
                        windowSizeField.isVisible = true
                        windowSizeLabel.isVisible = true
                        lowerBoundLabel.isVisible = true
                        upperBoundField.isVisible = true
                        upperBoundLabel.isVisible = true
                        lowerBoundField.isVisible = true

                        yAxisNameField.isVisible = false
                        yAxisNameLabel.isVisible = true
                        addMeasurementButton.isDisable = false
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

            add(lowerBoundLabel, 0, row)
            add(lowerBoundField, 1, row++)

            add(upperBoundLabel, 0, row)
            add(upperBoundField, 1, row++)

            add(tickSpacingLabel, 0, row)
            add(tickSpacingField, 1, row++)

            add(withDotsLabel, 0, row)
            add(withDotsField, 1, row++)

            add(yAxisNameLabel, 0, row)
            add(yAxisNameField, 1, row++)

            add(addMeasurementButton, 2, row++)

            add(axisSensorLabel, 1, row)
            add(axisMeasurementLabel, 2, row)
            add(axisUnitLabel, 3, row++)
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

                val newChart = when (typeDropdown.value) {
                    LineGraph::class.simpleName -> LineGraph().apply {
                        title = chartNameField.text
                        this.yAxisLabel = yAxisNameField.text
                        yAxes = selectedMeasurements

                        windowSize = windowSizeField.text.toInt() * 1_000
                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                        tickSpacing = tickSpacingField.text.toDouble()
                        withDots = withDotsField.isSelected
                    }
                    ScatterGraph::class.simpleName -> ScatterGraph().apply {
                        title = chartNameField.text
                        this.yAxisLabel = yAxisNameField.text
                        yAxes = selectedMeasurements

                        windowSize = windowSizeField.text.toInt() * 1_000
                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                        tickSpacing = tickSpacingField.text.toDouble()
                    }
                    BarGraph::class.simpleName -> BarGraph().apply {
                        title = chartNameField.text
                        this.yAxisLabel = yAxisNameField.text
                        yAxes = selectedMeasurements

                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                    }
                    CurrentValueGraph::class.simpleName -> CurrentValueGraph().apply {
                        title = chartNameField.text
                        axes = selectedMeasurements
                    }
                    DistributionGraph::class.simpleName -> DistributionGraph().apply {
                        title = chartNameField.text
                        axis = selectedMeasurements.first()
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
