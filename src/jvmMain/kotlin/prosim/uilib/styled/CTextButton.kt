package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JComponent

open class CTextButton(text: String, fontType: FontType, val primary: Boolean = true, hoverEffect: Boolean = true) : JComponent() {

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
        this.setUI(CTextButtonUI(fontType))
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
}