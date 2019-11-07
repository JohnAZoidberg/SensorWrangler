package me.danielschaefer.sensorwrangler.javafx.dialogs

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.scene.layout.GridPane
import javafx.scene.text.Text
import javafx.stage.Modality
import javafx.stage.Stage
import me.danielschaefer.sensorwrangler.javafx.getMeasurements

class AddChartPopup(val parentStage: Stage): Stage() {
    init {
        initModality(Modality.APPLICATION_MODAL)
        initOwner(parentStage)

        val formGrid = GridPane().apply {
            padding = Insets(25.0)
            hgap = 10.0
            vgap = 10.0

            val typeDropdown = ComboBox<String>().apply{
                items.add("LineChart")
                items.add("TODO: Scatterplot")
            }

            val xAxisMeasurement = ComboBox<String>().apply{
                items.setAll(getMeasurements())
            }

            val yAxisMeasurement = ComboBox<String>().apply{
                items.setAll(getMeasurements())
            }

            add(Text("Chart Type"), 0, 0)
            add(typeDropdown, 1, 0)
            add(Text("Label"), 1, 1)
            add(Text("Measurement"), 2, 1)
            add(Text("X-Axis"), 0, 2)
            //add(xAxisMeasurement, 2, 2)
            add(Text("Time"), 2, 2)
            add(Text("Y-Axis"), 0, 3)
            add(yAxisMeasurement, 2, 3)
            add(Button("Add"), 0, 4)
        }

        scene = Scene(formGrid)
        title = "Add LineChart"

        // Which Measurement on which axis
        // Axis labels


        sizeToScene()
        show()
    }
}