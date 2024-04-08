package me.c3.ui.components.styled

import emulator.kit.assembly.Compiler
import me.c3.ui.UIManager
import me.c3.ui.styled.CTextPaneUI
import me.c3.ui.theme.core.ui.UIAdapter
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.JTextPane
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.text.StyleConstants
import javax.swing.text.StyledDocument

class CTextPane(uiManager: UIManager) : JTextPane(), UIAdapter {
    init {
        this.setupUI(uiManager)
    }

    override fun setBounds(x: Int, y: Int, width: Int, height: Int) {
        val size = this.preferredSize
        super.setBounds(x, y, size.width.coerceAtLeast(width), height)
    }

    fun createScrollPane(uiManager: UIManager): CScrollPane {
        return CScrollPane(uiManager, true, this, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS)
    }

    override fun setupUI(uiManager: UIManager) {
        setUI(CTextPaneUI(uiManager))

        uiManager.themeManager.addThemeChangeListener {
            setDefaults(uiManager)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(uiManager)
        }

        // Apply Defaults
        setDefaults(uiManager)
    }

    private fun setDefaults(uiManager: UIManager) {
        border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
        background = uiManager.currTheme().globalLaF.bgPrimary
        caretColor = uiManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.BASE0)
        foreground = uiManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.BASE0)
        font = uiManager.currTheme().codeLaF.font.deriveFont(uiManager.scaleManager.currentScaling.fontScale.codeSize)
    }

}