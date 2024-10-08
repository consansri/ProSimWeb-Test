package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.borders.CLineBorder
import prosim.uilib.styled.params.BorderMode
import java.awt.*
import javax.swing.JPanel
import javax.swing.border.Border

open class CPanel(primary: Boolean = false, borderMode: BorderMode = BorderMode.NONE, roundCorners: Boolean = false, val isOverlay: Boolean = false) : JPanel() {

    var roundedCorners: Boolean = roundCorners
        set(value) {
            field = value
            repaint()
        }
    var primary: Boolean = primary
        set(value) {
            field = value
            repaint()
        }

    var borderMode: BorderMode = borderMode
        set(value) {
            field = value
            revalidate()
            repaint()
        }

    var customBorderColor: Color? = null
        set(value) {
            field = value
            repaint()
        }

    var customBorderThickness: Int? = null
        set(value) {
            field = value
            revalidate()
            repaint()
        }

    open val customBG: Color? = null

    init {
        isOpaque = false
        border = border

        this.revalidate()
        this.repaint()
    }

    override fun paintComponent(g: Graphics?) {
        val g2d = g as? Graphics2D
        if (g2d == null) {
            super.paintComponent(g)
            return
        }

        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        g2d.color = background
        if (roundedCorners) {
            val cornerRadius = UIStates.scale.get().SIZE_CORNER_RADIUS
            g2d.fillRoundRect(0, 0, width, height, cornerRadius, cornerRadius)
        } else {
            g2d.fillRect(0, 0, width, height)
        }

        if (isOverlay) {
            g2d.color = UIStates.theme.get().COLOR_BORDER
            g2d.drawRoundRect(0, 0, width - 1, height - 1, UIStates.scale.get().SIZE_CORNER_RADIUS, UIStates.scale.get().SIZE_CORNER_RADIUS)
        }
    }

    override fun paintBorder(g: Graphics?) {
        val border = border
        if (border is CLineBorder) {
            border.customColor = customBorderColor
            border.customThickness = customBorderThickness
        }
        border.paintBorder(this, g, 0, 0, width, height)
    }

    override fun getInsets(): Insets {
        return border.getBorderInsets(this)
    }

    override fun getBorder(): Border {
        return try {
            if (isOverlay) UIStates.scale.get().BORDER_INSET_MEDIUM else borderMode.getBorder()
        } catch (e: NullPointerException) {
            BorderMode.MEDIUM.getBorder()
        }
    }

    override fun getBackground(): Color {
        val currCustomBG = customBG
        return when {
            currCustomBG != null -> currCustomBG
            isOverlay -> UIStates.theme.get().COLOR_BG_OVERLAY
            primary -> UIStates.theme.get().COLOR_BG_0
            else -> UIStates.theme.get().COLOR_BG_1
        }
    }
}