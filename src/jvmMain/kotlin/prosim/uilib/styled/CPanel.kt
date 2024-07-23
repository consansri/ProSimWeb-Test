package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.BorderMode
import java.awt.Color
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.RenderingHints
import javax.swing.JPanel
import javax.swing.border.Border

open class CPanel(primary: Boolean = false, borderMode: BorderMode = BorderMode.NONE, roundCorners: Boolean = false, val isOverlay: Boolean = false) : JPanel() {

    var roundedCorners: Boolean = roundCorners
        set(value) {
            field = value
            revalidate()
            repaint()
        }
    var primary: Boolean = primary
        set(value) {
            field = value
            revalidate()
            repaint()
        }

    var borderMode: BorderMode = borderMode
        set(value) {
            field = value
            revalidate()
            repaint()
        }

    init {
        isOpaque = false
        border = border

        //this.setUI(CPanelUI())
        this.revalidate()
        this.repaint()
    }

    override fun paintComponent(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D
        if (g2d == null) {
            super.paintComponent(g)
            return
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2d.color = if (isOverlay) UIStates.theme.get().globalLaF.bgOverlay else if (primary) UIStates.theme.get().globalLaF.bgPrimary else UIStates.theme.get().globalLaF.bgSecondary
        if (roundedCorners) {
            g2d.fillRoundRect(0, 0, width, height, UIStates.scale.get().borderScale.cornerRadius, UIStates.scale.get().borderScale.cornerRadius)
        } else {
            g2d.fillRect(0, 0, width, height)
        }

        if (isOverlay) {
            g2d.color = UIStates.theme.get().globalLaF.borderColor
            g2d.drawRoundRect(0, 0, width - 1, height - 1, UIStates.scale.get().borderScale.cornerRadius, UIStates.scale.get().borderScale.cornerRadius)
        }
    }

    override fun getBorder(): Border {
        return try {
            if (isOverlay) UIStates.scale.get().borderScale.getInsetBorder() else borderMode.getBorder()
        } catch (e: NullPointerException) {
            BorderMode.INSET.getBorder()
        }
    }

    override fun getBackground(): Color {
        return Color(0, 0, 0, 0)
    }
}