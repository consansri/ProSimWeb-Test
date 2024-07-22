package prosim.skialib.core.layouts

import org.jetbrains.skia.Canvas
import prosim.skialib.core.SChild

sealed class Layout {
    abstract val width: Int
    abstract val height: Int

    val components: MutableMap<Attribute, SChild> = mutableMapOf()
    abstract fun renderContent(canvas: Canvas)
    fun addComponent(child: SChild, attribute: Attribute) {
        components[attribute] = child
    }

    class AttributeException(val layout: Layout, messages: String) : Exception("${layout::class.simpleName}: $messages")
}