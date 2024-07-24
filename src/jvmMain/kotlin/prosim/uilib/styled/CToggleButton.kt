package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import javax.swing.JButton
import javax.swing.border.Border

open class CToggleButton(initialText: String, val toggleSwitchType: CToggleButtonUI.ToggleSwitchType, val fontType: FontType) : JButton() {

    var isDeactivated = false
        set(value) {
            field = value
            repaint()
        }

    var isActive = false
        set(value) {
            field = value
            repaint()
        }

    init {
        this.setUI(CToggleButtonUI(toggleSwitchType, fontType))
        text = initialText
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

    override fun getBorder(): Border {
        return try {
            toggleSwitchType.getBorder()
        } catch (e: NullPointerException) {
            super.getBorder()
        }
    }

    override fun getBackground(): Color {
        return if (isActive) UIStates.theme.get().COLOR_ICON_BG_ACTIVE else UIStates.theme.get().COLOR_ICON_BG
    }

    override fun getForeground(): Color {
        return if (isDeactivated) UIStates.theme.get().COLOR_FG_1 else UIStates.theme.get().COLOR_FG_0
    }

}