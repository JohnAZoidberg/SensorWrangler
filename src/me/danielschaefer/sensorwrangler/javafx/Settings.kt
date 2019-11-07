package me.danielschaefer.sensorwrangler.javafx

import javafx.beans.value.ChangeListener
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.geometry.Insets
import javafx.geometry.Orientation
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.control.cell.PropertyValueFactory
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.StringUtil
import me.danielschaefer.sensorwrangler.javafx.dialogs.AddChartPopup


class Settings(val parentStage: Stage) : Stage() {
    init {
        // TODO: Do we want to restrict the user like this?
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parentStage)

        val mainContent = VBox().apply {
            val tabPane = TabPane().apply {
                val tab1 = Tab("Sensors", HBox(Label("All Sensors")))

                val tab2 = Tab("Charts", HBox().apply {
                    var chartDetail = VBox()

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
                            AddChartPopup(this@Settings)
                        }
                    }
                    val chartListSidebar = VBox(chartList, addChartButton)
                    chartListSidebar.children.add(chartDetail)

                    val separator = Separator().apply {
                        orientation = Orientation.VERTICAL
                        padding = Insets(10.0)
                    }
                    children.addAll(chartListSidebar, separator, chartDetail)
                })

                val tab3 = Tab("Formulas", HBox().apply {
                    children.addAll(Label("Configure formulas (virtual sensors created by applying measurements to a formula)"))
                })
                tabs.addAll(tab1, tab2, tab3)

                // TODO: Do using CSS .tab-pane > .tab-content-area > * { -fx-padding: 25; }
                val insets = Insets(25.0)
                for (tab in tabs)
                    (tab.content as HBox).padding = insets

                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            }

            children.addAll(tabPane)
        }

        scene = Scene(mainContent, 800.0, 600.0)
        title = "Settings"

        sizeToScene()
        show()
    }
}