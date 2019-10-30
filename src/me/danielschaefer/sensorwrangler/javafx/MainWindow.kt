package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.SensorWrangler
import me.danielschaefer.sensorwrangler.gui.Chart
import me.danielschaefer.sensorwrangler.gui.Graph
import kotlin.system.exitProcess

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
                        lowerBound = -10.0
                        upperBound = 10.0
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
                            val mappedList: ObservableList<XYChart.Data<String, Number>> = MappedList(chart.yAxis.values) {
                                XYChart.Data<String, Number>("${it.index}", it.value)
                            }
                            series.data = mappedList
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
            val vBox = VBox(createMenuBar(), chartBox, playBox)
            scene = Scene(vBox, 800.0, 600.0)
            show()
        }
    }

    private fun showAboutPopup(stage: Stage) {
        Stage().apply{
            initModality(Modality.APPLICATION_MODAL)
            initOwner(stage)

            val dialogVbox = VBox(20.0).apply {
                children.add(Text("2019 Daniel Schaefer <git@danielschaefer.me>"))
            }

            scene = Scene(dialogVbox, 300.0, 200.0)
            show()
        }
    }

    private fun createMenuBar(): MenuBar {
        return MenuBar().apply {
            val fileMenu = Menu("File").apply {
                items.add(MenuItem("Open"))
                items.add(MenuItem("Save"))
                items.add(MenuItem("Settings"))
                items.add(MenuItem("Exit").apply {
                    onAction = EventHandler {
                        Platform.exit()
                        // TODO: Properly close all stages
                        exitProcess(0);
                    }
                })
            }

            val sensorMenu = Menu("Sensors").apply {
                items.add(MenuItem("Add"))
                items.add(MenuItem("Manage All"))
            }

            val chartMenu = Menu("Charts").apply {
                items.add(MenuItem("Add"))
                items.add(MenuItem("Manage All"))
            }

            val helpMenu = Menu("Help").apply {
                val aboutItem = MenuItem("About").apply {
                    onAction = EventHandler {
                        showAboutPopup(primaryStage)
                        println("foo")
                    }
                }
                items.add(aboutItem)
            }

            menus.add(fileMenu)
            menus.add(sensorMenu)
            menus.add(chartMenu)
            menus.add(helpMenu)
        }
    }
}