package me.c3.ui.components.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.UIManager
import me.c3.ui.components.editor.EditPanel
import me.c3.ui.theme.core.components.CTabbedPaneUI
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import java.io.File
import javax.swing.Icon
import javax.swing.JTabbedPane
import javax.swing.JTextPane
import javax.swing.SwingUtilities

open class CTabbedPane(uiManager: UIManager, private val primary: Boolean) : JTabbedPane(), UIAdapter {

    var selectedColor = uiManager.currTheme().globalStyle.borderColor

    init {
        setupUI(uiManager)
    }

    fun addTextFileTab(uiManager: UIManager, file: File) {
        if (!file.exists()) {
            file.createNewFile()
        }

        addTab(null, EditPanel(uiManager, file.name))
        val lastIndex = tabCount - 1
        setTabComponentAt(lastIndex, CTextFileTab(uiManager, this, file.path))
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
        background = if (primary) uiManager.currTheme().globalStyle.bgPrimary else uiManager.currTheme().globalStyle.bgSecondary
        foreground = uiManager.currTheme().textStyle.base
        selectedColor = uiManager.currTheme().globalStyle.borderColor
        font = uiManager.currTheme().textStyle.font.deriveFont(uiManager.currScale().fontScale.textSize)
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