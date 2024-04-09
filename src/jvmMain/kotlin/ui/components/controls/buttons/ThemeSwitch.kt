package me.c3.ui.components.controls.buttons

import me.c3.ui.components.styled.CIconButton
import me.c3.ui.UIManager
import javax.swing.JFrame
import javax.swing.SwingUtilities

class ThemeSwitch(uiManager: UIManager) : CIconButton(uiManager, mode = Mode.PRIMARY_NORMAL) {

    private var currentIndex = 0

    init {
        setTheme(uiManager)

        addActionListener {
            if (!isDeactivated) {
                if (currentIndex < uiManager.themeManager.themes.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }

                setTheme(uiManager)
            }
        }
    }

    private fun setTheme(uiManager: UIManager) {
        uiManager.themeManager.themes.getOrNull(currentIndex)?.let {
            uiManager.themeManager.currentTheme = it
            svgIcon = it.icon
        }
    }

}