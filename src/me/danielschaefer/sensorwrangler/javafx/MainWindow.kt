package me.danielschaefer.sensorwrangler.javafx

import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.chart.*
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Slider
import javafx.scene.layout.*
import javafx.scene.text.Text
import javafx.stage.Stage
import javafx.util.StringConverter
import me.danielschaefer.sensorwrangler.NamedThreadFactory
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.StringUtil
import me.danielschaefer.sensorwrangler.gui.*
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.javafx.popups.Alert
import me.danielschaefer.sensorwrangler.javafx.popups.StartRecordingPopup
import me.danielschaefer.sensorwrangler.sensors.ConnectionChangeListener
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

class MainWindow(private val primaryStage: Stage, private val wrangler: SensorWrangler) {
    private val graphUpdaters: MutableMap<Node?, MutableList<() -> Unit>> = mutableMapOf()
    private var paused = SimpleBooleanProperty(false).apply {}
    private var live = SimpleBooleanProperty(true)

    private lateinit var timeSlider: Slider
    private lateinit var buttonSkipToNow: Button
    private lateinit var buttonPause: Button
    private lateinit var timeMultiplier: ComboBox<Int>

    init {
        primaryStage.apply {
            import()

            title = "SensorWrangler"

            val vBox = VBox(createMenuBar(primaryStage), createAllChartsBox(), createPlayBox())

            scene = Scene(
                vBox,
                App.instance.settings.defaultWindowWidth.toDouble(),
                App.instance.settings.defaultWindowHeight.toDouble()
            )

            // TODO: Set an icon for the program - how to embed resources in the .jar?
            //icons.add(Image(javaClass.getResourceAsStream("ruler.png")))

            live.addListener { _, _, newLive ->
                // If we go slower than 1x, we'll fall behind current time
                if (timeMultiplier.value < 100)
                    live.value = false

                // Can't skip to now, if we're already live
                buttonSkipToNow.isDisable = newLive
            }

            paused.addListener { _, _, newPaused ->
                if (newPaused) {
                    live.value = false
                    buttonPause.text = "Start"
                } else {
                    buttonPause.text = "Pause"
                }
            }

            show()

            updateShownTimeWindow()
        }
    }

    private fun import() {
        if (!App.instance.wrangler.import(App.instance.settings.configPath)) {
            Alert(primaryStage, "Import failed",
                "Failed to import configuration because '${App.instance.settings.configPath}' was not found.")
            return
        }

        App.instance.wrangler.addSensorConnectionListener(JavaFXUtil.createConnectionChangeListener(primaryStage))
    }

    private fun createAllChartsBox(): GridPane {
        return GridPane().apply {
            // Let grid fill available space
            HBox.setHgrow(this, Priority.ALWAYS)
            VBox.setVgrow(this, Priority.ALWAYS)

            hgap = 25.0
            vgap = 25.0
            padding = Insets(25.0)

            val rows = App.instance.settings.chartGridRows;
            val cols = App.instance.settings.chartGridCols;

            //val fxChartIterator = fxCharts.iterator()
            rowLoop@ for (row in 0 until rows) {
                rowConstraints.add(RowConstraints().apply {
                    // Force row to resize, only then will the grid resize to its parent
                    vgrow = Priority.ALWAYS
                    // Keep all rows at the same height
                    percentHeight = 100.0 / rows
                })

                for (col in 0 until cols) {
                    // Add column only once
                    if (row == 0)
                        columnConstraints.add(ColumnConstraints().apply {
                            // Force row to resize, only then will the grid resize to its parent
                            hgrow = Priority.ALWAYS
                            // Keep all columns at the same width
                            percentWidth = 100.0 / cols
                        })

                    val chartBox = VBox(10.0).apply {
                        alignment = Pos.BOTTOM_CENTER
                    }
                    val stackPane = OpaqueStackPane(Text("No Chart"))
                    val chartDropdown = ComboBox<Chart>().apply {
                        items.addAll(App.instance.wrangler.charts)
                        App.instance.wrangler.charts.addListener(ListChangeListener {
                            it.next()
                            items.addAll(it.addedSubList)
                            items.removeAll(it.removed)
                        })
                        valueProperty().addListener(ChangeListener { _, oldChart, newChart ->
                            // No need to do anything if we don't switch to a chart
                            // TODO: Maybe remove the current chart. Except it's not possible to manually select null
                            if (newChart == null)
                                return@ChangeListener

                            oldChart?.let {
                                oldChart.hideOne()
                            }

                            newChart.showOne()
                            val fxChart = createFxChart(newChart)

                            if (stackPane.children.size > 1)
                                stackPane.children.remove(1, stackPane.children.size)
                            stackPane.children.add(fxChart)

                            // TODO: Delete this listener when chart is deselected, then we can also remove `&& chartPane.children.contains(fxChart)`
                            newChart.shown.addListener { _, _, newShown ->
                                // Replace deselected chart by transparent pane
                                if (!newShown && stackPane.children.contains(fxChart)) {
                                    graphUpdaters.remove(fxChart)
                                    stackPane.children.remove(fxChart)
                                    value = null
                                }
                            }
                            println("Switched from chart '$oldChart' to '$newChart'")
                        })
                    }

                    chartBox.children.setAll(stackPane, chartDropdown)

                    add(chartBox, col, row)
                }
            }
        }
    }

    private fun createPlayBox(): Node {
        val spacer = fun() = Region().apply {
            HBox.setHgrow(this, Priority.ALWAYS)
        }

        timeSlider = Slider().apply {
            var connectedSensors = 0
            min = Date().time.toDouble()
            max = min
            value = min

            App.instance.wrangler.addSensorConnectionListener(ConnectionChangeListener { _, connected, _ ->
                connectedSensors += if (connected) 1 else -1
                if (connectedSensors > 0)
                    min = Date().time.toDouble()
            })

            // No ticks
            isShowTickMarks = false
            isShowTickLabels = false
            minorTickCount = 0

            // Fill all of the horizontal space
            HBox.setHgrow(this, Priority.ALWAYS)

            // When the user changes the slider value (drag or click), we're not live anymore
            setOnMousePressed { live.value = false }
        }

        buttonSkipToNow = Button().apply {
            onAction = EventHandler {
                live.value = !paused.value
                timeSlider.value = timeSlider.max
            }
            disabledProperty().addListener { _, _, newIsDisable ->
                text = if (newIsDisable) "Live" else "Skip to now"
            }

            // Must come after the disable listener, so setting this value triggers it
            isDisable = live.value
        }

        buttonPause = Button("Pause").apply {
            onAction = EventHandler {
                paused.value = !paused.value
            }
        }

        val buttonProjected = Button("Start Recording").apply {
            onAction = EventHandler {
                if (App.instance.wrangler.isRecording.value) {
                    App.instance.wrangler.stopRecording()
                } else {
                    StartRecordingPopup(primaryStage)
                }
            }
            App.instance.wrangler.isRecording.addListener(ChangeListener<Boolean> { _, _, new ->
                text = if (new) "Stop Recording" else "Start Recording"
            })
        }

        val selectedTimeLabel = Text()
        val selectedTimeBox = HBox(Text("Timestamp displayed: "), selectedTimeLabel)
        val beginningLabel = Text(StringUtil.formatDate(timeSlider.min))
        val nowLabel = Text()

        timeSlider.minProperty().addListener { _, _, new ->
            beginningLabel.text = StringUtil.formatDate(new)
        }

        timeSlider.maxProperty().addListener { _, _, new ->
            nowLabel.text = StringUtil.formatDate(new)
        }

        timeSlider.valueProperty().addListener { _, _, new ->
            selectedTimeLabel.text = StringUtil.formatDate(new)
        }

        timeMultiplier = ComboBox<Int>().apply {
            items.addAll(-200, -150, -100, -50, 50, 100, 150, 200)
            value = 100

            valueProperty().addListener{ _, _, new ->
                App.instance.settings.chartUpdateMultiplier = new

                // If we go slower than 1x, we'll fall behind current time
                if (new < 100)
                    live.value = false
            }

            converter = object : StringConverter<Int>() {
                override fun toString(value: Int): String? {
                    return "${value / 100.0} x"
                }

                override fun fromString(string: String?): Int {
                    // TODO: Is this really necessary?
                    return 0
                }
            }
        }

        val buttonBox = HBox(10.0, selectedTimeBox, spacer(), timeMultiplier, buttonPause, buttonSkipToNow, buttonProjected)
        val sliderBox = HBox(10.0, beginningLabel, timeSlider, nowLabel)

        return VBox(10.0, buttonBox, sliderBox).apply {
            padding = Insets(25.0)
        }
    }

    private fun updateShownTimeWindow() {
        Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update slider")).apply {
            scheduleAtFixedRate({
                timeSlider.max = Date().time.toDouble()

                // Don't update the current value if paused
                if (paused.value)
                    return@scheduleAtFixedRate

                if (live.value)
                    timeSlider.value = Date().time.toDouble()
                else
                    timeSlider.value += App.instance.settings.chartUpdatePeriod * (App.instance.settings.chartUpdateMultiplier / 100.0)

                graphUpdaters.forEach{ (_, updaters) -> updaters.forEach { it() } }
            }, 0, App.instance.settings.chartUpdatePeriod.toLong(), TimeUnit.MILLISECONDS)  // 40ms = 25FPS
        }
    }

    private fun createFxChart(chart: Chart): Node? {
        return when (chart) {
            is AngleGraph -> {
                PieChart().apply {
                    val startAngleOffset = if (chart.horizontal) 0.0 else 90.0
                    animated = false
                    startAngle = startAngleOffset
                    labelsVisible = false

                    val leftData = PieChart.Data("Foreground", 50.0)
                    val rightData = PieChart.Data("Background", 50.0)

                    // Show data from now until chart.windowSize ago
                    addGraphUpdater(this) {
                        // Get latest value that is before the current slider selection
                        // The assumption is that the list of data points is sorted by timestamp
                        // TODO: Measurements should have a second list of the sorted list
                        val sortedDataPoints = chart.axis.dataPoints
                        val latestDataPoint = sortedDataPoints.lastOrNull { it.timestamp < timeSlider.value }

                        startAngle = (latestDataPoint?.value ?: 0.0) + startAngleOffset
                    }

                    data = FXCollections.observableArrayList(listOf(leftData, rightData))
                }
            }
            is CurrentValueGraph -> {
                GridPane().apply {
                    vgap = 20.0
                    hgap = 20.0

                    chart.axes.forEachIndexed { row, axis ->
                        val text = Text(axis.description)
                        val value = Text()

                        // Show current value
                        addGraphUpdater(this) {
                            // We assume they're already sorted by timestamp, this is not necessarily the case
                            val sortedDataPoints = axis.dataPoints
                            val latestDataPoint = sortedDataPoints.lastOrNull { it.timestamp < timeSlider.value }

                            value.text = "${(latestDataPoint?.value ?: 0.0)}${axis.unit.unitAppendix}"
                        }
                        addRow(row, text, value)
                    }
                }
            }
            is BarDistributionGraph -> {
                val yAxis = CategoryAxis().apply {
                    animated = false
                }
                val xAxis = NumberAxis().apply {
                    animated = false
                    isAutoRanging = false
                    lowerBound = 0.0
                    upperBound = 100.0
                }
                StackedBarChart(xAxis, yAxis).apply {
                    title = chart.title
                    animated = false

                    val leftData = XYChart.Data(50.0 as Number, "Distribution")
                    val leftSeries = XYChart.Series<Number, String>().apply {
                        data.addAll(leftData)
                    }

                    val rightData = XYChart.Data(50.0 as Number, "Distribution")
                    val rightSeries = XYChart.Series<Number, String>().apply {
                        data.addAll(rightData)
                    }

                    data.addAll(leftSeries, rightSeries)

                    // Show data from now until chart.windowSize ago
                    addGraphUpdater(this) {
                        // Get latest value that is before the current slider selection
                        // The assumption is that the list of data points is sorted by timestamp
                        // TODO: Measurements should have a second list of the sorted list
                        val sortedDataPoints = chart.axis.dataPoints
                        val latestDataPoint = sortedDataPoints.lastOrNull { it.timestamp < timeSlider.value }

                        rightData.xValue = latestDataPoint?.value ?: 50
                        leftData.xValue = 100.0 - rightData.xValue.toDouble()
                    }
                }
            }
            is BarGraph -> {
                val xAxis = CategoryAxis().apply {
                    //label = chart.axisNames[0]
                    animated = false
                    // Long labels are automatically rotated to 90°.
                    // Setting it to 0° doesn't change that behaviour *shrug*
                    tickLabelRotation = 360.0
                }
                val fxYAxis = NumberAxis().apply {
                    label = chart.yAxisLabel
                    animated = false
                    isAutoRanging = false
                    lowerBound = chart.lowerBound
                    upperBound = chart.upperBound
                }
                BarChart(xAxis, fxYAxis).apply {
                    title = chart.title  // TODO: Actually we don't need this at all, since the combo box already shows it
                    animated = false
                    val series = XYChart.Series<String, Number>().apply {
                        name = chart.title
                        val emptyList = mutableListOf<XYChart.Data<String, Number>>()
                        data = FXCollections.observableList(emptyList)
                    }

                    for (yAxis in chart.yAxes) {
                        // Start at 0, we need a starting value to later change the yValue of that
                        val data = XYChart.Data(yAxis.description, 0.0 as Number)
                        series.data.add(data)

                        // Show data from now until chart.windowSize ago
                        addGraphUpdater(this) {
                            // Get latest value that is before the current slider selection
                            // The assumption is that the list of data points is sorted by timestamp
                            // TODO: Measurements should have a second list of the sorted list
                            val sortedDataPoints = yAxis.dataPoints
                            val latestDataPoint = sortedDataPoints.lastOrNull { it.timestamp < timeSlider.value }

                            data.yValue = latestDataPoint?.value ?: 0.0
                        }
                    }

                    data.add(series)
                    this.isLegendVisible = false  // It's useless for bar charts
                }
            }
            is AxisGraph -> {
                val xAxis = NumberAxis().apply {
                    isAutoRanging = false
                    tickUnit = 5_000.0  // Tick mark every 5 seconds
                    animated = false

                    tickLabelFormatter = object : StringConverter<Number>() {
                        override fun toString(unixTime: Number): String? {
                            return StringUtil.formatDate(unixTime)
                        }

                        override fun fromString(string: String?): Number {
                            // TODO: DateTimeParser to Number
                            return 0
                        }
                    }
                }

                val fxYAxis = NumberAxis().apply {
                    label = chart.yAxisLabel
                    isAutoRanging = false
                    lowerBound = chart.lowerBound
                    upperBound = chart.upperBound
                    tickUnit = chart.tickSpacing
                    animated = false
                }
                val fxChart = when (chart) {
                    is LineGraph -> LineChart(xAxis, fxYAxis).apply { createSymbols = chart.withDots }
                    is ScatterGraph -> ScatterChart(xAxis, fxYAxis)
                    else -> {
                        println("Cannot display this kind of chart")
                        null
                    }
                }

                fxChart?.apply {
                    title = chart.title
                    animated = false
                    for (yAxis in chart.yAxes) {
                        val series = XYChart.Series<Number, Number>().apply {
                            name = yAxis.description ?: "Data"
                        }


                        series.data = FXCollections.observableList(mutableListOf<XYChart.Data<Number, Number>>())
                        // Fill with past data
                        series.data.addAll(yAxis.dataPoints.map { dp -> XYChart.Data(dp.timestamp as Number, dp.value as Number) })

                        // TODO: Maybe we can define some sort of mapping to get rid of the additional listener,
                        //       like the cellFactory, but for charts
                        yAxis.dataPoints.addListener(ListChangeListener {
                            it.next()
                            series.data.addAll(it.addedSubList.map { dp -> XYChart.Data(dp.timestamp as Number, dp.value as Number) })
                        })

                        fxChart.data.add(series)

                        // Show data from now until chart.windowSize ago
                        addGraphUpdater(this) {
                            xAxis.upperBound = timeSlider.value
                            xAxis.lowerBound = xAxis.upperBound - chart.windowSize
                        }
                    }
                }
            }
            is PieDistributionGraph -> {
                PieChart().apply {
                    animated = false
                    startAngle = -90.0

                    // Start at 0, we need a starting value to later change the yValue of that
                    val leftData = PieChart.Data("Left", 50.0)
                    val rightData = PieChart.Data("Right", 50.0)

                    // Show data from now until chart.windowSize ago
                    addGraphUpdater(this) {
                        // Get latest value that is before the current slider selection
                        // The assumption is that the list of data points is sorted by timestamp
                        // TODO: Measurements should have a second list of the sorted list
                        val sortedDataPoints = chart.axis.dataPoints
                        val latestDataPoint = sortedDataPoints.lastOrNull { it.timestamp < timeSlider.value }

                        rightData.pieValue = latestDataPoint?.value ?: 50.0
                        leftData.pieValue = 100.0 - rightData.pieValue
                    }

                    data = FXCollections.observableArrayList(listOf(leftData, rightData))
                }
            }
            else -> {
                println("Cannot display this kind of chart")
                null
            }
        }
    }

    private fun addGraphUpdater(node: Node, updater: () -> Unit) {
        if (!graphUpdaters.containsKey(node))
            graphUpdaters[node] = mutableListOf()

        graphUpdaters[node]?.add(updater)
    }
}
