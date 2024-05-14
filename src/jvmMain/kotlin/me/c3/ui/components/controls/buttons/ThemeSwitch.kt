package me.c3.ui.components.controls.buttons

import me.c3.ui.styled.CIconButton
import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager

class ThemeSwitch(themeManager: ThemeManager, scaleManager: ScaleManager) : CIconButton(themeManager, scaleManager, mode = Mode.PRIMARY_NORMAL) {

    private var currentIndex = 0

    init {
        setTheme(themeManager)

        addActionListener {
            if (!isDeactivated) {
                if (currentIndex < themeManager.themes.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }

                setTheme(themeManager)
            }
        }
    }

    private fun setTheme(themeManager: ThemeManager) {
        themeManager.themes.getOrNull(currentIndex)?.let {
            themeManager.curr = it
            svgIcon = it.icon
        }
    }

}