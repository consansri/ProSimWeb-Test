package me.c3.ui.components.styled

import me.c3.ui.UIManager
import me.c3.ui.styled.CTabbedPaneUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Graphics
import java.awt.Graphics2D
import java.io.File
import javax.swing.JTabbedPane
import javax.swing.SwingUtilities

open class CTabbedPane(private val uiManager: UIManager, private val primary: Boolean) : JTabbedPane(), UIAdapter {

    var selectedColor = uiManager.currTheme().globalLaF.borderColor

    init {
        setupUI(uiManager)
    }

    final override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            setUI(CTabbedPaneUI())

            uiManager.scaleManager.addScaleChangeEvent {
                setDefaults(uiManager)
            }

            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    private fun setDefaults(uiManager: UIManager) {
        background = if (primary) uiManager.currTheme().globalLaF.bgPrimary else uiManager.currTheme().globalLaF.bgSecondary
        foreground = uiManager.currTheme().textLaF.base
        selectedColor = uiManager.currTheme().globalLaF.borderColor
        font = uiManager.currTheme().textLaF.getBaseFont().deriveFont(uiManager.currScale().fontScale.textSize)
        repaint()
    }

    override fun paint(g: Graphics) {
        val g2d = g.create() as Graphics2D

        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)
        g2d.dispose()
    }


}