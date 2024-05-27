package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.CScrollPaneUI
import me.c3.ui.theme.ThemeManager
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JScrollPane

open class CScrollPane(tm: ThemeManager, sm: ScaleManager, val primary: Boolean, c: Component?) : JScrollPane(c) {

    constructor(tm: ThemeManager, sm: ScaleManager, primary: Boolean) : this(tm, sm, primary, null)

    constructor(tm: ThemeManager, sm: ScaleManager, primary: Boolean, component: Component, vsb: Int, hsb: Int) : this(tm, sm, primary, component) {
        this.verticalScrollBarPolicy = vsb
        this.horizontalScrollBarPolicy = hsb
    }

    init {
        this.setUI(CScrollPaneUI(tm, sm))
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D ?: return
        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)

        g2d.dispose()
    }

}