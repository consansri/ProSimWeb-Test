package me.c3.ui.components.controls.buttons

import me.c3.uilib.UIResource
import me.c3.uilib.styled.CIconButton
import java.lang.ref.WeakReference

/**
 * This class represents a button used for switching between themes within the application.
 */
class ThemeSwitch() : CIconButton(mode = Mode.PRIMARY_NORMAL) {

    private var currentIndex = 0

    init {
        setTheme()

        addActionListener {
            switchTheme()
        }

        UIManager.theme.addEvent(WeakReference(this)) {
            currentIndex = UIResource.themes.indexOf(it)
            svgIcon = it.icon
        }
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
            UIManager.theme.set(it)
            svgIcon = it.icon
        }
    }

}