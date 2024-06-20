package me.c3.uilib.styled

import me.c3.uilib.UIStates
import me.c3.uilib.styled.params.FontType
import java.awt.*
import java.lang.ref.WeakReference
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicLabelUI

class CVerticalLabelUI( private val primary: Boolean, private val fontType: FontType, private val clockwise: Boolean = true) : BasicLabelUI() {

    private var paintIconR = Rectangle()
    private var paintTextR = Rectangle()
    private var paintViewR = Rectangle()
    private var paintViewInsets = Insets(0, 0, 0, 0)

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val cLabel = c as? CVerticalLabel ?: return

        UIStates.theme.addEvent(WeakReference(cLabel)) { _ ->
            setDefaults(cLabel)
        }

        UIStates.scale.addEvent(WeakReference(cLabel)) { _ ->
            setDefaults(cLabel)
        }

        setDefaults(cLabel)
    }

    private fun setDefaults(cLabel: CVerticalLabel) {
        cLabel.font = fontType.getFont()
        cLabel.border = UIStates.scale.get().borderScale.getInsetBorder()
        cLabel.foreground = if(primary) UIStates.theme.get().textLaF.base else UIStates.theme.get().textLaF.baseSecondary
        cLabel.repaint()
    }

    override fun getPreferredSize(c: JComponent?): Dimension {
        val dim = super.getPreferredSize(c)
        return Dimension(dim.height, dim.width)
    }

    override fun paint(g: Graphics, c: JComponent) {
        val g2d = g.create() as? Graphics2D ?: return
        val label = c as? CVerticalLabel ?: return
        val text = label.text
        val icon = if (label.isEnabled) label.icon else label.disabledIcon

        if (icon == null && text == null) return

        val fm = g2d.getFontMetrics(label.font)
        paintViewInsets = c.insets

        paintViewR.x = paintViewInsets.left
        paintViewR.y = paintViewInsets.top

        // Use inverted height and width
        paintViewR.height = c.width - (paintViewInsets.left + paintViewInsets.right)
        paintViewR.width = c.height - (paintViewInsets.top + paintViewInsets.bottom)

        paintIconR.x = 0
        paintIconR.y = 0
        paintIconR.width = 0
        paintIconR.height = 0

        paintTextR.x = 0
        paintTextR.y = 0
        paintTextR.width = 0
        paintTextR.height = 0

        g2d.font = label.font
        g2d.color = label.foreground

        val clippedText = layoutCL(label, fm, text, icon, paintViewR, paintIconR, paintTextR)

        val tr = g2d.transform
        if (clockwise) {
            g2d.rotate(Math.PI / 2)
            g2d.translate(0, -c.width)
        } else {
            g2d.rotate(Math.PI / 2)
            g2d.translate(-c.height, 0)
        }

        if (icon != null) {
            icon.paintIcon(c, g2d, paintIconR.x, paintIconR.y)
        }

        if (text != null) {
            val textX = paintTextR.x
            val textY = paintTextR.y + fm.ascent

            if (label.isEnabled) {
                paintEnabledText(label, g2d, clippedText, textX, textY)
            }else{
                paintDisabledText(label, g2d, clippedText, textX, textY)
            }
        }

        g2d.transform = tr
        g2d.dispose()
    }

}