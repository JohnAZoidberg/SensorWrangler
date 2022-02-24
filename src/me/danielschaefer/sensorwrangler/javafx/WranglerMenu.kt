package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.popups.AboutPopup
import me.danielschaefer.sensorwrangler.javafx.popups.AddAveragePopup
import me.danielschaefer.sensorwrangler.javafx.popups.AddChartPopup
import me.danielschaefer.sensorwrangler.javafx.popups.AddSensorPopup
import me.danielschaefer.sensorwrangler.javafx.popups.Alert
import me.danielschaefer.sensorwrangler.javafx.popups.StartRecordingPopup
import me.danielschaefer.sensorwrangler.javafx.popups.test.DataPointsExporter
import java.util.*

fun createMenuBar(primaryStage: Stage): MenuBar {
    return MenuBar().apply {
        val fileMenu = Menu("File").apply {
            // TODO: What should they even do?
            items.add(
                MenuItem("Open").apply {
                    onAction = EventHandler {
                        FileChooser().apply {
                            showOpenDialog(primaryStage)?.absolutePath?.let {
                                if (!App.instance.wrangler.import(it))
                                    Alert(
                                        primaryStage, "Import failed",
                                        "Failed to import configuration because '$it' was not found."
                                    )
                            }
                        }
                    }
                }
            )
            items.add(
                MenuItem("Save").apply {
                    onAction = EventHandler {
                        FileChooser().apply {
                            showSaveDialog(primaryStage)?.absolutePath?.let {
                                App.instance.wrangler.export(it)
                            }
                        }
                    }
                }
            )

            items.add(
                MenuItem("Settings").apply {
                    onAction = EventHandler {
                        SettingsWindow(primaryStage)
                    }
                }
            )
            items.add(
                MenuItem("Exit").apply {
                    onAction = EventHandler {
                        DataPointsExporter().closeWriter()
                        Platform.exit()
                    }
                }
            )
        }

        val recordingMenu = Menu("Recording").apply {
            items.add(
                MenuItem("Add Recorder").apply {
                    onAction = EventHandler {
                        StartRecordingPopup(primaryStage)
                    }
                }
            )
            items.add(
                MenuItem("Stop Recording").apply {
                    onAction = EventHandler {
                        App.instance.wrangler.stopRecording()
                    }
                }
            )
        }

        val sensorMenu = Menu("Sensors/Timers").apply {
            items.add(
                MenuItem("Add").apply {
                    onAction = EventHandler {
                        AddSensorPopup(primaryStage)
                    }
                }
            )
            items.add(
                MenuItem("Manage All").apply {
                    onAction = EventHandler {
                        SettingsWindow(primaryStage).apply {
                            tabPane.selectionModel.select(this.sensorTab)
                        }
                    }
                }
            )
        }

        val chartMenu = Menu("Charts").apply {
            items.add(
                MenuItem("Add").apply {
                    onAction = EventHandler {
                        AddChartPopup(primaryStage)
                    }
                }
            )
            items.add(
                MenuItem("Manage All").apply {
                    onAction = EventHandler {
                        SettingsWindow(primaryStage).apply {
                            tabPane.selectionModel.select(this.chartTab)
                        }
                    }
                }
            )
        }

        val averageMenu = Menu("Averages").apply {
            items.add(
                MenuItem("Add").apply {
                    onAction = EventHandler {
                        AddAveragePopup(primaryStage)
                    }
                }
            )
        }

        val helpMenu = Menu("Help").apply {
            val aboutItem = MenuItem("About").apply {
                onAction = EventHandler {
                    AboutPopup(primaryStage)
                }
            }
            items.add(aboutItem)
        }

        val testMenu = Menu("Test").apply {
            val exportDataPoints = MenuItem("Data Points").apply {
                onAction = EventHandler {
                    DataPointsExporter().extractDataPoints()

                }
            }
            items.add(exportDataPoints)
        }

        menus.add(fileMenu)
        menus.add(recordingMenu)
        menus.add(sensorMenu)
        menus.add(chartMenu)
        menus.add(averageMenu)
        menus.add(helpMenu)
        menus.add(testMenu)
    }
}

fun getMeasurements(): MutableList<String?> {
    return App.instance.wrangler.sensors.flatMap { sensor ->
        sensor.measurements.map { measurement ->
            "${sensor.title}: ${measurement.description}"
        }
    }.toMutableList()
}
