package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.theme.ThemeManager
import java.awt.*
import javax.swing.BorderFactory
import javax.swing.JButton
import javax.swing.JComponent
import javax.swing.plaf.basic.BasicScrollBarUI
import javax.swing.plaf.basic.BasicScrollPaneUI

class CScrollPaneUI(private val tm: ThemeManager, private val sm: ScaleManager) : BasicScrollPaneUI() {

    var scrollBarFgColor: Color = tm.curr.globalLaF.borderColor
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

        sm.addScaleChangeEvent {
            setDefaults(pane)
        }

        tm.addThemeChangeListener {
            setDefaults(pane)
        }

        setDefaults(pane)
    }

    private fun setDefaults(cScrollPane: CScrollPane) {
        cScrollPane.viewport.preferredSize = cScrollPane.preferredSize
        cScrollPane.viewport.isOpaque = false
        cScrollPane.background = if (cScrollPane.primary) tm.curr.globalLaF.bgPrimary else tm.curr.globalLaF.bgSecondary
        scrollBarBgColor = if (cScrollPane.primary) tm.curr.globalLaF.bgPrimary else tm.curr.globalLaF.bgSecondary
        scrollBarFgColor = tm.curr.globalLaF.borderColor
        cScrollPane.verticalScrollBar.preferredSize = Dimension(sm.curr.scrollScale.thumbSize, 0)
        cScrollPane.horizontalScrollBar.preferredSize = Dimension(0, sm.curr.scrollScale.thumbSize)
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