package prosim.uilib.styled.core

import prosim.uilib.styled.core.border.Insets
import prosim.uilib.styled.core.layouts.Attribute
import prosim.uilib.styled.core.layouts.BorderLayout
import prosim.uilib.styled.core.layouts.Layout

abstract class SComp: SChild {
    override val children: MutableMap<Attribute, SChild> = mutableMapOf()
    override var layout: Layout = BorderLayout()

    override val sWidth: Int get() = layout.width
    override val sHeight: Int get() = layout.height
    override var insets: Insets = Insets()
}