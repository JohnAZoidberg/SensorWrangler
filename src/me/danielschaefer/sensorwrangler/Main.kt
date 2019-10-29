package me.danielschaefer.sensorwrangler

import javafx.application.Application
import javafx.application.Platform
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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit


fun main(args: Array<String>) {
    Application.launch(Main::class.java, *args)
}

class Main : Application() {
    private val windowSize = 50
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val series: XYChart.Series<String, Number> = XYChart.Series<String, Number>().apply{
        name = "Data"
    }
    private val scheduledExecutorService: ScheduledExecutorService = Executors.newSingleThreadScheduledExecutor()

    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
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

        for (i in 0 until windowSize)
            addDataPoint(windowSize - i)

        val lineChart1 = LineChart(xAxis, yAxis).apply {
            title = "Altitude"
            animated = false
            data.add(series)
        }

        val lineChart2 = LineChart(xAxis, yAxis).apply {
            title = "Pedal Frequency"
            animated = false
            data.add(series)
        }

        val lineChart3 = LineChart(xAxis, yAxis).apply {
            title = "Power"
            animated = false
            data.add(series)
        }

        val lineChart4 = LineChart(xAxis, yAxis).apply {
            title = "Heart Rate"
            animated = false
            data.add(series)
        }

        val menuBar = MenuBar().apply {
            val fileMenu = Menu("File").apply {
                items.add(MenuItem("Open"))
                items.add(MenuItem("Save"))
                items.add(MenuItem("Exit"))
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

        primaryStage.apply {
            title = "JavaFX Realtime Chart"
            val chartBox = GridPane().apply {
                add(lineChart1, 0, 0)
                add(lineChart2, 0, 1)
                add(lineChart3, 1, 0)
                add(lineChart4, 1, 1)
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
            val vBox = VBox(menuBar, chartBox, playBox)
            scene = Scene(vBox, 800.0, 600.0)
            show()
        }

        scheduledExecutorService.scheduleAtFixedRate({
            Platform.runLater { addDataPoint() }
        }, 0, 1, TimeUnit.SECONDS)
    }

    private fun showAboutPopup(stage: Stage) {
        val dialog = Stage().apply{
            initModality(Modality.APPLICATION_MODAL)
            initOwner(stage)

            val dialogVbox = VBox(20.0).apply {
                children.add(Text("2019 Daniel Schaefer <git@danielschaefer.me>"))
            }

            scene = Scene(dialogVbox, 300.0, 200.0)
            show()
        }
    }

    private fun addDataPoint(fakeSecondOffset: Int = 0) {
        val random = ThreadLocalRandom.current().nextInt(20) - 10
        val now = LocalTime.now().minusSeconds(fakeSecondOffset.toLong())
        series.data.add(XYChart.Data(now.format(timeFormat), random))

        if (series.data.size > windowSize)
            series.data.removeAt(0)
    }

    @Throws(Exception::class)
    override fun stop() {
        super.stop()
        scheduledExecutorService.shutdownNow()
    }
}
