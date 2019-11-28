package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.beans.value.ChangeListener
import javafx.geometry.Insets
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.GridPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.FileChooser
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.App
import java.io.File
import kotlin.reflect.full.createType
import kotlin.reflect.full.isSubtypeOf
import kotlin.reflect.full.primaryConstructor

class StartRecordingPopup(val parentStage: Stage): Stage() {
    init {
        initOwner(parentStage)

        val startRecordingButton = Button("Start recording")

        val recorderConfiguration = GridPane().apply {
            padding = Insets(25.0)
            hgap = 10.0
            vgap = 10.0
        }

        val recorderSelection = ComboBox<String>().apply {
            for (supportedRecorder in App.instance.settings.supportedRecorders) {
                items.add(supportedRecorder.simpleName)

                valueProperty().addListener(ChangeListener { observable, oldValue, newValue ->
                    if (!newValue.equals(supportedRecorder.simpleName))
                        return@ChangeListener

                    recorderConfiguration.children.clear()
                    var row = 0


                    val parameters = supportedRecorder.primaryConstructor!!.parameters
                    val parameterValues: Array<() -> Any> = Array(parameters.size) { { } }
                    for ((i, param) in parameters.withIndex()) {
                        val input: Node = when {
                            param.type.isSubtypeOf(String::class.createType()) -> {
                                TextField().apply {
                                    parameterValues[i] = { text }
                                }
                            }
                            param.type.isSubtypeOf(Long::class.createType()) -> {
                                TextField().apply {
                                    parameterValues[i] = { text.toLong() }
                                }
                            }
                            param.type.isSubtypeOf(Double::class.createType()) -> {
                                TextField().apply {
                                    parameterValues[i] = { text.toDouble() }
                                }
                            }
                            param.type.isSubtypeOf(Int::class.createType()) -> {
                                TextField().apply {
                                    parameterValues[i] = { text.toInt() }
                                }
                            }
                            param.type.isSubtypeOf(Boolean::class.createType()) -> {
                                CheckBox().apply {
                                    parameterValues[i] = { isSelected }
                                }
                            }
                            param.type.isSubtypeOf(File::class.createType()) -> {
                                HBox(10.0).apply {
                                    val fileLabel = Label()
                                    val fileButton = Button("Choose file").apply {
                                        setOnAction {
                                            val fileChooser = FileChooser()
                                            App.instance.settings.recordingDirectory?.let {
                                                recordingDir ->
                                                    fileChooser.initialDirectory = File(recordingDir)
                                            }

                                            fileChooser.showOpenDialog(this@StartRecordingPopup)?.absolutePath?.let {
                                                fileLabel.text = it
                                            }
                                        }
                                        parameterValues[i] = { File(fileLabel.text) }
                                    }
                                    children.addAll(fileLabel, fileButton)
                                }
                            }
                            else -> TODO("Don't know how to handle this type")
                        }
                        val label = Label(param.name?.capitalize())
                        recorderConfiguration.add(label, 0, row)
                        recorderConfiguration.add(input, 1, row)
                        row++
                    }
                    startRecordingButton.setOnAction {
                        val constructorParams = parameterValues.map { it() }.toTypedArray()
                        val recorder = supportedRecorder.primaryConstructor!!.call(*constructorParams)
                        App.instance.wrangler.addRecorder(recorder)
                        // TODO: Show all active recorders
                        close()
                    }
                    sizeToScene()
                })
            }
        }

        val recorderBox = VBox().apply {
            spacing = 10.0
            padding = Insets(10.0)
            children.addAll(Text("Recorder options"), recorderConfiguration)
        }

        val vBox = VBox(10.0, recorderSelection, recorderBox, startRecordingButton).apply {
            padding = Insets(25.0)
        }

        scene = Scene(vBox)
        title = "Start recording"

        sizeToScene()
        show()
    }
}
