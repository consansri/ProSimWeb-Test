package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Dimension
import java.awt.Font
import java.awt.Insets
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.border.Border

open class CTextButton(text: String, val fontType: FontType = FontType.BASIC, val primary: Boolean = true, hoverEffect: Boolean = true, val borderMode: BorderMode = BorderMode.INSET) : JComponent() {

    var text: String = text
        set(value) {
            field = value
            size = Dimension(getFontMetrics(font).stringWidth(value) + insets.left + insets.right, getFontMetrics(font).height + insets.top + insets.bottom)
            revalidate()
            repaint()
        }

    var isDeactivated = false
        set(value) {
            field = value
            repaint()
        }

    var isHovered = false
        set(value) {
            field = value
            repaint()
        }

    init {
        this.setUI(CTextButtonUI())
        if (hoverEffect) installHoverEffect()
    }

    fun addActionListener(event: () -> Unit) {
        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                event()
            }
        })
    }

    private fun installHoverEffect() {
        // Apply hover effect
        addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                if (!isDeactivated) {
                    isHovered = true
                }
            }

            override fun mouseExited(e: MouseEvent?) {
                if (isHovered) {
                    isHovered = false
                }
            }
        })
    }

    override fun getPreferredSize(): Dimension {
        val fm = getFontMetrics(font)
        val width = fm.stringWidth(text) + insets.left + insets.right
        val height = fm.height + insets.top + insets.bottom
        return Dimension(width, height)
    }

    override fun getBorder(): Border {
        return borderMode.getBorder()
    }

    override fun getInsets(): Insets {
        return border.getBorderInsets(this)
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

    override fun getForeground(): Color {
        return if (primary) UIStates.theme.get().textLaF.base else UIStates.theme.get().textLaF.baseSecondary
    }

}