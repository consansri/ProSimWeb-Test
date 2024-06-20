package me.c3.uilib.styled

import me.c3.ui.States
import java.awt.Color
import java.awt.Dimension
import java.awt.Graphics
import java.awt.Rectangle
import java.lang.ref.WeakReference
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.plaf.basic.BasicScrollPaneUI

class CScrollPaneUI() : BasicScrollPaneUI() {

    var scrollBarFgColor: Color = States.theme.get().globalLaF.borderColor
        set(value) {
            field = value
            scrollpane.verticalScrollBar.repaint()
            scrollpane.horizontalScrollBar.repaint()
        }

    var scrollBarBgColor: Color = Color(0, 0, 0, 0)
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
        pane.isOpaque = false

        States.scale.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        States.theme.addEvent(WeakReference(pane)) { _ ->
            setDefaults(pane)
        }

        setDefaults(pane)
    }

    private fun setDefaults(cScrollPane: CScrollPane) {
        cScrollPane.viewport.preferredSize = cScrollPane.preferredSize
        cScrollPane.viewport.isOpaque = false
        cScrollPane.background = if (cScrollPane.primary) States.theme.get().globalLaF.bgPrimary else States.theme.get().globalLaF.bgSecondary
        scrollBarBgColor = if (cScrollPane.primary) States.theme.get().globalLaF.bgPrimary else States.theme.get().globalLaF.bgSecondary
        scrollBarFgColor = States.theme.get().globalLaF.borderColor
        cScrollPane.verticalScrollBar.preferredSize = Dimension(States.scale.get().scrollScale.thumbSize, 0)
        cScrollPane.horizontalScrollBar.preferredSize = Dimension(0, States.scale.get().scrollScale.thumbSize)
        cScrollPane.repaint()
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