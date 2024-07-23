package prosim.uilib.styled

import prosim.uilib.UIStates
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.plaf.basic.BasicScrollPaneUI

class CScrollPaneUI(val primary: Boolean) : BasicScrollPaneUI() {

    private val scrollBarFgColor: Color
        get() = UIStates.theme.get().globalLaF.borderColor

    private val scrollBarBgColor: Color
        get() = if (primary) UIStates.theme.get().globalLaF.bgPrimary else UIStates.theme.get().globalLaF.bgSecondary

    override fun installUI(x: JComponent?) {
        super.installUI(x)

        val pane = x as? CScrollPane ?: return

        pane.border = BorderFactory.createEmptyBorder()
        pane.verticalScrollBar.setUI(CScrollBarUI())
        pane.horizontalScrollBar.setUI(CScrollBarUI())
        pane.isOpaque = false
        pane.viewport.preferredSize = pane.preferredSize
        pane.viewport.isOpaque = false
        pane.verticalScrollBar.preferredSize = Dimension(UIStates.scale.get().scrollScale.thumbSize, 0)
        pane.horizontalScrollBar.preferredSize = Dimension(0, UIStates.scale.get().scrollScale.thumbSize)

        pane.revalidate()
        pane.repaint()
    }

    inner class CScrollBarUI() : BasicScrollBarUI() {

        override fun installUI(c: JComponent?) {
            super.installUI(c)

            c?.isOpaque = false
        }

        override fun configureScrollBarColors() {
            thumbDarkShadowColor = Color(0, 0, 0, 0)
            thumbLightShadowColor = Color(0, 0, 0, 0)
            thumbHighlightColor = Color(0, 0, 0, 0)
            trackHighlightColor = Color(0, 0, 0, 0)
        }

        override fun createDecreaseButton(orientation: Int): JButton {
            return InvisibleScrollBarButton()
        }

        override fun createIncreaseButton(orientation: Int): JButton {
            return InvisibleScrollBarButton()
        }

        override fun paintThumb(g: Graphics?, c: JComponent?, thumbBounds: Rectangle?) {
            if (g == null || c == null || thumbBounds == null) return
            g.color = scrollBarFgColor
            g.fillRect(thumbBounds.x, thumbBounds.y, thumbBounds.width, thumbBounds.height)
        }

        override fun paintTrack(g: Graphics?, c: JComponent?, trackBounds: Rectangle?) {
            if (g == null || c == null || trackBounds == null) return
            g.color = scrollBarBgColor
            g.fillRect(trackBounds.x, trackBounds.y, trackBounds.width, trackBounds.height)
        }

        override fun setThumbBounds(x: Int, y: Int, width: Int, height: Int) {
            super.setThumbBounds(x, y, width, height)
            this@CScrollPaneUI.scrollpane.repaint()
        }

        private inner class InvisibleScrollBarButton : JButton() {
            init {
                isOpaque = false
                isFocusable = false
                isFocusPainted = false
                isBorderPainted = false
                border = BorderFactory.createEmptyBorder()
            }
        }

    }

}