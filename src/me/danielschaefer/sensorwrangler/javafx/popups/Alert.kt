package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.input.KeyCode
import javafx.scene.input.KeyEvent
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage

open class Alert(parent: Stage, alertTitle: String, alertText: String) : Stage() {
    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parent)

        val textLabel = Text(alertText)
        val okButton = Button("OK").apply {
            onAction = EventHandler {
                close()
            }
        }
        val vBox = VBox(15.0, textLabel, okButton).apply {
            padding = Insets(25.0)
            alignment = Pos.CENTER
        }

        scene = Scene(vBox)
        scene.addEventHandler(
            KeyEvent.KEY_PRESSED,
            EventHandler {
                if (it.code == KeyCode.ESCAPE)
                    close()
            }
        )
        title = alertTitle

        sizeToScene()
        show()
    }
}

class TodoAlert(parent: Stage) : Alert(parent, "TODO", "This functionality is not yet implemented")
