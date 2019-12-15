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
import javafx.util.StringConverter
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.gui.*
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.javafx.ChartTab
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf

class AddChartPopup(val parentStage: Stage, chartTab: ChartTab? = null): Stage() {
    private val yAxisMeasurements: MutableList<ComboBox<String>> = mutableListOf()
    private val yAxisSensors: MutableList<ComboBox<String>> = mutableListOf()
    private val yAxisUnits: MutableList<Text> = mutableListOf()
    private val formGrid: GridPane

    private fun addYAxis() {
        val newAxisIndex = yAxisSensors.size

        yAxisSensors.add(ComboBox<String>().apply {
            items.setAll(App.instance.wrangler.sensors.map { it.title })
            valueProperty().addListener(ChangeListener { _, _, newValue ->
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

    private val chartNameField = TextField()
    private val yAxisNameField = TextField()
    private val lowerBoundField = TextField("-25.0")
    private val upperBoundField = TextField("25.0")
    private val tickSpacingField = TextField("5")
    private val windowSizeField = TextField("10")
    private val withDotsField = CheckBox()
    private val typeDropdown = ComboBox<KClass<out Chart>>()

    init {
        initOwner(parentStage)

        val yAxisNameLabel = Label("Y-Axis label")
        val lowerBoundLabel = Label("Lower Bound")
        val upperBoundLabel = Label("Upper Bound")

        val axisSensorLabel = Label("Sensor")
        val axisMeasurementLabel = Label("Measurement")
        val axisUnitLabel = Label("Unit")

        val tickSpacingLabel = Label("Tick spacing")
        val windowSizeLabel = Label("Window Size [s]")
        val withDotsLabel = Label("With dots")

        val addMeasurementButton = Button("Add y-axis measurement").apply {
            setOnAction {
                addYAxis()
            }
        }

        typeDropdown.apply {
            items.addAll(App.instance.settings.supportedCharts)

            converter = object : StringConverter<KClass<out Chart>>() {
                override fun toString(value: KClass<out Chart>?): String? {
                    return value?.simpleName
                }

                override fun fromString(string: String?): KClass<out Chart>? {
                    // TODO: Is this really necessary?
                    return null
                }
            }

            valueProperty().addListener(ChangeListener { _, _, newChart ->
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
                addMeasurementButton.isDisable = true

                when {
                    newChart == CurrentValueGraph::class -> {
                        addMeasurementButton.isDisable = false
                    }
                    newChart == BarGraph::class -> {
                        lowerBoundLabel.isVisible = true
                        upperBoundField.isVisible = true

                        upperBoundLabel.isVisible = true
                        lowerBoundField.isVisible = true

                        yAxisNameLabel.isVisible = true
                        yAxisNameField.isVisible = true

                        addMeasurementButton.isDisable = false
                    }
                    newChart.isSubclassOf(AxisGraph::class) -> {
                        if (newChart == LineGraph::class) {
                            withDotsLabel.isVisible = true
                            withDotsField.isVisible = true
                        }

                        tickSpacingField.isVisible = true
                        tickSpacingLabel.isVisible = true

                        windowSizeField.isVisible = true
                        windowSizeLabel.isVisible = true

                        lowerBoundLabel.isVisible = true
                        lowerBoundField.isVisible = true

                        upperBoundField.isVisible = true
                        upperBoundLabel.isVisible = true

                        yAxisNameField.isVisible = true
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
                    val selectedSensor = yAxisSensors[i].value?.let {
                        App.instance.wrangler.findVirtualSensorByTitle(it)
                    }
                    selectedSensor?.measurements?.filter { it.description == yAxisMeasurements[i].value }?.let {
                        selectedMeasurements.add(it[0])
                    }
                }

                if (selectedMeasurements.isEmpty()) {
                    Alert(parentStage, "Form invalid", "No measurements selected")
                    return@EventHandler
                }

                checkFormInputValidity()?.let {
                    Alert(parentStage, "Form invalid", it)
                    return@EventHandler
                }

                val newChart = when (typeDropdown.value) {
                    LineGraph::class -> LineGraph().apply {
                        title = chartNameField.text
                        this.yAxisLabel = yAxisNameField.text
                        yAxes = selectedMeasurements

                        windowSize = windowSizeField.text.toInt() * 1_000
                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                        tickSpacing = tickSpacingField.text.toDouble()
                        withDots = withDotsField.isSelected
                    }
                    ScatterGraph::class -> ScatterGraph().apply {
                        title = chartNameField.text
                        this.yAxisLabel = yAxisNameField.text
                        yAxes = selectedMeasurements

                        windowSize = windowSizeField.text.toInt() * 1_000
                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                        tickSpacing = tickSpacingField.text.toDouble()
                    }
                    BarGraph::class -> BarGraph().apply {
                        title = chartNameField.text
                        this.yAxisLabel = yAxisNameField.text
                        yAxes = selectedMeasurements

                        lowerBound = lowerBoundField.text.toDouble()
                        upperBound = upperBoundField.text.toDouble()
                    }
                    CurrentValueGraph::class -> CurrentValueGraph().apply {
                        title = chartNameField.text
                        axes = selectedMeasurements
                    }
                    DistributionGraph::class -> DistributionGraph().apply {
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

    private fun checkFormInputValidity(): String? {
        if (typeDropdown.value == null)
            return "Must select a chart type"

        if (chartNameField.text.isEmpty())
            return "Chart must have a title"

        if (yAxisNameField.isVisible && yAxisNameField.text.isEmpty())
            return "Y-Axis must have a name"

        if (lowerBoundField.isVisible && lowerBoundField.text.toDoubleOrNull() == null)
            return "Lower bound must be a decimal number (e.g. 25.0)"

        if (upperBoundField.isVisible && upperBoundField.text.toDoubleOrNull() == null)
            return "Upper bound must be a decimal number (e.g. 25.0)"

        if (windowSizeField.isVisible && windowSizeField.text.toIntOrNull() == null)
            return "Window size must be an integer"

        if (tickSpacingField.isVisible && tickSpacingField.text.toDoubleOrNull() == null)
            return "Tick spacing must be a decimal number (e.g. 5.0)"

        return null
    }
}
