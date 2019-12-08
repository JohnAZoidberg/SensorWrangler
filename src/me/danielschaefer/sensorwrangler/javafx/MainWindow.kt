package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
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
import me.danielschaefer.sensorwrangler.gui.*
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.javafx.popups.Alert
import me.danielschaefer.sensorwrangler.javafx.popups.StartRecordingPopup
import me.danielschaefer.sensorwrangler.sensors.Sensor
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainWindow(private val primaryStage: Stage, private val wrangler: SensorWrangler) {
    private var paused: Boolean = false
    private var live: Boolean = true
    private var lastUpperBound: Double? = null

    fun import() {
        if (!App.instance.wrangler.import(App.instance.settings.configPath)) {
            Alert(primaryStage, "Import failed",
                "Failed to import configuration because '${App.instance.settings.configPath}' was not found.")
            return
        }

        App.instance.wrangler.sensors.filterIsInstance<Sensor>().forEach {
            it.addConnectionChangeListener(JavaFXUtil.createConnectionChangeListener(primaryStage))
        }
    }

    init {
        primaryStage.apply {
            import()

            title = "SensorWrangler"

            val allChartsBox = GridPane().apply {
                padding = Insets(25.0)

                val rows = App.instance.settings.chartGridRows;
                val cols = App.instance.settings.chartGridCols;

                //val fxChartIterator = fxCharts.iterator()
                rowLoop@ for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        // Dummy chart as a placeholder
                        val chartBox = VBox(10.0)
                        val fxChart = LineChart<String, Number>(CategoryAxis(), NumberAxis())
                        val chartDropdown = ComboBox<String>().apply {
                            App.instance.wrangler.charts.addListener(ListChangeListener {
                                items.setAll(it.list.map { it.title })
                            })
                            items.addAll(App.instance.wrangler.charts.map { it.title })
                            valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                                // No need to do anything if we don't switch to a chart
                                // TODO: Maybe remove the current chart. Except it's not possible to manually select null
                                if (newValue == null)
                                    return@ChangeListener

                                oldValue?.let {
                                    App.instance.wrangler.findChartByTitle(oldValue)?.let {
                                        it.shown = false
                                    }
                                }

                                App.instance.wrangler.findChartByTitle(newValue)?.let {
                                    it.shown = true
                                    chartBox.children[0] = createFxChart(it)
                                }
                                println("Switched from chart $oldValue to $newValue")
                            })
                        }
                        chartBox.children.setAll(fxChart, chartDropdown)
                        add(chartBox, col, row)
                    }
                }
            }

            // TODO: Make this useful
            val playBox = HBox(10.0).apply {
                padding = Insets(25.0)

                val spacer = fun() = Region().apply {
                    HBox.setHgrow(this, Priority.ALWAYS)
                }

                val slider = Slider().apply {
                    min = -60.0;
                    max = 0.0;
                    value = 0.0;
                    isShowTickLabels = true;
                    isShowTickMarks = true;
                    majorTickUnit = 10.0;
                    minorTickCount = 1;
                    blockIncrement = 1.0;
                    HBox.setHgrow(this, Priority.ALWAYS)
                    isDisable = true
                    labelFormatter = object : StringConverter<Double>() {
                        override fun toString(value: Double): String? {
                            return if (value == 0.0) "now" else "$value s"
                        }

                        override fun fromString(string: String?): Double {
                            return if (string == "now")
                                0.0
                            else
                                string!!.removeSuffix(" s").toDouble()
                        }
                    }
                }

                val buttonSkipToNow = Button("Skip to now").apply {
                    isDisable = true
                    onAction = EventHandler {
                        lastUpperBound = Date().time.toDouble() - App.instance.settings.chartUpdatePeriod
                        live = true
                        isDisable = true
                    }
                }

                val buttonPause = Button("Pause").apply {
                    onAction = EventHandler {
                        paused = !paused
                        live = !paused
                        buttonSkipToNow.isDisable = !live
                        text = if (paused) "Start" else "Pause"
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
                    App.instance.wrangler.isRecording.addListener(ChangeListener<Boolean> { observable, old, new ->
                        text = if (new) "Stop Recording" else "Start Recording"
                    })
                }

                children.addAll(slider, buttonPause, buttonSkipToNow, buttonProjected)
            }
            val vBox = VBox(createMenuBar(primaryStage), allChartsBox, playBox)

            scene = Scene(vBox, 800.0, 600.0)
            // TODO: Set an icon for the program - how to embed resources in the .jar?
            //icons.add(Image(javaClass.getResourceAsStream("ruler.png")))
            sizeToScene()
            show()

            Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update lastUpperBound")).apply {
                scheduleAtFixedRate({
                    if (paused)
                        return@scheduleAtFixedRate

                    if (lastUpperBound == null)
                        lastUpperBound = Date().time.toDouble() - App.instance.settings.chartUpdatePeriod

                    lastUpperBound?.let {
                        // TODO: This might slowly fall behind the current time,
                        //       if this thread isn't properly scheduled every 40ms
                        lastUpperBound = it.plus(App.instance.settings.chartUpdatePeriod)
                    }
                }, 0, App.instance.settings.chartUpdatePeriod.toLong(), TimeUnit.MILLISECONDS)  // 40ms = 25FPS
            }
        }
    }

    private fun createFxChart(chart: Chart): Node? {
        return when (chart) {
            is CurrentValueGraph -> {
                GridPane().apply {
                    vgap = 20.0
                    hgap = 20.0

                    chart.axes.forEachIndexed { row, axis ->
                        val text = Text(axis.description)
                        val value = Text()
                        axis.dataPoints.addListener(ListChangeListener {
                            it.next()
                            value.text = it.addedSubList.last().value.toString()
                        })
                        addRow(row, text, value)
                    }
                }
            }
            is BarGraph -> {
                val xAxis = CategoryAxis().apply {
                    label = chart.axisNames[0]
                    animated = false
                    // Long labels are automatically rotated to 90°.
                    // Setting it to 0° doesn't change that behaviour *shrug*
                    tickLabelRotation = 360.0
                }
                val fxYAxis = NumberAxis().apply {
                    label = chart.axisNames[0]
                    animated = false
                    isAutoRanging = false
                    lowerBound = chart.lowerBound
                    upperBound = chart.upperBound
                }
                BarChart(xAxis, fxYAxis).apply {
                    val series = XYChart.Series<String, Number>().apply {
                        name = chart.title
                        val emptyList = mutableListOf<XYChart.Data<String, Number>>()
                        data = FXCollections.observableList(emptyList)
                    }

                    for (yAxis in chart.yAxes) {
                        // Start at 0, we need a starting value to later change the yValue of that
                        val data = XYChart.Data(yAxis.description, 0.0 as Number)
                        series.data.add(data)

                        yAxis.dataPoints.addListener(ListChangeListener {
                            it.next()
                            data.yValue = it.addedSubList.last().value
                        })
                    }

                    data.add(series)
                    this.isLegendVisible = false  // It's useless for bar charts
                }
            }
            is AxisGraph -> {
                val xAxis = NumberAxis().apply {
                    label = chart.axisNames[0]
                    isAutoRanging = false
                    tickUnit = 5_000.0  // Tick mark every 5 seconds
                    animated = false

                    tickLabelFormatter = object : StringConverter<Number>() {
                        override fun toString(unixTime: Number): String? {
                            val formatter = DateTimeFormatter.ofPattern("HH:mm:ss")
                            return Instant.ofEpochMilli(unixTime.toLong())
                                .atZone(ZoneId.of("GMT+1"))  // TODO: Think about how to deal with TZs
                                .format(formatter)
                        }

                        override fun fromString(string: String?): Number {
                            // TODO: DateTimeParser to Number
                            return 0
                        }
                    }
                }

                val fxYAxis = NumberAxis().apply {
                    label = chart.axisNames[1]
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
                        // TODO: Maybe dynamically adjust the period, e.g. if a sensors measures faster than 40ms
                        Executors.newSingleThreadScheduledExecutor(NamedThreadFactory("Update ${chart.title} window")).apply {
                            scheduleAtFixedRate({
                                Platform.runLater {
                                    if (paused)
                                        return@runLater

                                    lastUpperBound?.let {
                                        xAxis.upperBound = it
                                        xAxis.lowerBound = xAxis.upperBound - chart.windowSize
                                    }
                                }
                            }, 0, App.instance.settings.chartUpdatePeriod.toLong(), TimeUnit.MILLISECONDS)  // 40ms = 25FPS
                        }
                    }
                }
            }
            is DistributionGraph -> {
                PieChart().apply {
                    this.startAngle = -90.0
                    // Start at 0, we need a starting value to later change the yValue of that
                    val leftData = PieChart.Data("Left", 50.0)
                    val rightData = PieChart.Data("Right", 50.0)

                    chart.axis.dataPoints.addListener(ListChangeListener {
                        it.next()
                        rightData.pieValue = it.addedSubList.last().value
                        leftData.pieValue = 100.0 - rightData.pieValue
                    })

                    data = FXCollections.observableArrayList(listOf(leftData, rightData))
                }
            }
            else -> {
                println("Cannot display this kind of chart")
                null
            }
        }
    }
}
