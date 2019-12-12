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

class AddAveragePopup(parentStage: Stage) : Stage() {
    private val measurements: MutableList<ComboBox<String>> = mutableListOf()
    private val sensors: MutableList<ComboBox<String>> = mutableListOf()
    private val units: MutableList<Text> = mutableListOf()
    private val formGrid: GridPane

    private fun addMeasurement() {
        val newAxisIndex = sensors.size

        sensors.add(ComboBox<String>().apply {
            items.setAll(App.instance.wrangler.sensors.filter { it !is Averager }.map { it.title })
            valueProperty().addListener(ChangeListener { _, _, newValue ->
                if (newValue == null)
                    return@ChangeListener

                val sensor = App.instance.wrangler.findSensorByTitle(newValue)
                if (sensor == null)
                    return@ChangeListener

                measurements[newAxisIndex].items.setAll(sensor.measurements.map { it.description })
                units[newAxisIndex].text = ""
                sizeToScene()
            })
        })
        measurements.add(ComboBox<String>().apply {
            valueProperty().addListener(ChangeListener { _,  _, newValue ->
                if (newValue == null)
                    return@ChangeListener

                val sensor = App.instance.wrangler.findVirtualSensorByTitle(sensors[newAxisIndex].value)
                if (sensor == null)
                    return@ChangeListener

                units[newAxisIndex].text = sensor.measurements.first { it.description == newValue }.unit.toString()
                sizeToScene()
            })
        })
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
                val selectedMeasurements: MutableList<Measurement> = mutableListOf()
                for (i in 0 until sensors.size) {
                    val selectedSensor = App.instance.wrangler.findSensorByTitle(sensors[i].value)
                    selectedSensor?.measurements?.filter { it.description == measurements[i].value }?.let {
                        selectedMeasurements.add(it[0])
                    }
                }

                if (selectedMeasurements.isEmpty())
                    return@EventHandler



                App.instance.wrangler.sensors.add(Averager().apply {
                    sourceMeasurements = selectedMeasurements
                    connect()
                })
                close()
            }
        }

        val explanationLabel = Text("Average each of the inputs over the last second.\n" + "Measurements of disconnected sensors are ignored.")
        val contentBox = VBox(10.0, explanationLabel, formGrid, addButton).apply {
            padding = Insets(25.0)
            alignment = Pos.CENTER;
        }
        scene = Scene(contentBox)
        title = "Add Average"

        sizeToScene()
        show()
    }
}
