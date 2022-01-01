package me.danielschaefer.sensorwrangler.javafx.popups

import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.control.Label
import javafx.scene.layout.GridPane
import javafx.scene.layout.VBox
import javafx.scene.text.Text
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.Measurement
import me.danielschaefer.sensorwrangler.javafx.App
import me.danielschaefer.sensorwrangler.sensors.Averager
import me.danielschaefer.sensorwrangler.sensors.Sensor

class AddAveragePopup(parentStage: Stage) : Stage() {
    private val measurements: MutableList<ComboBox<Measurement>> = mutableListOf()
    private val sensors: MutableList<ComboBox<Sensor>> = mutableListOf()
    private val units: MutableList<Text> = mutableListOf()
    private val formGrid: GridPane

    private fun addMeasurement() {
        val newAxisIndex = sensors.size

        sensors.add(
            ComboBox<Sensor>().apply {
                // TODO: Do we want to be able to average averages?
                items.setAll(App.instance.wrangler.sensors.filterIsInstance<Sensor>())
                valueProperty().addListener(
                    ChangeListener { _, _, selectedSensor ->
                        if (selectedSensor == null)
                            return@ChangeListener

                        measurements[newAxisIndex].items.setAll(selectedSensor.measurements)
                        units[newAxisIndex].text = ""
                        sizeToScene()
                    }
                )
            }
        )
        measurements.add(
            ComboBox<Measurement>().apply {
                valueProperty().addListener(
                    ChangeListener { _, _, selectedMeasurement ->
                        if (selectedMeasurement == null)
                            return@ChangeListener

                        units[newAxisIndex].text = selectedMeasurement.unit.toString()
                        sizeToScene()
                    }
                )
            }
        )
        units.add(Text())

        formGrid.add(Label("Measurement ${newAxisIndex + 1}"), 0, newAxisIndex + 1)
        formGrid.add(sensors[newAxisIndex], 1, newAxisIndex + 1)
        formGrid.add(measurements[newAxisIndex], 2, newAxisIndex + 1)
        formGrid.add(units[newAxisIndex], 3, newAxisIndex + 1)
        sizeToScene()
    }

    init {
        initOwner(parentStage)

        formGrid = GridPane().apply {
            padding = Insets(25.0)
            hgap = 10.0
            vgap = 10.0

            val addMeasurementButton = Button("Add measurement").apply {
                setOnAction { addMeasurement() }
            }
            add(addMeasurementButton, 2, 0)
        }

        // To average we need at least two measurements
        addMeasurement()
        addMeasurement()

        val addButton = Button("Add average").apply {
            onAction = EventHandler {
                val selectedMeasurements = measurements.map { it.value }.filterNotNull()

                if (selectedMeasurements.isEmpty()) {
                    Alert(parentStage, "Invalid average", "No measurements selected")
                    return@EventHandler
                }

                if (!selectedMeasurements.all { it.unit == selectedMeasurements.first().unit }) {
                    Alert(parentStage, "Invalid average", "Only measurements with the same unit can be averaged.")
                    return@EventHandler
                }

                if (selectedMeasurements.distinct().size != selectedMeasurements.size) {
                    Alert(parentStage, "Invalid average", "Cannot average a measurement with itself.")
                    return@EventHandler
                }

                App.instance.wrangler.sensors.add(
                    Averager().apply {
                        sourceMeasurements = selectedMeasurements
                        connect()
                    }
                )
                close()
            }
        }

        val explanationLabel = Text("Average each of the inputs over the last second.\n" + "Measurements of disconnected sensors are ignored.")
        val contentBox = VBox(10.0, explanationLabel, formGrid, addButton).apply {
            padding = Insets(25.0)
            alignment = Pos.CENTER
        }
        scene = Scene(contentBox)
        title = "Add Average"

        sizeToScene()
        show()
    }
}
