package me.c3.ui.styled

import me.c3.ui.UIManager
import java.awt.Color
import javax.swing.BorderFactory
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicTextFieldUI

class CTextFieldUI(private val uiManager: UIManager, val type: Type): BasicTextFieldUI() {

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val tf = c as? CTextField ?: return
        tf.border = BorderFactory.createEmptyBorder()

        uiManager.themeManager.addThemeChangeListener {
            setDefaults(tf)
        }

        uiManager.scaleManager.addScaleChangeEvent {
            setDefaults(tf)
        }

        setDefaults(tf)
    }

    private fun setDefaults(tf: CTextField){
        tf.font = when(type){
            Type.DATA -> uiManager.currTheme().codeLaF.getFont().deriveFont(uiManager.currScale().fontScale.dataSize)
            Type.CODE -> uiManager.currTheme().codeLaF.getFont().deriveFont(uiManager.currScale().fontScale.codeSize)
            Type.TEXT -> uiManager.currTheme().textLaF.getBaseFont().deriveFont(uiManager.currScale().fontScale.textSize)
        }
        tf.background = uiManager.currTheme().globalLaF.bgPrimary
        tf.foreground = uiManager.currTheme().textLaF.base
        tf.caretColor = uiManager.currTheme().textLaF.base
    }

    enum class Type{
        DATA,
        CODE,
        TEXT
    }

}