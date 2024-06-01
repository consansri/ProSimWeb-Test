package me.c3.ui.components.controls.buttons

import me.c3.ui.styled.CIconButton
import me.c3.ui.manager.ScaleManager
import me.c3.ui.manager.ThemeManager

/**
 * This class represents a button used for switching between themes within the application.
 */
class ThemeSwitch() : CIconButton( mode = Mode.PRIMARY_NORMAL) {

    private var currentIndex = 0

    init {
        setTheme()

        addActionListener {
            if (!isDeactivated) {
                if (currentIndex < ThemeManager.themes.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }

                setTheme()
            }
        }
    }

    private fun setTheme() {
        ThemeManager.themes.getOrNull(currentIndex)?.let {
            ThemeManager.curr = it
            svgIcon = it.icon
        }
    }

}