package me.danielschaefer.sensorwrangler.javafx

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage

class Alert(parent: Stage, alertTitle: String, alertText: String): Stage() {
    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)

        val textLabel = Text(alertText)
        val okButton = Button("OK").apply {
            onAction = EventHandler {
                close()
            }
        }
        val vBox = VBox(textLabel, okButton).apply {
            padding = Insets(25.0)
        }

        scene = Scene(vBox)
        title = alertTitle

        sizeToScene()
        show()
    }
}