package me.c3.ui.theme.core.components

import me.c3.ui.components.styled.CScrollPane
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Graphics2D
import java.awt.Rectangle
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.JScrollBar
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.plaf.basic.BasicScrollPaneUI

class CScrollPaneUI : BasicScrollPaneUI() {

    companion object {
        private const val THUMB_SIZE = 8
        private val THUMB_COLOR = Color(0x77777777, true)
        private val TRACK_COLOR = Color.RED
    }

    var scrollBarFgColor: Color = THUMB_COLOR
        set(value) {
            field = value
            scrollpane.verticalScrollBar.repaint()
            scrollpane.horizontalScrollBar.repaint()
        }

    var scrollBarBgColor: Color = TRACK_COLOR
        set(value) {
            field = value
            scrollpane.verticalScrollBar.repaint()
            scrollpane.horizontalScrollBar.repaint()
        }

    override fun installUI(x: JComponent?) {
        super.installUI(x)

        val pane = x as? CScrollPane ?: return
        pane.border = BorderFactory.createEmptyBorder()
        pane.verticalScrollBar.setUI(CScrollBarUI())
        pane.horizontalScrollBar.setUI(CScrollBarUI())
        pane.verticalScrollBar.preferredSize = Dimension(THUMB_SIZE, 0)
        pane.horizontalScrollBar.preferredSize = Dimension(0, THUMB_SIZE)
    }

    inner class CScrollBarUI() : BasicScrollBarUI() {
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