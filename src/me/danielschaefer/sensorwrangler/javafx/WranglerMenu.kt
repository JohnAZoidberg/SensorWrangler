package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.scene.Scene
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import kotlin.system.exitProcess

fun createMenuBar(primaryStage: Stage): MenuBar {
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

fun showAboutPopup(stage: Stage) {
    Stage().apply{
        initModality(Modality.APPLICATION_MODAL)
        initOwner(stage)

        val dialogVbox = VBox(20.0).apply {
            children.add(Text("2019 Daniel Schaefer <git@danielschaefer.me>"))
        }

        scene = Scene(dialogVbox, 300.0, 200.0)
        title = "About - SensorWrangler"

        sizeToScene()
        show()
    }
}
