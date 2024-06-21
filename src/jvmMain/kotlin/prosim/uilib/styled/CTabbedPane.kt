package prosim.uilib.styled

import prosim.uilib.styled.params.FontType
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JTabbedPane

open class CTabbedPane( val primary: Boolean, fontType: FontType) : JTabbedPane() {

    init {
        this.setUI(CTabbedPaneUI( primary, fontType))
    }

    override fun paint(g: Graphics) {
        val g2d = g.create() as Graphics2D

        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)
        g2d.dispose()
    }
}