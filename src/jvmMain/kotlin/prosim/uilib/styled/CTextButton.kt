package prosim.uilib.styled

import prosim.uilib.styled.params.BorderMode
import prosim.uilib.styled.params.FontType
import java.awt.Dimension
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

open class CTextButton(text: String, fontType: FontType = FontType.BASIC, val primary: Boolean = true, hoverEffect: Boolean = true, borderMode: BorderMode = BorderMode.INSET) : JComponent() {

    var text: String = text
        set(value) {
            field = value
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
        this.setUI(CTextButtonUI(fontType, borderMode))
        if(hoverEffect) installHoverEffect()
    }

    fun addActionListener(event: () -> Unit){
        addMouseListener(object : MouseAdapter(){
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

}