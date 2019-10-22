package me.danielschaefer.sensorwrangler

import javafx.application.Application
import javafx.application.Platform
import javafx.scene.Scene
import javafx.scene.chart.CategoryAxis
import javafx.scene.chart.LineChart
import javafx.scene.chart.NumberAxis
import javafx.scene.chart.XYChart
import javafx.stage.Stage

import java.time.format.DateTimeFormatter
import java.time.LocalTime
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

fun main(args: Array<String>) {
    Application.launch(*args)
}

class Main : Application() {
    private val windowSize = 50
    private val timeFormat = DateTimeFormatter.ofPattern("HH:mm:ss")

    private val series: XYChart.Series<String, Number> = XYChart.Series()
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

        val lineChart = LineChart(xAxis, yAxis).apply {
            title = "Realtime JavaFX Charts"
            animated = false
            data.add(series)
            series.name = "Data Series"
        }

        for (i in 0 until windowSize)
            addDataPoint(windowSize - i)

        primaryStage.apply {
            title = "JavaFX Realtime Chart"
            scene = Scene(lineChart, 800.0, 600.0)
            show()
        }

        scheduledExecutorService.scheduleAtFixedRate({
            Platform.runLater { addDataPoint() }
        }, 0, 1, TimeUnit.SECONDS)
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
