package prosim.uilib.styled.borders

import prosim.uilib.UIStates
import java.awt.Color
import javax.swing.border.LineBorder

open class CLineBorder : LineBorder(UIStates.theme.get().COLOR_BORDER) {

    open var customColor: Color? = null
        set(value) {
            field = value
            lineColor = value ?: UIStates.theme.get().COLOR_BORDER
        }

    open var customThickness: Int? = null
        set(value) {
            field = value
            thickness = value ?: UIStates.scale.get().SIZE_BORDER_THICKNESS
        }

    init {
        roundedCorners = true
    }
}