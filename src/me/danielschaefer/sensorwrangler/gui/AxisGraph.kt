package me.danielschaefer.sensorwrangler.gui

import me.danielschaefer.sensorwrangler.data.Chart
import me.danielschaefer.sensorwrangler.data.Measurement

abstract class AxisGraph : Chart() {
    abstract var yAxisLabel: String
    abstract var yAxes: List<Measurement>

    /**
     * How many millisencds of data the graph can show at once.
     *
     * After that, old data slides out of the window and cannot be seen.
     */
    abstract var windowSize: Int

    /**
     * The lowest value to display on the x-axis
     */
    abstract var lowerBound: Double

    /**
     * The highest value to display on the x-axis
     */
    abstract var upperBound: Double

    /**
     * How far the ticks marks on the chart are apart from each other
     */
    abstract var tickSpacing: Double
}
