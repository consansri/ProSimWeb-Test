package me.c3.ui.components.controls.buttons

import me.c3.ui.components.styled.IconButton
import me.c3.ui.resources.UIManager
import javax.swing.JFrame
import javax.swing.SwingUtilities

class ThemeSwitch(uiManager: UIManager, mainFrame: JFrame) : IconButton(uiManager) {

    private var currentIndex = 0

    init {
        setTheme(uiManager, mainFrame)

        addActionListener {
            if (currentIndex < uiManager.themes.size - 1) {
                currentIndex++
            } else {
                currentIndex = 0
            }

            setTheme(uiManager, mainFrame)
        }
    }

    private fun setTheme(UIManager: UIManager, mainFrame: JFrame) {
        SwingUtilities.invokeLater {
            UIManager.themes.getOrNull(currentIndex)?.let {
                UIManager.currentTheme = it
                    it.install(mainFrame)
                svgIcon = it.icon.derive(28, 28)
            }
        }
    }

}