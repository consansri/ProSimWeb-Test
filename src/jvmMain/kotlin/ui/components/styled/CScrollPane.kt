package me.c3.ui.components.styled

import me.c3.ui.theme.core.components.CScrollPaneUI
import me.c3.ui.theme.core.ui.UIAdapter
import org.intellij.lang.annotations.JdkConstants.HorizontalScrollBarPolicy
import org.intellij.lang.annotations.JdkConstants.VerticalScrollBarPolicy
import java.awt.Component
import java.awt.event.FocusEvent
import java.awt.event.FocusListener
import javax.swing.BorderFactory
import javax.swing.JScrollPane
import javax.swing.SwingUtilities
import javax.swing.UIManager

class CScrollPane(uiManager: me.c3.ui.UIManager, private val primary: Boolean) : JScrollPane(), UIAdapter {

    constructor(uiManager: me.c3.ui.UIManager, primary: Boolean, component: Component) : this(uiManager, primary) {
        this.setViewportView(component)
    }

    constructor(uiManager: me.c3.ui.UIManager, primary: Boolean, component: Component, vsb: Int, hsb: Int) : this(uiManager, primary, component) {
        this.verticalScrollBarPolicy = vsb
        this.horizontalScrollBarPolicy = hsb
    }

    init {

        setupUI(uiManager)
    }

    override fun setupUI(uiManager: me.c3.ui.UIManager) {
        SwingUtilities.invokeLater {
            setUI(CScrollPaneUI())

            uiManager.themeManager.addThemeChangeListener {
                background = if (primary) it.globalStyle.bgPrimary else it.globalStyle.bgSecondary
            }

            val currTheme = uiManager.currTheme()
            background = if (primary) currTheme.globalStyle.bgPrimary else currTheme.globalStyle.bgSecondary
        }
    }


}