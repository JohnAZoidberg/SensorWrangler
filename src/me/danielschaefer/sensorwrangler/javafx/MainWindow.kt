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
import javafx.stage.Stage
import javafx.util.StringConverter
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.gui.*
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.javafx.popups.StartRecordingPopup
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MainWindow(private val primaryStage: Stage, private val wrangler: SensorWrangler) {
    private val jfxSettings = JavaFxSettings()

    init {
        wrangler.import(App.instance.settings.defaultExportPath)
        primaryStage.apply {
            title = "SensorWrangler"

            val allChartsBox = GridPane().apply {
                padding = Insets(25.0)

                val rows = jfxSettings.rows;
                val cols = jfxSettings.cols;

                //val fxChartIterator = fxCharts.iterator()
                rowLoop@ for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        // Dummy chart as a placeholder
                        val chartBox = VBox()
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
                                    //val newChart = LineChart<Number, Number>(NumberAxis().apply{ label = newValue}, NumberAxis())
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
            val playBox = HBox().apply {
                padding = Insets(25.0)

                val spacer = fun() = Region().apply {
                    HBox.setHgrow(this, Priority.ALWAYS)
                }

                val slider = Slider().apply {
                    min = 0.0;
                    max = 100.0;
                    value = 40.0;
                    isShowTickLabels = true;
                    isShowTickMarks = true;
                    majorTickUnit = 50.0;
                    minorTickCount = 5;
                    blockIncrement = 10.0;
                }

                val buttonCurrent = Button("Pause")

                val buttonProjected = Button("Start Recording").apply {
                    onAction = EventHandler {
                        if (App.instance.wrangler.isRecording.value) {
                            App.instance.wrangler.stopRecording()
                        } else {
                            if (App.instance.settings.recordingDirectory == null)
                               StartRecordingPopup(primaryStage)
                            else
                                App.instance.wrangler.startRecording()
                        }
                    }
                    App.instance.wrangler.isRecording.addListener(ChangeListener<Boolean> { observable, old, new ->
                        text = if (new) "Stop Recording" else "Start Recording"
                    })
                }

                children.addAll(spacer(), buttonProjected)
            }
            val vBox = VBox(createMenuBar(primaryStage), allChartsBox, playBox)

            scene = Scene(vBox, 800.0, 600.0)
            // TODO: Set an icon for the program - how to embed resources in the .jar?
            //icons.add(Image(javaClass.getResourceAsStream("ruler.png")))
            sizeToScene()
            show()
        }
    }

    private fun createFxChart(chart: Chart): Node? {
        when (chart) {
            is BarGraph -> {
                val xAxis = CategoryAxis().apply {
                    label = chart.axisNames[0]
                    animated = false
                }
                val fxYAxis = NumberAxis().apply {
                    label = chart.axisNames[0]
                    animated = false
                    isAutoRanging = false
                    lowerBound = chart.lowerBound
                    upperBound = chart.upperBound
                }
                return BarChart(xAxis, fxYAxis).apply {
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
                    is LineGraph -> LineChart(xAxis, fxYAxis)
                    is ScatterGraph -> ScatterChart(xAxis, fxYAxis)
                    else -> {
                        println("Cannot display this kind of chart")
                        null
                    }
                }

                return fxChart?.apply {
                    title = chart.title
                    animated = false
                    for (yAxis in chart.yAxes) {
                        XYChart.Series<Number, Number>().apply {
                            name = yAxis.description ?: "Data"

                            // TODO: Maybe we can have it as a list of measurements and there is something like a cellFactory for charts?
                            data = MappedList(yAxis.dataPoints) {
                                val datum: XYChart.Data<Number, Number> = XYChart.Data(it.value.timestamp, it.value.value)
                                datum
                            }

                            fxChart.data.add(this)
                        }

                        // Show data from now until chart.windowSize ago
                        // TODO: Maybe dynamically adjust the period, e.g. if a sensors measures faster than 40ms
                        Executors.newSingleThreadScheduledExecutor().apply {
                            scheduleAtFixedRate({
                                Platform.runLater {
                                    xAxis.upperBound = Date().time.toDouble()
                                    xAxis.lowerBound = xAxis.upperBound - chart.windowSize
                                }
                            }, 0, 40, TimeUnit.MILLISECONDS)  // 40ms = 25FPS
                        }
                    }
                }
            }
            else -> println("Cannot display this kind of chart")
        }
        return null
    }
}
