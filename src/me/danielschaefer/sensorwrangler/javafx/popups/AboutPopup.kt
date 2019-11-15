package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Hyperlink
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.scene.text.TextFlow
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.App
import kotlin.reflect.KClass

class AboutPopup(val parentStage: Stage): Stage() {
    private fun foldClassNames(classes: Collection<KClass<*>>): String {
        return classes.fold("", { acc, cls -> "$acc\n  - ${cls.simpleName}" })
    }

    init {
        initOwner(parentStage)

        // TODO: Rewrite this with something better than bunching Text objects together
        // TODO: Add information about available sensors, virtual sensors and charts
        val titleText =
            Text("SensorWrangler ${me.danielschaefer.sensorwrangler.javafx.App.instance!!.settings.version}")
        val sensorText = Text("Built-in sensor drivers:" + foldClassNames(App.instance!!.settings.supportedSensors))
        val virtualSensorText = Text("Available formulas types:" + foldClassNames(App.instance!!.settings.supportedFormulas))
        val chartText = Text("Available chart types:" + foldClassNames(App.instance!!.settings.supportedCharts))

        val freeSoftware = Hyperlink("Free Software").apply {
            setOnAction {
                me.danielschaefer.sensorwrangler.javafx.App.instance!!.hostServices.showDocument("https://www.gnu.org/philosophy/free-sw.en.html")
            }
        }
        val github = Hyperlink("https://github.com/JohnAZoidberg/SensorWrangler\n").apply {
            setOnAction {
                me.danielschaefer.sensorwrangler.javafx.App.instance!!.hostServices.showDocument("https://github.com/JohnAZoidberg/SensorWrangler")
            }
        }
        val licenseText = TextFlow(
            Text(
                """
                This program is """.trimIndent()
            ),
            freeSoftware,
            Text(
                """
                licensed under the terms of the GNU Public License version 2 (GPLv2).
                It was developed at the Corporate State University Baden-Württemberg (DHBW) in Stuttgart.
                Source code hosting and issue tracking is available at
            """.trimIndent()
            ),
            github,
            Text(".\nContributions are always welcome.")
        )

        val email = Hyperlink("<git@danielschaefer.me>").apply {
            setOnAction {
                // FIXME: Always launches the browser first, instead of MUA
                // Try to get the libgnome integration https://docs.oracle.com/javase/tutorial/uiswing/misc/desktop.html
                // Or use something like
                //   https://github.com/jjYBdx4IL/misc/blob/master/swing-utils/src/main/java/com/github/jjYBdx4IL/utils/awt/Desktop.java
                // Or
                //   https://stackoverflow.com/a/18004334/5932056
                me.danielschaefer.sensorwrangler.javafx.App.instance!!.hostServices.showDocument("mailto:git@danielschaefer.me")
            }
        }
        val me = Text("Copyright 2019 Daniel Schaefer ")
        val aboutText = TextFlow(titleText, me, email)

        scene = Scene(
            VBox(
            25.0,
            titleText,
            sensorText,
            virtualSensorText,
            chartText,
            licenseText,
            aboutText
        ).apply {
            padding = Insets(25.0)
        })
        title = "About - SensorWrangler"

        sizeToScene()
        show()
    }
}