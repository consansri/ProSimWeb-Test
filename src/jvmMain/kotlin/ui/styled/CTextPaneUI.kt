package me.c3.ui.styled

import com.formdev.flatlaf.ui.FlatTextPaneUI
import emulator.kit.assembly.Compiler
import me.c3.emulator.kit.install
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CTextPane
import java.awt.Font
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextPaneUI

class CTextPaneUI(private val uiManager: UIManager) : FlatTextPaneUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)
        val pane = c as? CTextPane ?: return

        pane.font = uiManager.currTheme().codeLaF.getFont().deriveFont(uiManager.currScale().fontScale.codeSize)

        uiManager.themeManager.addThemeChangeListener {
            setDefaults(pane)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(pane)
        }

        // Apply Defaults
        setDefaults(pane)
    }

    private fun setDefaults(tp: CTextPane){
        tp.border = BorderFactory.createEmptyBorder(0, uiManager.currScale().borderScale.insets, 0, uiManager.currScale().borderScale.insets)
        tp.background = uiManager.currTheme().globalLaF.bgPrimary
        tp.caretColor = uiManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.BASE0)
        tp.foreground = uiManager.currTheme().codeLaF.getColor(Compiler.CodeStyle.BASE0)
        uiManager.currTheme().codeLaF.getFont().install(tp, uiManager.scaleManager.currentScaling.fontScale.codeSize)
    }

}