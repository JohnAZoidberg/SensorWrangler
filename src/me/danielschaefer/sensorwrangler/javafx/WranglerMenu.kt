package me.danielschaefer.sensorwrangler.javafx

import javafx.application.Platform
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.control.Menu
import javafx.scene.control.MenuBar
import javafx.scene.control.MenuItem
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Modality
import javafx.stage.Stage

fun createMenuBar(primaryStage: Stage): MenuBar {
    return MenuBar().apply {
        val fileMenu = Menu("File").apply {
            items.add(MenuItem("Open"))
            items.add(MenuItem("Save"))
            items.add(MenuItem("Settings"))
            items.add(MenuItem("Exit").apply {
                onAction = EventHandler {
                    Platform.exit()
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

        // TODO: Rewrite this with something better than bunching Text objects together
        val titleText   = Text("SensorWrangler ${App.instance!!.settings.version}")
        val sensorText  = Text("Built-in sensor drivers:")
        val virtualSensorText = Text("Available virtual sensor types")
        val chartText   = Text("Available chart types:")

        val freeSoftware = Hyperlink("Free Software").apply{
            setOnAction {
                App.instance!!.hostServices.showDocument("https://www.gnu.org/philosophy/free-sw.en.html")
            }
        }
        val github = Hyperlink("https://github.com/JohnAZoidberg/SensorWrangler\n").apply{
            setOnAction {
                App.instance!!.hostServices.showDocument("https://github.com/JohnAZoidberg/SensorWrangler")
            }
        }
        val licenseText = TextFlow(Text("""
                This program is """.trimIndent()),
            freeSoftware,
            Text("""
                licensed under the terms of the GPLv2 with classpath exception.
                It was developed at the Corporate State University Baden-Württemberg (DHBW) in Stuttgart.
                Source code hosting and issue tracking is available at
            """.trimIndent()),
            github,
            Text("\nContributions are always welcome."))

        val email = Hyperlink("<git@danielschaefer.me>").apply{
            setOnAction{
                // FIXME: Always launches the browser first, instead of MUA
                // Try to get the libgnome integration https://docs.oracle.com/javase/tutorial/uiswing/misc/desktop.html
                // Or use something like
                //   https://github.com/jjYBdx4IL/misc/blob/master/swing-utils/src/main/java/com/github/jjYBdx4IL/utils/awt/Desktop.java
                // Or
                //   https://stackoverflow.com/a/18004334/5932056
                App.instance!!.hostServices.showDocument("mailto:git@danielschaefer.me")
            }
        }
        val me  = Text("Copyright 2019 Daniel Schaefer ")
        val aboutText = TextFlow(titleText, me, email)

        scene = Scene(VBox(25.0,
            titleText,
            sensorText,
            virtualSensorText,
            chartText,
            licenseText,
            aboutText).apply {
            padding = Insets(25.0)
        })
        title = "About - SensorWrangler"

        sizeToScene()
        show()
    }
}
