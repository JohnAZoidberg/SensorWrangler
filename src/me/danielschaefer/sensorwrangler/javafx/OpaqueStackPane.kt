package me.danielschaefer.sensorwrangler.javafx

import javafx.collections.ListChangeListener
import javafx.scene.Node
import javafx.scene.layout.StackPane

/**
 * Just like {@link javafx.scene.layout StackPane} but only the top most element is ever shown
 *
 * Once a new child is pushed onto the stack, the others are hidden
 */
class OpaqueStackPane(vararg children: Node) : StackPane(*children) {
    init {
        this.children.addListener(
            ListChangeListener {
                it.next()
                it.list.forEach { child -> child.isVisible = false }
                it.list.lastOrNull()?.isVisible = true
            }
        )
    }
}
