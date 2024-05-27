package me.c3.ui.styled

import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicComboBoxUI

class CComboBoxUI(private val tm: ThemeManager, private val sm: ScaleManager, private val icons: ProSimIcons, private val fontType: FontType) : BasicComboBoxUI() {

    var isHovered: Boolean = false
        set(value) {
            field = value
            comboBox.repaint()
        }

    override fun installUI(c: JComponent?) {
        super.installUI(c)

        val comboBox = c as? CComboBox<*> ?: return

        comboBox.border = BorderFactory.createEmptyBorder()
        comboBox.isOpaque = false
        comboBox.addMouseListener(object : MouseAdapter() {
            override fun mouseEntered(e: MouseEvent?) {
                isHovered = true
            }

            override fun mouseExited(e: MouseEvent?) {
                isHovered = false
            }
        })

        tm.addThemeChangeListener {
            setDefaults(comboBox)
        }

        sm.addScaleChangeEvent {
            setDefaults(comboBox)
        }

        setDefaults(comboBox)
    }

    private fun setDefaults(pane: CComboBox<*>) {
        pane.font = fontType.getFont(tm, sm)
        pane.foreground = tm.curr.textLaF.base
        pane.renderer = CComboBoxRenderer(tm, sm)
        pane.repaint()
    }

    override fun createArrowButton(): JButton {
        return CIconButton(tm, sm, icons.folderOpen, CIconButton.Mode.SECONDARY_SMALL).apply {
            iconBg = Color(0, 0, 0, 0)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    comboBox.isPopupVisible = !comboBox.isPopupVisible
                }
            })
        }
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return Dimension(sm.curr.controlScale.comboBoxWidth, super.getPreferredSize(c).height)
    }

    override fun paintCurrentValueBackground(g: Graphics, bounds: Rectangle, hasFocus: Boolean) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        if (isHovered) {
            val cornerRadius = sm.curr.controlScale.cornerRadius
            g2.color = tm.curr.globalLaF.bgPrimary
            g2.fillRoundRect(bounds.x, bounds.y, bounds.width, bounds.height, cornerRadius, cornerRadius)
        }

        g2.dispose()
    }

    override fun paintCurrentValue(g: Graphics, bounds: Rectangle, hasFocus: Boolean) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val insets = comboBox.insets
        val width = bounds.width - insets.left - insets.right
        val height = bounds.height - insets.top - insets.bottom

        g2.color = comboBox.foreground
        g2.font = comboBox.font
        val selectedItem = comboBox.selectedItem?.toString() ?: ""
        val fm = g2.fontMetrics
        val stringWidth = fm.stringWidth(selectedItem)
        val stringHeight = fm.ascent + fm.descent
        val x = insets.left + (width - stringWidth) / 2
        val y = insets.top + (height + stringHeight) / 2 - fm.descent

        g2.drawString(selectedItem, x, y)

        g2.dispose()
    }

    override fun createRenderer(): ListCellRenderer<Any> {
        return object : DefaultListCellRenderer() {
            override fun getListCellRendererComponent(
                list: JList<*>?,
                value: Any?,
                index: Int,
                isSelected: Boolean,
                cellHasFocus: Boolean
            ): Component {
                val c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
                c.background = if (isSelected) tm.curr.globalLaF.bgPrimary else tm.curr.globalLaF.bgSecondary
                c.foreground = tm.curr.textLaF.base
                (c as? JComponent)?.border = BorderFactory.createEmptyBorder()
                return c
            }
        }
    }

    override fun installListeners() {
        super.installListeners()
        comboBox.addFocusListener(
            object : FocusHandler() {
                override fun focusGained(e: FocusEvent?) {
                    comboBox.repaint()
                }

                override fun focusLost(e: FocusEvent?) {
                    comboBox.repaint()
                }
            },
        )
    }

    class CComboBoxRenderer(private val tm: ThemeManager, private val sm: ScaleManager) : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            background = if(isSelected) tm.curr.globalLaF.bgSecondary else tm.curr.globalLaF.bgPrimary
            foreground = tm.curr.textLaF.base
            this.border = sm.curr.controlScale.getNormalInsetBorder()
            horizontalAlignment = SwingConstants.CENTER
            return this
        }
    }

}