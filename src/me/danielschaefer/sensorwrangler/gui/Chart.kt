package me.danielschaefer.sensorwrangler.gui

import javafx.beans.property.ReadOnlyBooleanProperty
import javafx.beans.property.ReadOnlyBooleanWrapper
import javafx.beans.property.SimpleIntegerProperty

abstract class Chart {
    abstract val title: String

    val shown: ReadOnlyBooleanProperty
        get() = shownProperty.readOnlyProperty
    private val shownProperty: ReadOnlyBooleanWrapper = ReadOnlyBooleanWrapper(false)

    // How many instances of this chart are shown
    private var shownCount: SimpleIntegerProperty = SimpleIntegerProperty(0).apply {
        addListener { _, oldCount, newCount ->
            if (oldCount !is Int || newCount !is Int)
                return@addListener

            if (oldCount == 0 && newCount > 0)
                shownProperty.value = true

            if (oldCount > 0 && newCount == 0)
                shownProperty.value = false
        }
    }

    fun showOne() {
        shownCount.value++
    }

    fun hideOne() {
        shownCount.value++
    }

    fun hideAll() {
        shownCount.value = 0
    }

    override fun toString(): String {
        return title
    }
}
