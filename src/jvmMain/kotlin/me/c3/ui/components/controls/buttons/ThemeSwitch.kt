package me.c3.ui.components.controls.buttons

import me.c3.uilib.UIResource
import me.c3.uilib.UIStates
import me.c3.uilib.styled.CIconButton
import me.c3.uilib.theme.core.Theme
import java.lang.ref.WeakReference

/**
 * This class represents a button used for switching between themes within the application.
 */
class ThemeSwitch() : CIconButton(UIStates.theme.get().icon, mode = Mode.PRIMARY_NORMAL) {

    private var currentIndex = 0

    init {
        setTheme()

        addActionListener {
            switchTheme()
        }

        UIStates.theme.addEvent(WeakReference(this), ::updateTheme)
    }

    private fun updateTheme(theme: Theme) {
        currentIndex = UIResource.themes.indexOf(theme)
        svgIcon = theme.icon
    }

    private fun switchTheme() {
        if (!isDeactivated) {
            if (currentIndex < UIResource.themes.size - 1) {
                currentIndex++
            } else {
                currentIndex = 0
            }
        }
        setTheme()
    }

    private fun setTheme() {
        UIResource.themes.getOrNull(currentIndex)?.let {
            UIStates.theme.set(it)
            svgIcon = it.icon
        }
    }

}