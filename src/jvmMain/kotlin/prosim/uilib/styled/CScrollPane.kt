package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Color
import java.awt.Component
import java.awt.Graphics
import java.awt.Graphics2D
import javax.swing.JComponent
import javax.swing.JScrollPane
import javax.swing.KeyStroke

open class CScrollPane(val primary: Boolean, c: Component?) : JScrollPane(c) {

    constructor(primary: Boolean) : this(primary, null)

    constructor(primary: Boolean, component: Component, vsb: Int, hsb: Int) : this(primary, component) {
        this.verticalScrollBarPolicy = vsb
        this.horizontalScrollBarPolicy = hsb
    }

    init {
        this.setUI(CScrollPaneUI(primary))
        verticalScrollBar.unitIncrement = 16
        horizontalScrollBar.unitIncrement = 16
    }

    override fun paint(g: Graphics?) {
        val g2d = g?.create() as? Graphics2D ?: return
        g2d.color = background
        g2d.fillRect(0, 0, width, height)

        super.paint(g2d)

        g2d.dispose()
    }

    override fun getBackground(): Color {
        return if (primary) UIStates.theme.get().globalLaF.bgPrimary else UIStates.theme.get().globalLaF.bgSecondary
    }

    companion object {
        fun removeArrowKeyScrolling(scrollPane: JScrollPane) {
            val inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

            // Remove bindings for arrow keys
            inputMap.put(KeyStroke.getKeyStroke("UP"), "none")
            inputMap.put(KeyStroke.getKeyStroke("DOWN"), "none")
            inputMap.put(KeyStroke.getKeyStroke("LEFT"), "none")
            inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "none")
        }

        fun removePageUpDownScrolling(scrollPane: JScrollPane) {
            val inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

            inputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), "none")
            inputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "none")
        }

        fun restoreArrowKeyScrolling(scrollPane: JScrollPane) {
            val inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

            // restore bindings for arrow keys
            inputMap.put(KeyStroke.getKeyStroke("UP"), "scrollUp")
            inputMap.put(KeyStroke.getKeyStroke("DOWN"), "scrollDown")
            inputMap.put(KeyStroke.getKeyStroke("LEFT"), "scrollLeft")
            inputMap.put(KeyStroke.getKeyStroke("RIGHT"), "scrollRight")
        }

        fun restorePageUpDownScrolling(scrollPane: JScrollPane) {
            val inputMap = scrollPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)

            inputMap.put(KeyStroke.getKeyStroke("PAGE_UP"), "scrollUpExtend")
            inputMap.put(KeyStroke.getKeyStroke("PAGE_DOWN"), "scrollDownExtend")
        }
    }

}