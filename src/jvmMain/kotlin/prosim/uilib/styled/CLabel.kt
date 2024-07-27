package prosim.uilib.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.params.IconSize
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import javax.swing.Icon
import javax.swing.JLabel
import javax.swing.SwingConstants
import javax.swing.border.Border

open class CLabel(content: String, val fontType: FontType = FontType.BASIC, val borderMode: BorderMode = BorderMode.MEDIUM, open var svgIcon: FlatSVGIcon? = null, val iconSize: IconSize = IconSize.PRIMARY_NORMAL) : JLabel(content) {

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