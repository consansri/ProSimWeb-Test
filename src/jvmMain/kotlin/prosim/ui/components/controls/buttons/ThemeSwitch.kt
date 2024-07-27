package prosim.ui.components.controls.buttons

import prosim.uilib.UIResource
import prosim.uilib.UIStates
import prosim.uilib.state.StateListener
import prosim.uilib.styled.CIconButton
import prosim.uilib.styled.params.IconSize
import prosim.uilib.theme.core.Theme

/**
 * This class represents a button used for switching between themes within the application.
 */
class ThemeSwitch() : CIconButton(UIStates.theme.get().icon, iconSize = IconSize.PRIMARY_NORMAL), StateListener<Theme> {

    private var currentIndex = 0

    init {
        setTheme()

        addActionListener {
            switchTheme()
        }

        UIStates.theme.addEvent(this)
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

    override suspend fun onStateChange(newVal: Theme) {
        currentIndex = UIResource.themes.indexOf(newVal)
        svgIcon = newVal.icon
    }

}