package prosim.skialib.core

import prosim.skialib.core.border.Insets
import prosim.skialib.core.layouts.Attribute
import prosim.skialib.core.layouts.BorderLayout
import prosim.skialib.core.layouts.Layout

abstract class SComp: SChild {
    override val children: MutableMap<Attribute, SChild> = mutableMapOf()
    override var layout: Layout = BorderLayout()

    override val sWidth: Int get() = layout.width
    override val sHeight: Int get() = layout.height
    override var insets: Insets = Insets()
}