package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.theme.core.ui.UIAdapter
import java.awt.Color
import java.awt.Component
import javax.swing.BorderFactory
import javax.swing.DefaultListCellRenderer
import javax.swing.JComboBox
import javax.swing.JList
import javax.swing.SwingUtilities

open class CComboBox<T>(uiManager: UIManager, array: Array<T>) : JComboBox<T>(array), UIAdapter {

    init {
        this.setupUI(uiManager)
    }

    override fun setupUI(uiManager: UIManager) {
        SwingUtilities.invokeLater {
            this.setUI(CComboBoxUI(uiManager))

            uiManager.themeManager.addThemeChangeListener {
                setDefaults(uiManager)
            }

            uiManager.scaleManager.addScaleChangeEvent {
                setDefaults(uiManager)
            }

            setDefaults(uiManager)
        }
    }

    override fun setDefaults(uiManager: UIManager) {
        this.font = uiManager.currTheme().textLaF.getBaseFont().deriveFont(uiManager.currScale().fontScale.textSize)
        this.foreground = uiManager.currTheme().textLaF.base
        this.renderer = CComboBoxRenderer(uiManager)
        repaint()
    }

    class CComboBoxRenderer(val uiManager: UIManager): DefaultListCellRenderer(){
        init {
            this.background = uiManager.currTheme().globalLaF.bgSecondary
        }

        override fun getListCellRendererComponent(list: JList<*>?, value: Any?, index: Int, isSelected: Boolean, cellHasFocus: Boolean): Component {
            super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus)
            background = uiManager.currTheme().globalLaF.bgSecondary
            this.border = BorderFactory.createEmptyBorder()
            return this
        }
    }

}