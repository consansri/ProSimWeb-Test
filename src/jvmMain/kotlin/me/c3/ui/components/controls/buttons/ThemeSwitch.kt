package me.c3.ui.components.controls.buttons

import me.c3.ui.styled.CIconButton
import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager

/**
 * This class represents a button used for switching between themes within the application.
 */
class ThemeSwitch(tm: ThemeManager, sm: ScaleManager) : CIconButton(tm, sm, mode = Mode.PRIMARY_NORMAL) {

    private var currentIndex = 0

    init {
        setTheme(tm)

        addActionListener {
            if (!isDeactivated) {
                if (currentIndex < tm.themes.size - 1) {
                    currentIndex++
                } else {
                    currentIndex = 0
                }

                setTheme(tm)
            }
        }
    }

    private fun setTheme(tm: ThemeManager) {
        tm.themes.getOrNull(currentIndex)?.let {
            tm.curr = it
            svgIcon = it.icon
        }
    }

}