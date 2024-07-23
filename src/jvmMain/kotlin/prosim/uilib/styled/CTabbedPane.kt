package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import java.awt.Color
import java.awt.Font
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JTabbedPane

open class CTabbedPane(val primary: Boolean, val fontType: FontType) : JTabbedPane() {

    init {
        this.setUI(CTabbedPaneUI(primary, fontType))
    }

    override fun paint(g: Graphics) {
        val g2d = g.create() as Graphics2D

        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)
        g2d.dispose()
    }

    override fun getBackground(): Color {
        return if (primary) UIStates.theme.get().globalLaF.bgPrimary else UIStates.theme.get().globalLaF.bgSecondary
    }

    override fun getForeground(): Color {
        return UIStates.theme.get().textLaF.base
    }

    override fun getFont(): Font {
        return try {
            fontType.getFont()
        } catch (e: NullPointerException) {
            super.getFont()
        }
    }

}