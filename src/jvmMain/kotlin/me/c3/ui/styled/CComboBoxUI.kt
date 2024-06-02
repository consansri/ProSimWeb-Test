package me.c3.ui.styled

import me.c3.ui.States
import me.c3.ui.styled.params.FontType
import java.awt.*
import java.awt.event.FocusEvent
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.*
import javax.swing.plaf.basic.BasicComboBoxUI

class CComboBoxUI(private val fontType: FontType) : BasicComboBoxUI() {

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

        States.theme.addEvent { _ ->
            setDefaults(comboBox)
        }

        States.scale.addEvent { _ ->
            setDefaults(comboBox)
        }

        setDefaults(comboBox)
    }

    private fun setDefaults(pane: CComboBox<*>) {
        pane.font = fontType.getFont()
        pane.foreground = States.theme.get().textLaF.base
        pane.renderer = CComboBoxRenderer()
        pane.repaint()
    }

    override fun createArrowButton(): JButton {
        return CIconButton(States.icon.get().folderOpen, CIconButton.Mode.SECONDARY_SMALL).apply {
            iconBg = Color(0, 0, 0, 0)
            addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    comboBox.isPopupVisible = !comboBox.isPopupVisible
                }
            })
        }
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return Dimension(States.scale.get().controlScale.comboBoxWidth, super.getPreferredSize(c).height)
    }

    override fun paintCurrentValueBackground(g: Graphics, bounds: Rectangle, hasFocus: Boolean) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        if (isHovered) {
            val cornerRadius = States.scale.get().controlScale.cornerRadius
            g2.color = States.theme.get().globalLaF.bgPrimary
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
                c.background = if (isSelected) States.theme.get().globalLaF.bgPrimary else States.theme.get().globalLaF.bgSecondary
                c.foreground = States.theme.get().textLaF.base
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

    class CComboBoxRenderer() : DefaultListCellRenderer() {
        override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            background = if(isSelected) States.theme.get().globalLaF.bgSecondary else States.theme.get().globalLaF.bgPrimary
            foreground = States.theme.get().textLaF.base
            this.border = States.scale.get().controlScale.getNormalInsetBorder()
            horizontalAlignment = SwingConstants.CENTER
            return this
        }
    }

}