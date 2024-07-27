package prosim.uilib.styled.params

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import prosim.uilib.scale.core.Scaling
import prosim.uilib.theme.core.Theme
import java.awt.Color
import javax.swing.border.Border

enum class IconSize {
    PRIMARY_NORMAL,
    SECONDARY_NORMAL,
    PRIMARY_SMALL,
    SECONDARY_SMALL,
    GRADIENT_NORMAL,
    GRADIENT_SMALL;

    fun applyFilter(icon: FlatSVGIcon, theme: Theme) {
        icon.colorFilter = FlatSVGIcon.ColorFilter {
            if (it == Color.black) {
                when (this) {
                    PRIMARY_NORMAL, PRIMARY_SMALL -> theme.COLOR_ICON_FG_0
                    SECONDARY_NORMAL, SECONDARY_SMALL -> theme.COLOR_ICON_FG_1
                    GRADIENT_NORMAL, GRADIENT_SMALL -> {
                        it
                    }
                }
            } else {
                it
            }
        }
    }

    fun size(scale: Scaling): Int = when (this) {
        PRIMARY_NORMAL, SECONDARY_NORMAL, GRADIENT_NORMAL -> scale.SIZE_CONTROL_MEDIUM
        PRIMARY_SMALL, SECONDARY_SMALL, GRADIENT_SMALL -> scale.SIZE_CONTROL_SMALL
    }

    fun getBorder(): Border = when (this) {
        PRIMARY_NORMAL -> UIStates.scale.get().BORDER_INSET_MEDIUM
        SECONDARY_NORMAL -> UIStates.scale.get().BORDER_INSET_MEDIUM
        PRIMARY_SMALL -> UIStates.scale.get().BORDER_INSET_SMALL
        SECONDARY_SMALL -> UIStates.scale.get().BORDER_INSET_SMALL
        GRADIENT_NORMAL -> UIStates.scale.get().BORDER_INSET_MEDIUM
        GRADIENT_SMALL -> UIStates.scale.get().BORDER_INSET_SMALL
    }

    fun getInset(): Int = when (this) {
        PRIMARY_NORMAL, GRADIENT_NORMAL -> UIStates.scale.get().SIZE_INSET_MEDIUM
        SECONDARY_NORMAL -> UIStates.scale.get().SIZE_INSET_MEDIUM
        PRIMARY_SMALL, GRADIENT_SMALL -> UIStates.scale.get().SIZE_CONTROL_SMALL
        SECONDARY_SMALL -> UIStates.scale.get().SIZE_CONTROL_SMALL
    }
}