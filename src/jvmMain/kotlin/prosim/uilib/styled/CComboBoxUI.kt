package prosim.uilib.styled

import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
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
        comboBox.renderer = CComboBoxRenderer()
    }

    override fun createArrowButton(): JButton? {
        return null
    }

    override fun getMaximumSize(c: JComponent?): Dimension {
        return Dimension(UIStates.scale.get().SIZE_COMBOBOX, super.getPreferredSize(c).height)
    }

    override fun paintCurrentValueBackground(g: Graphics, bounds: Rectangle, hasFocus: Boolean) {
        val g2 = g.create() as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        if (isHovered) {
            val cornerRadius = UIStates.scale.get().SIZE_CORNER_RADIUS
            g2.color = UIStates.theme.get().COLOR_BG_0
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
                c.background = if (isSelected) UIStates.theme.get().COLOR_BG_0 else UIStates.theme.get().COLOR_BG_1
                c.foreground = UIStates.theme.get().COLOR_FG_0
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
            background = if (isSelected) UIStates.theme.get().COLOR_BG_1 else UIStates.theme.get().COLOR_BG_0
            foreground = UIStates.theme.get().COLOR_FG_0
            this.border = UIStates.scale.get().BORDER_INSET_MEDIUM
            horizontalAlignment = SwingConstants.CENTER
            return this
        }
    }

}