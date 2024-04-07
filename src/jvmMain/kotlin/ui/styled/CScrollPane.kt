package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.styled.CScrollPaneUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JScrollPane
import javax.swing.SwingUtilities

class CScrollPane(uiManager: UIManager, private val primary: Boolean) : JScrollPane(), UIAdapter {

    constructor(uiManager: UIManager, primary: Boolean, component: Component) : this(uiManager, primary) {
        this.setViewportView(component)
    }

    constructor(uiManager: UIManager, primary: Boolean, component: Component, vsb: Int, hsb: Int) : this(uiManager, primary, component) {
        this.verticalScrollBarPolicy = vsb
        this.horizontalScrollBarPolicy = hsb
    }

    init {
        setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            setUI(CScrollPaneUI())

            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    private fun setDefaults(uiManager: UIManager) {
        background = if (primary) uiManager.currTheme().globalLaF.bgPrimary else uiManager.currTheme().globalLaF.bgSecondary
        val paneUI = ui as? CScrollPaneUI ?: return
        paneUI.scrollBarBgColor = if (primary) uiManager.currTheme().globalLaF.bgPrimary else uiManager.currTheme().globalLaF.bgSecondary
        paneUI.scrollBarFgColor = uiManager.currTheme().globalLaF.borderColor
        repaint()
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D ?: return
        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)

        g2d.dispose()
    }


}