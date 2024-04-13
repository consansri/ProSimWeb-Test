package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
import me.c3.ui.spacing.ScaleManager
import me.c3.ui.theme.ThemeManager
import me.c3.ui.theme.icons.ProSimIcons
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory

open class CAdvancedTabPane(private val themeManager: ThemeManager, private val scaleManager: ScaleManager, icons: ProSimIcons, private val tabsAreCloseable: Boolean, primary: Boolean = true, borderMode: BorderMode = BorderMode.NONE) : CPanel(themeManager, scaleManager, primary, borderMode) {

    private val tabsPane = CPanel(themeManager, scaleManager, primary, BorderMode.SOUTH)
    private val contentPane = CScrollPane(themeManager, scaleManager, primary, CPanel(themeManager, scaleManager))
    private val closeIcon = icons.close

    val tabs = mutableListOf<ClosableTab>()
    private var currentTab: ClosableTab? = null
        set(value) {
            field?.let { it.actionEvent?.let { it1 -> it1(ClosableTab.Event.LOSTFOCUS, it) } }
            field = value
            contentPane.setViewportView(value?.content)
            field?.select()
            tabs.filter { it != value }.forEach {
                it.deselect()
            }
        }

    init {
        attachContent()
    }

    fun addTab(tab: Component, content: Component) {
        val closeableTab = ClosableTab(themeManager, scaleManager, closeIcon,!primary, tabsAreCloseable, tab, content)
        closeableTab.closeButton.addActionListener {
            removeTab(closeableTab)
        }
        closeableTab.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                currentTab = closeableTab
            }
        })
        tabs.add(closeableTab)
        currentTab = closeableTab
        tabsPane.add(closeableTab)
    }

    fun addTab(tab: Component, content: Component, action: (ClosableTab.Event, ClosableTab) -> Unit) {
        val closeableTab = ClosableTab(themeManager, scaleManager, closeIcon,!primary, tabsAreCloseable, tab, content, action)
        closeableTab.closeButton.addActionListener {
            removeTab(closeableTab)
        }
        closeableTab.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent?) {
                currentTab = closeableTab
            }
        })
        tabs.add(closeableTab)
        currentTab = closeableTab
        tabsPane.add(closeableTab)
    }

    fun removeAllTabs() {
        val currTabs = ArrayList(tabs)
        currTabs.forEach {
            removeTab(it)
        }
    }

    fun getCurrent() = currentTab

    fun removeTab(closeableTab: ClosableTab) {
        closeableTab.actionEvent?.let { it(ClosableTab.Event.CLOSE, closeableTab) }
        tabs.remove(closeableTab)
        if (currentTab == closeableTab) currentTab = null
        tabsPane.remove(closeableTab)
        tabsPane.revalidate()
        tabsPane.repaint()
    }

    private fun attachContent() {
        layout = BorderLayout()
        tabsPane.layout = FlowLayout(FlowLayout.LEFT)

        add(tabsPane, BorderLayout.NORTH)
        add(contentPane, BorderLayout.CENTER)
    }

    class ClosableTab(themeManager: ThemeManager, scaleManager: ScaleManager, closeIcon: FlatSVGIcon,primary: Boolean, private val isCloseable: Boolean, val tab: Component, val content: Component, val actionEvent: ((Event, ClosableTab) -> Unit)? = null) : CPanel(themeManager, scaleManager, primary, roundCorners = true) {

        val unselectedPrimaryValue = primary
        val closeButton = CIconButton(themeManager, scaleManager, closeIcon, CIconButton.Mode.SECONDARY_SMALL)

        init {
            layout = BorderLayout()
            if (isCloseable) {
                add(closeButton, BorderLayout.EAST)
            }

            add(tab, BorderLayout.CENTER)
        }

        fun select() {
            this.primary = !unselectedPrimaryValue
        }

        fun deselect() {
            this.primary = unselectedPrimaryValue
        }

        enum class Event {
            LOSTFOCUS,
            CLOSE
        }
    }

}