package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.dialogs.AboutPopup
import me.danielschaefer.sensorwrangler.javafx.dialogs.AddChartPopup
import me.danielschaefer.sensorwrangler.javafx.dialogs.StartRecordingPopup


fun createMenuBar(primaryStage: Stage): MenuBar {
    return MenuBar().apply {
        val fileMenu = Menu("File").apply {
            items.add(MenuItem("Open"))
            items.add(MenuItem("Save"))
            items.add(MenuItem("Settings").apply {
                onAction = EventHandler {
                    Settings(primaryStage)
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
        }

        val sensorMenu = Menu("Sensors").apply {
            items.add(MenuItem("Add"))
            items.add(MenuItem("Manage All"))
        }

        val chartMenu = Menu("Charts").apply {
            items.add(MenuItem("Add").apply {
                onAction = EventHandler {
                    AddChartPopup(primaryStage)
                }
            })
            items.add(MenuItem("Manage All"))
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
    if (App.instance == null)
        return mutableListOf()

    val foo = mutableListOf<String?>()
    for (sensor in App.instance!!.wrangler.sensors) {
        for (measurement in sensor.measurements) {
            foo.add("${sensor.title}: ${measurement.description}")
        }
    }
    return foo
}
