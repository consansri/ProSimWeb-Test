package me.c3.ui.components.controls.buttons

import me.c3.ui.components.styled.CIconButton
import me.c3.ui.MainManager

class ThemeSwitch(mainManager: MainManager) : CIconButton(mainManager.themeManager, mainManager.scaleManager, mode = Mode.PRIMARY_NORMAL) {

    private var currentIndex = 0

    init {
        setTheme(mainManager)

        addActionListener {
            if (!isDeactivated) {
                if (currentIndex < mainManager.themeManager.themes.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }

                setTheme(mainManager)
            }
        }
    }

    private fun setTheme(mainManager: MainManager) {
        mainManager.themeManager.themes.getOrNull(currentIndex)?.let {
            mainManager.themeManager.curr = it
            svgIcon = it.icon
        }
    }

}