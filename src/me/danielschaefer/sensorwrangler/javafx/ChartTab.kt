package me.danielschaefer.sensorwrangler.javafx

import javafx.beans.value.ChangeListener
import javafx.collections.ListChangeListener
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.StringUtil
import me.danielschaefer.sensorwrangler.gui.*
import me.danielschaefer.sensorwrangler.javafx.popups.AddChartPopup

class ChartTab(parentStage: Stage): Tab("Charts") {
    val chartList: ListView<Chart>

    init {
        content = HBox().apply {
            val chartDetail = VBox(10.0).apply {
                HBox.setHgrow(this, Priority.SOMETIMES)
            }

            chartList = ListView<Chart>().apply {
                items.setAll(App.instance.wrangler.charts)
                App.instance.wrangler.charts.addListener(ListChangeListener {
                    it.next()
                    items.setAll(it.list)
                })

                //setCellFactory {
                //    ListCell()
                //}

                // TODO: Cache these for better performance
                selectionModel.selectedItemProperty().addListener(ChangeListener { _, _, selectedChart ->
                    if (selectedChart == null)
                        return@ChangeListener

                    // TODO: Pass Chart object to avoid searching and possible failure
                    val chartDetailTable = TableView<TableRow>().apply {
                        // Have columns expand to fill all available space
                        columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

                        val firstCol = TableColumn<TableRow, Text>().apply {
                            cellValueFactory = PropertyValueFactory("firstName")
                            isSortable = false
                            isReorderable = false
                        }
                        val secondCol = TableColumn<TableRow, Text>().apply {
                            cellValueFactory = PropertyValueFactory("lastName")
                            isSortable = false
                            isReorderable = false
                        }
                        columns.setAll(firstCol, secondCol)

                        items.setAll(
                            TableRow("Title", selectedChart.title),
                            TableRow("Type", selectedChart::class.simpleName),
                            TableRow("Currently shown?", StringUtil.yesNo(selectedChart.shown.value))
                        )

                        when (selectedChart) {
                            is BarGraph -> {
                                items.addAll(
                                    TableRow("Y-axis label", selectedChart.yAxisLabel),
                                    TableRow("Y-axis lower bound", selectedChart.lowerBound.toString()),
                                    TableRow("Y-axis upper bound", selectedChart.upperBound.toString())
                                )
                                selectedChart.yAxes.forEachIndexed { i, yAxis ->
                                    items.addAll(
                                        TableRow("Y-axis $i sensor", yAxis.sensor.title),
                                        TableRow("Y-axis $i measurement", yAxis.description)
                                    )
                                }
                            }
                            is CurrentValueGraph -> {
                                selectedChart.axes.forEachIndexed { i, yAxis ->
                                    items.addAll(
                                        TableRow("Y-axis $i sensor", yAxis.sensor.title),
                                        TableRow("Y-axis $i measurement", yAxis.description)
                                    )
                                }
                            }
                            is DistributionGraph -> {
                                items.addAll(
                                    TableRow("Sensor", selectedChart.axis.sensor.title),
                                    TableRow("Measurement", selectedChart.axis.description)
                                )
                            }
                            is AxisGraph -> {
                                if (selectedChart is LineGraph)
                                    items.addAll(
                                        TableRow("With dots", StringUtil.yesNo(selectedChart.withDots))
                                    )
                                items.addAll(
                                    TableRow("Y-axis label", selectedChart.yAxisLabel),
                                    TableRow("Y-axis lower bound", selectedChart.lowerBound.toString()),
                                    TableRow("Y-axis upper bound", selectedChart.upperBound.toString()),
                                    TableRow("Y-axis tick spacing", selectedChart.tickSpacing.toString()),
                                    TableRow("Window size [s]", (selectedChart.windowSize / 1_000).toString())
                                )
                                selectedChart.yAxes.forEachIndexed { i, yAxis ->
                                    items.addAll(
                                        TableRow("Y-axis $i sensor", yAxis.sensor.title),
                                        TableRow("Y-axis $i measurement", yAxis.description)
                                    )
                                }
                            }
                        }
                    }

                    val removeChartButton = Button("Remove Chart").apply {
                        setOnAction {
                            App.instance.wrangler.charts.remove(selectedChart)
                            if (App.instance.wrangler.charts.size > 0) {
                                selectionModel.select(items.last())
                            } else {
                                chartDetail.children.clear()
                            }
                        }
                    }

                    chartDetail.children.setAll(chartDetailTable, removeChartButton)
                })
            }

            val addChartButton = Button("Add Chart").apply {
                setOnAction {
                    AddChartPopup(parentStage, this@ChartTab)
                }
            }
            val chartListSidebar = VBox(10.0, chartList, addChartButton)
            chartListSidebar.children.add(chartDetail)

            val separator = Separator().apply {
                orientation = Orientation.VERTICAL
                padding = Insets(10.0)
            }
            children.addAll(chartListSidebar, separator, chartDetail)
        }
    }
}
