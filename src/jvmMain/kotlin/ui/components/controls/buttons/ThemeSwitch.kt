package me.c3.ui.components.controls.buttons

import me.c3.ui.components.styled.IconButton
import me.c3.ui.UIManager
import javax.swing.JFrame
import javax.swing.SwingUtilities

class ThemeSwitch(uiManager: UIManager, mainFrame: JFrame) : IconButton(uiManager, mode = Mode.PRIMARY) {

    private var currentIndex = 0

    init {
        setTheme(uiManager, mainFrame)

        addActionListener {
            if (!isDeactivated) {
                if (currentIndex < uiManager.themeManager.themes.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }

                setTheme(uiManager, mainFrame)
            }
        }
    }

    private fun setTheme(UIManager: UIManager, mainFrame: JFrame) {
        SwingUtilities.invokeLater {
            UIManager.themeManager.themes.getOrNull(currentIndex)?.let {
                UIManager.themeManager.currentTheme = it
                svgIcon = it.icon.derive(28, 28)
            }
        }
    }

}