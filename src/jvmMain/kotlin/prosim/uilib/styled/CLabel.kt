package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.Border

open class CLabel(content: String, val fontType: FontType = FontType.BASIC, val borderMode: BorderMode = BorderMode.MEDIUM, open val svgIcon: FlatSVGIcon? = null, val mode: CIconButton.Mode = CIconButton.Mode.PRIMARY_NORMAL) : JLabel(content) {


    var customFG: Color? = null
    var customBG: Color? = null

    init {
        horizontalAlignment = SwingConstants.CENTER
        isFocusable = false
    }

    fun setColouredText(text: String, color: Color) {
        this.text = text
        customFG = color
    }

    override fun getBorder(): Border {
        return borderMode.getBorder()
    }

    override fun getBackground(): Color {
        return customBG ?: Color(0, 0, 0, 0)
    }

    override fun getForeground(): Color {
        return customFG ?: UIStates.theme.get().COLOR_FG_0
    }

    override fun getInsets(): Insets {
        return border.getBorderInsets(this)
    }

    override fun getPreferredSize(): Dimension {
        val size = super.getPreferredSize()
        val insets = insets
        return Dimension(size.width + insets.left + insets.right, size.height + insets.top + insets.bottom)
    }

    override fun getIcon(): Icon? {
        svgIcon?.colorFilter = FlatSVGIcon.ColorFilter {
            when (mode) {
                CIconButton.Mode.PRIMARY_NORMAL, CIconButton.Mode.PRIMARY_SMALL -> UIStates.theme.get().COLOR_ICON_FG_0
                CIconButton.Mode.SECONDARY_NORMAL, CIconButton.Mode.SECONDARY_SMALL -> UIStates.theme.get().COLOR_ICON_FG_1
                CIconButton.Mode.GRADIENT_NORMAL, CIconButton.Mode.GRADIENT_SMALL -> null
            }
        }

        val size = when (mode) {
            CIconButton.Mode.PRIMARY_SMALL, CIconButton.Mode.SECONDARY_SMALL, CIconButton.Mode.GRADIENT_SMALL -> UIStates.scale.get().SIZE_CONTROL_SMALL
            CIconButton.Mode.PRIMARY_NORMAL, CIconButton.Mode.SECONDARY_NORMAL, CIconButton.Mode.GRADIENT_NORMAL -> UIStates.scale.get().SIZE_CONTROL_MEDIUM
        }

        return svgIcon?.derive(size, size)
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }
}