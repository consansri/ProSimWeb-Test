package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import prosim.uilib.UIStates
import prosim.uilib.styled.CIconButton.Mode
import java.awt.Color
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.border.Border

class CIcon(val svgIcon: FlatSVGIcon, val mode: Mode = Mode.PRIMARY_NORMAL) : JLabel() {

    init {
        setDefaults()
    }

    private fun setDefaults() {
        isOpaque = false
        background = Color(0, 0, 0, 0)
    }

    override fun getBorder(): Border {
        return when (mode) {
            Mode.PRIMARY_NORMAL, Mode.SECONDARY_NORMAL, Mode.GRADIENT_NORMAL -> UIStates.scale.get().controlScale.getNormalInsetBorder()
            Mode.GRADIENT_SMALL, Mode.SECONDARY_SMALL, Mode.PRIMARY_SMALL -> UIStates.scale.get().controlScale.getSmallInsetBorder()
        }
    }

    override fun getIcon(): Icon {
        svgIcon.colorFilter = ColorFilter {
            when (mode) {
                Mode.PRIMARY_NORMAL, Mode.PRIMARY_SMALL -> UIStates.theme.get().iconLaF.iconFgPrimary
                Mode.SECONDARY_NORMAL, Mode.SECONDARY_SMALL -> UIStates.theme.get().iconLaF.iconFgSecondary
                Mode.GRADIENT_NORMAL, Mode.GRADIENT_SMALL -> null
            }
        }

        val size = when (mode) {
            Mode.PRIMARY_SMALL, Mode.SECONDARY_SMALL, Mode.GRADIENT_SMALL -> UIStates.scale.get().controlScale.smallSize
            Mode.PRIMARY_NORMAL, Mode.SECONDARY_NORMAL, Mode.GRADIENT_NORMAL -> UIStates.scale.get().controlScale.normalSize
        }

        return svgIcon.derive(size, size)
    }
}