package me.danielschaefer.sensorwrangler.javafx

import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Slider
import javafx.scene.layout.*
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.gui.Graph
import me.danielschaefer.sensorwrangler.javafx.popups.StartRecordingPopup

class MainWindow(private val primaryStage: Stage, private val wrangler: SensorWrangler) {
    private val jfxSettings = JavaFxSettings()

    init {
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
                            App.instance!!.wrangler.charts.addListener(ListChangeListener {
                                items.setAll(it.list.map { it.title })
                            })
                            items.addAll(App.instance!!.wrangler.charts.map { it.title })
                            valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                                oldValue?.let {
                                    App.instance!!.wrangler.findChartByTitle(oldValue)?.let {
                                        it.shown = false
                                    }
                                }

                                App.instance!!.wrangler.findChartByTitle(newValue)?.let {
                                    it.shown = true
                                    //val newChart = LineChart<Number, Number>(NumberAxis().apply{ label = newValue}, NumberAxis())
                                    chartBox.children[0] = createFxChart(it)
                                }
                                println("From $oldValue to $newValue")
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
                        if (App.instance!!.wrangler.isRecording.value) {
                            App.instance!!.wrangler.stopRecording()
                        } else {
                            if (App.instance!!.settings.recordingDirectory == null)
                               StartRecordingPopup(primaryStage)
                            else
                                App.instance!!.wrangler.startRecording()
                        }
                    }
                    App.instance!!.wrangler.isRecording.addListener(ChangeListener<Boolean> { observable, old, new ->
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
            is Graph -> {
                val xAxis = CategoryAxis().apply {
                    label = "Time/s"
                    animated = false
                }

                val yAxis = NumberAxis().apply {
                    label = "Value"
                    isAutoRanging = false
                    lowerBound = chart.lowerBound
                    upperBound = chart.upperBound
                    tickUnit = chart.tickSpacing
                    animated = false
                }
                return LineChart(xAxis, yAxis).apply {
                    title = chart.title
                    animated = false
                    val series = XYChart.Series<String, Number>().apply {
                        name = "Data"
                    }
                    if (chart.yAxis != null) {
                        // Need to attach it to chart, otherwise it gets garbage collected
                        // TODO: Remove this really bad hack
                        if (chart.mappedList == null) {
                            chart.mappedList = MappedList(chart.yAxis.values) {
                                 XYChart.Data("${it.index}", it.value as Number)
                            }
                        }

                        val emptyList = mutableListOf<XYChart.Data<String, Number>>()
                        series.data = FXCollections.observableList(emptyList)
                        series.data.addAll(listOf(1, 2, 3, 4, 5).map({ XYChart.Data<String, Number>("foo$it", it) }))

                        // TODO: Do this more efficiently without reassigning the entire list
                        chart.mappedList!!.addListener(ListChangeListener {
                            val first =
                                if (chart.mappedList!!.size > chart.windowSize) chart.mappedList!!.size - chart.windowSize else 0;
                            series.data.setAll(chart.mappedList!!.subList(first, chart.mappedList!!.size))
                        })
                        //series.data = mappedList
                    } else {
                        val emptyList = mutableListOf<XYChart.Data<String, Number>>()
                        series.data = FXCollections.observableList(emptyList)
                        series.data.addAll(listOf(1, 2, 3, 4, 5).map({ XYChart.Data<String, Number>("foo$it", it) }))
                    }
                    data.add(series)
                }
            }
            else -> println("Cannot display this kind of chart")
        }
        return null
    }
}
