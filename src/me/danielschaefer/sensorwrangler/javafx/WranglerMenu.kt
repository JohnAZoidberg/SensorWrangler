package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.popups.AboutPopup
import me.danielschaefer.sensorwrangler.javafx.popups.AddChartPopup
import me.danielschaefer.sensorwrangler.javafx.popups.AddSensorPopup
import me.danielschaefer.sensorwrangler.javafx.popups.StartRecordingPopup
import java.io.File


fun createMenuBar(primaryStage: Stage): MenuBar {
    return MenuBar().apply {
        val fileMenu = Menu("File").apply {
            // TODO: What should they even do?
            items.add(MenuItem("Open").apply { onAction = EventHandler {
                FileChooser().apply {
                    App.instance.settings.defaultFileSensorPath?.let {
                        initialDirectory = File(it)
                    }
                    showOpenDialog(primaryStage)?.absolutePath?.let {
                        App.instance.wrangler.import(it)
                    }
                }
            }})
            items.add(MenuItem("Save").apply { onAction = EventHandler {
                FileChooser().apply {
                    App.instance.settings.defaultFileSensorPath?.let {
                        initialDirectory = File(it)
                    }
                    showSaveDialog(primaryStage)?.absolutePath?.let {
                        App.instance.wrangler.export(it)
                    }
                }
            }})

            items.add(MenuItem("Settings").apply {
                onAction = EventHandler {
                    SettingsWindow(primaryStage)
                }
            })
            items.add(MenuItem("Exit").apply {
                onAction = EventHandler {
                    Platform.exit()
                }
            })
        }

        val recordingMenu = Menu("Recording").apply {
            items.add(MenuItem("Start Recording").apply {
                onAction = EventHandler {
                    StartRecordingPopup(primaryStage)
                }
            })
            items.add(MenuItem("Stop Recording").apply {
                onAction = EventHandler {
                    App.instance.wrangler.stopRecording()
                }
            })
        }

        val sensorMenu = Menu("Sensors").apply {
            items.add(MenuItem("Add").apply { onAction = EventHandler {
                AddSensorPopup(primaryStage)
            }})
            items.add(MenuItem("Manage All").apply {
                onAction = EventHandler {
                    SettingsWindow(primaryStage).apply {
                        tabPane.selectionModel.select(this.sensorTab)
                    }
                }
            })
        }

        val chartMenu = Menu("Charts").apply {
            items.add(MenuItem("Add").apply {
                onAction = EventHandler {
                    AddChartPopup(primaryStage)
                }
            })
            items.add(MenuItem("Manage All").apply {
                onAction = EventHandler {
                    SettingsWindow(primaryStage).apply {
                        tabPane.selectionModel.select(this.chartTab)
                    }
                }
            })
        }

        val helpMenu = Menu("Help").apply {
            val aboutItem = MenuItem("About").apply {
                onAction = EventHandler {
                    AboutPopup(primaryStage)
                }
            }
            items.add(aboutItem)
        }

        menus.add(fileMenu)
        menus.add(recordingMenu)
        menus.add(sensorMenu)
        menus.add(chartMenu)
        menus.add(helpMenu)
    }
}

fun getMeasurements(): MutableList<String?> {
    return App.instance.wrangler.sensors.flatMap { sensor ->
        sensor.measurements.map { measurement ->
            "${sensor.title}: ${measurement.description}"
        }
    }.toMutableList()
}
