package me.c3.ui.components.styled

import me.c3.ui.UIManager
import java.awt.Color
import java.awt.Cursor
import javax.swing.BorderFactory
import javax.swing.JButton

class TextButton(uiManager: UIManager, text: String) : JButton(text) {

    init {
        // apply listeners
        uiManager.themeManager.addThemeChangeListener {
            val currScale = uiManager.currScale()
            font = it.textStyle.titleFont.deriveFont(currScale.fontScale.titleSize)
            foreground = it.textStyle.base
        }
        uiManager.scaleManager.addScaleChangeEvent {
            val currTheme = uiManager.currTheme()
            font = currTheme.textStyle.titleFont.deriveFont(it.fontScale.titleSize)
            foreground = currTheme.textStyle.base
        }

        // set defaults
        val currTheme = uiManager.currTheme()
        val currScale = uiManager.currScale()
        font = currTheme.textStyle.titleFont.deriveFont(currScale.fontScale.titleSize)
        foreground = currTheme.textStyle.base
        background = Color(0, 0, 0, 0)
        cursor = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR)
        border = BorderFactory.createEmptyBorder()
    }


}