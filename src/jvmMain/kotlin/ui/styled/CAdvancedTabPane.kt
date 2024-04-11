package me.c3.ui.styled

import me.c3.ui.UIManager
import me.c3.ui.components.styled.CIconButton
import me.c3.ui.components.styled.CPanel
import me.c3.ui.components.styled.CScrollPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.BorderFactory

open class CAdvancedTabPane(private val uiManager: UIManager, private val tabsAreCloseable: Boolean, primary: Boolean = true, borderMode: BorderMode = BorderMode.NONE) : CPanel(uiManager, primary, borderMode) {

    private val tabsPane = CPanel(uiManager, primary, BorderMode.SOUTH)
    private val contentPane = CScrollPane(uiManager, primary, CPanel(uiManager))

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
        val closeableTab = ClosableTab(uiManager, !primary, tabsAreCloseable, tab, content)
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
        val closeableTab = ClosableTab(uiManager, !primary, tabsAreCloseable, tab, content, action)
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

    private fun removeTab(closeableTab: ClosableTab) {
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

    class ClosableTab(uiManager: UIManager, primary: Boolean, private val isCloseable: Boolean, val tab: Component, val content: Component, val actionEvent: ((Event, ClosableTab) -> Unit)? = null) : CPanel(uiManager, primary, roundCorners = true) {

        val unselectedPrimaryValue = primary
        val closeButton = CIconButton(uiManager, uiManager.icons.close, CIconButton.Mode.SECONDARY_SMALL)

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