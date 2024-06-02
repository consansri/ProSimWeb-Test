package me.c3.ui.styled

import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JScrollPane

open class CScrollPane( val primary: Boolean, c: Component?) : JScrollPane(c) {

    constructor( primary: Boolean) : this( primary, null)

    constructor( primary: Boolean, component: Component, vsb: Int, hsb: Int) : this( primary, component) {
        this.verticalScrollBarPolicy = vsb
        this.horizontalScrollBarPolicy = hsb
    }

    init {
        this.setUI(CScrollPaneUI())
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D ?: return
        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)

        g2d.dispose()
    }

}