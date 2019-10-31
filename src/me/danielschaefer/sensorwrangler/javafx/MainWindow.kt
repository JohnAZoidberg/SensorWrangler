package me.danielschaefer.sensorwrangler.javafx

import javafx.collections.FXCollections
import javafx.collections.ListChangeListener
import javafx.scene.Scene
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.Button
import javafx.scene.control.Slider
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.gui.Graph

class MainWindow(private val primaryStage: Stage, private val wrangler: SensorWrangler) {
    private val jfxSettings = JavaFxSettings()

    init {
        // Create charts
        val fxCharts: MutableList<javafx.scene.chart.Chart> = mutableListOf()
        for (chart: Chart in wrangler.charts.filter {it.shown}) {
            when (chart) {
                is Graph -> {
                    val xAxis = CategoryAxis().apply {
                        label = "Time/s"
                        animated = false
                    }

                    val yAxis = NumberAxis().apply {
                        label = "Value"
                        isAutoRanging = false
                        lowerBound = 25.0
                        upperBound = -25.0
                        tickUnit = 1.0
                        animated = false
                    }
                    val fxChart = LineChart(xAxis, yAxis).apply {
                        title = chart.title
                        animated = false
                        val series = XYChart.Series<String, Number>().apply{
                            name = "Data"
                        }
                        if (chart.yAxis != null) {
                            // Need to attach it to chart, otherwise it gets garbage collected
                            // TODO: Remove this really bad hack
                            chart.mappedList = MappedList(chart.yAxis.values)  {
                                val foo: XYChart.Data<String, Number> = XYChart.Data("${it.index}", it.value)
                                foo
                            }

                            val emptyList = mutableListOf<XYChart.Data<String, Number>>()
                            series.data = FXCollections.observableList(emptyList)
                            series.data.addAll(listOf(1, 2, 3, 4, 5).map({ XYChart.Data<String, Number>("foo$it", it)}))

                            // TODO: Do this more efficiently without reassigning the entire list
                            chart.mappedList!!.addListener(ListChangeListener {
                                val first = if (chart.mappedList!!.size > chart.windowSize) chart.mappedList!!.size - chart.windowSize else 0;
                                series.data.setAll(chart.mappedList!!.subList(first, chart.mappedList!!.size))
                            })
                            //series.data = mappedList
                        } else {
                            val emptyList = mutableListOf<XYChart.Data<String, Number>>()
                            series.data = FXCollections.observableList(emptyList)
                            series.data.addAll(listOf(1, 2, 3, 4, 5).map({ XYChart.Data<String, Number>("foo$it", it)}))
                        }
                        data.add(series)
                    }
                    fxCharts.add(fxChart)
                }
                else -> println("Cannot display this kind of chart")
            }
        }

        primaryStage.apply {
            title = "SensorWrangler"
            val chartBox = GridPane().apply {
                val rows = jfxSettings.rows;
                val cols = jfxSettings.cols;

                val fxChartIterator = fxCharts.iterator()
                rowLoop@ for (row in 0 until rows) {
                    for (col in 0 until cols) {
                        if (!fxChartIterator.hasNext()) {
                            println("More charts than fit in the grid")
                            break@rowLoop
                        }
                        add(fxChartIterator.next(), col, row)
                    }
                }
            }

            val playBox = HBox().apply {
                val slider = Slider().apply{
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
                buttonCurrent.setPrefSize(100.0, 20.0)

                val buttonProjected = Button("Start Recording")
                buttonProjected.setPrefSize(100.0, 20.0)
                children.addAll(slider, buttonCurrent, buttonProjected)
            }
            val vBox = VBox(createMenuBar(primaryStage), chartBox, playBox)

            scene = Scene(vBox, 800.0, 600.0)
            sizeToScene()
            show()
        }
    }
}