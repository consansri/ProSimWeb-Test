package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import com.formdev.flatlaf.extras.FlatSVGIcon.ColorFilter
import prosim.uilib.UIStates
import prosim.uilib.styled.params.IconSize
import java.awt.Color
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.border.Border

class CIcon(val svgIcon: FlatSVGIcon, val iconSize: IconSize = IconSize.PRIMARY_NORMAL) : JLabel() {

    init {
        setDefaults()
    }

    private fun setDefaults() {
        isOpaque = false
        background = Color(0, 0, 0, 0)
    }

    override fun getBorder(): Border {
        return when (iconSize) {
            IconSize.PRIMARY_NORMAL, IconSize.SECONDARY_NORMAL, IconSize.GRADIENT_NORMAL -> UIStates.scale.get().BORDER_INSET_MEDIUM
            IconSize.GRADIENT_SMALL, IconSize.SECONDARY_SMALL, IconSize.PRIMARY_SMALL -> UIStates.scale.get().BORDER_INSET_SMALL
        }
    }

    override fun getIcon(): Icon {
        svgIcon.colorFilter = ColorFilter {
            if (it == Color.black) {
                when (iconSize) {
                    IconSize.PRIMARY_NORMAL, IconSize.PRIMARY_SMALL -> UIStates.theme.get().COLOR_ICON_FG_0
                    IconSize.SECONDARY_NORMAL, IconSize.SECONDARY_SMALL -> UIStates.theme.get().COLOR_ICON_FG_1
                    IconSize.GRADIENT_NORMAL, IconSize.GRADIENT_SMALL -> null
                }
            } else {
                it
            }
        }

        val size = when (iconSize) {
            IconSize.PRIMARY_SMALL, IconSize.SECONDARY_SMALL, IconSize.GRADIENT_SMALL -> UIStates.scale.get().SIZE_CONTROL_SMALL
            IconSize.PRIMARY_NORMAL, IconSize.SECONDARY_NORMAL, IconSize.GRADIENT_NORMAL -> UIStates.scale.get().SIZE_CONTROL_MEDIUM
        }

        return svgIcon.derive(size, size)
    }
}