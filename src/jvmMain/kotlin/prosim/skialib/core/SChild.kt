package prosim.skialib.core

import emulator.kit.nativeLog
import org.jetbrains.skia.Canvas
import prosim.skialib.core.border.Insets
import prosim.skialib.core.layouts.Attribute
import prosim.skialib.core.layouts.Layout

interface SChild {
    val children: MutableMap<Attribute, SChild>
    val preferredWidth: Int
    val preferredHeight: Int
    val sWidth: Int
    val sHeight: Int
    var layout: Layout
    var insets: Insets

    fun addComponent(child: SChild, attr: Attribute){
        children[attr] = child
        layout.addComponent(child, attr)
    }

    fun removeComponent(child: SChild){
        children.values.remove(child)
        layout.components.values.remove(child)
    }

    fun render(canvas: Canvas){
        nativeLog("Render Child ${this::class.simpleName}")
        customRender(canvas)
        renderChildren(canvas)
    }

    fun customRender(canvas: Canvas)

    fun renderChildren(canvas: Canvas){
        layout.renderContent(canvas)
    }
}