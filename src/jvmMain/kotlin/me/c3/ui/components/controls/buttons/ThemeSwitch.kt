package me.c3.ui.components.controls.buttons

import me.c3.ui.Res
import me.c3.ui.States
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

        States.theme.addEvent(WeakReference(this)) {
            currentIndex = Res.themes.indexOf(it)
            svgIcon = it.icon
        }
    }

    private fun switchTheme() {
        if (!isDeactivated) {
            if (currentIndex < Res.themes.size - 1) {
                currentIndex++
            } else {
                currentIndex = 0
            }
        }
        setTheme()
    }

    private fun setTheme() {
        Res.themes.getOrNull(currentIndex)?.let {
            States.theme.set(it)
            svgIcon = it.icon
        }
    }

}