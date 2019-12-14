package me.danielschaefer.sensorwrangler.javafx

import javafx.geometry.Insets
import javafx.scene.Scene
import javafx.scene.control.Tab
import javafx.scene.control.TabPane
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.stage.Stage


class SettingsWindow(val parentStage: Stage) : Stage() {
    val tabPane: TabPane
    val sensorTab: Tab
    val chartTab: Tab
    val preferencesTab: Tab

    init {
        initOwner(parentStage)

        val mainContent = VBox().apply {
            tabPane = TabPane().apply {
                sensorTab = SensorTab(this@SettingsWindow)
                chartTab = ChartTab(this@SettingsWindow)
                preferencesTab = PreferencesTab(this@SettingsWindow)

                tabs.addAll(sensorTab, chartTab, preferencesTab)

                // TODO: Do using CSS .tab-pane > .tab-content-area > * { -fx-padding: 25; }
                val insets = Insets(25.0)
                for (tab in tabs)
                    (tab.content as HBox).padding = insets

                tabClosingPolicy = TabPane.TabClosingPolicy.UNAVAILABLE
            }

            children.addAll(tabPane)
        }

        scene = Scene(mainContent)
        title = "Settings"

        sizeToScene()
        show()
    }
}
