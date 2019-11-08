package me.danielschaefer.sensorwrangler.javafx

import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.StringUtil
import me.danielschaefer.sensorwrangler.javafx.popups.AddChartPopup

class ChartTab(parentStage: Stage): Tab("Sensors") {
    init {
        content = HBox().apply {
            val chartDetail = VBox()

            val chartList = ListView<Text>().apply {
                val charts: ObservableList<Text> = FXCollections.observableList(mutableListOf())

                for (chart in App.instance!!.wrangler.charts) {
                    charts.add(Text(chart.title))
                }

                items = charts

                // TODO: Cache these for better performance
                selectionModel.selectedItemProperty().addListener(ChangeListener { x, oldValue, newValue ->
                    // TODO: Pass Chart object to avoid searching and possible failure
                    val chart = App.instance!!.wrangler.findChartByTitle(newValue.text)
                    chart?.let {
                        val chartDetailTable = TableView<TableRow>().apply {
                            // Have columns expand to fill all available space
                            columnResizePolicy = TableView.CONSTRAINED_RESIZE_POLICY

                            val firstCol = TableColumn<TableRow, Text>().apply {
                                cellValueFactory = PropertyValueFactory("firstName")
                            }
                            val secondCol = TableColumn<TableRow, Text>().apply {
                                cellValueFactory = PropertyValueFactory("lastName")
                            }
                            columns.setAll(firstCol, secondCol)

                            items.setAll(
                                TableRow("Title", chart.title),
                                TableRow("Currently shown?", StringUtil.yesNo(chart.shown))
                            )
                        }

                        val removeChartButton = Button("Remove Chart").apply {
                            setOnAction {
                                App.instance!!.wrangler.charts.remove(chart)
                            }
                        }

                        chartDetail.children.setAll(chartDetailTable, removeChartButton)
                    }
                })
            }

            val addChartButton = Button("Add Chart").apply {
                setOnAction {
                    AddChartPopup(parentStage)
                }
            }
            val chartListSidebar = VBox(chartList, addChartButton)
            chartListSidebar.children.add(chartDetail)

            val separator = Separator().apply {
                orientation = Orientation.VERTICAL
                padding = Insets(10.0)
            }
            children.addAll(chartListSidebar, separator, chartDetail)
        }
    }
}