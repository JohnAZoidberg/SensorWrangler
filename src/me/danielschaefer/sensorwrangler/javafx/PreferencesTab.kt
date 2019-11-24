package me.danielschaefer.sensorwrangler.javafx

import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.DirectoryChooser
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.Picker
import me.danielschaefer.sensorwrangler.Preference
import me.danielschaefer.sensorwrangler.Settings
import java.io.File
import kotlin.reflect.KMutableProperty
import kotlin.reflect.full.declaredMemberProperties
import kotlin.reflect.typeOf


class PreferencesTab(private val parentStage: Stage) : Tab("Preferences") {
    private val contentBox = GridPane().apply {
        padding = Insets(25.0)
        hgap = 10.0
        vgap = 10.0
    }

    init {
        var row = 1
        for (property in Settings::class.declaredMemberProperties.filterIsInstance<KMutableProperty<*>>())
            for (annotation in property.annotations.filterIsInstance<Preference>())
                if (addPreference(row, property, annotation))
                    row++

        content = HBox(VBox(Text("Changes will be saved automatically"), contentBox))
    }

    private fun addPreference(row: Int, property: KMutableProperty<*>, annotation: Preference): Boolean {
        when (property.returnType) {
            typeOf<String>(), typeOf<String?>() -> {
                val label = Label(annotation.description)
                label.tooltip = Tooltip(annotation.explanation)
                val field = TextField(property.getter.call(App.instance.settings) as String?).apply {
                    this.textProperty().addListener { observable, oldValue, newValue ->
                        if (text == null)
                            return@addListener

                        property.setter.call(App.instance.settings, newValue)
                    }
                }
                contentBox.add(label, 0, row)
                contentBox.add(field, 1, row)

                if (annotation.picker != Picker.None)
                    addPicker(row, annotation, property, field)
            }
            else -> {
                println(property.returnType)
                return false
            }
        }

        return true
    }

    private fun addPicker( row: Int, annotation: Preference, property: KMutableProperty<*>, field: TextField) {
        val chooserButton = Button("Choose path")
        chooserButton.setOnAction {
            when (annotation.picker) {
                Picker.Directory -> {
                    DirectoryChooser().apply {
                        title = "Choose path"
                        (property.getter.call(App.instance.settings) as String?)?.let { initialDirectory = File(it) }

                        showDialog(parentStage)?.let {
                            field.text = it.absolutePath
                        }
                    }
                }
                Picker.FileOpen -> {
                    FileChooser().apply {
                        title = "Choose path"
                        (property.getter.call(App.instance.settings) as String?)?.let { initialDirectory = File(it) }

                        showOpenDialog(parentStage)?.let {
                            field.text = it.absolutePath
                        }
                    }
                }
                Picker.FileSave -> {
                    FileChooser().apply {
                        title = "Choose path"
                        (property.getter.call(App.instance.settings) as String?)?.let { initialDirectory = File(it) }

                        showSaveDialog(parentStage)?.let {
                            field.text = it.absolutePath
                        }
                    }
                }
                else -> {}
            }
        }
        contentBox.add(chooserButton, 2, row)
    }
}