package me.c3.ui.styled

import com.formdev.flatlaf.extras.FlatSVGIcon
import me.c3.ui.scale.ScaleManager
import me.c3.ui.styled.params.BorderMode
import me.c3.ui.styled.params.FontType
import me.c3.ui.theme.ThemeManager
import me.c3.ui.resources.icons.ProSimIcons
import java.awt.BorderLayout
import java.awt.Component
import java.awt.FlowLayout
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.SwingConstants
import javax.swing.SwingUtilities

open class CAdvancedTabPane(
    private val tm: ThemeManager,
    private val sm: ScaleManager,
    icons: ProSimIcons,
    private val tabsAreCloseable: Boolean,
    primary: Boolean = true,
    borderMode: BorderMode = BorderMode.NONE,
    emptyMessage: String = ""
) : CPanel(tm, sm, primary, borderMode) {

    private val emptyField = CLabel(tm, sm, emptyMessage, FontType.CODE).apply {
        alignmentX = CENTER_ALIGNMENT
        alignmentY = CENTER_ALIGNMENT
        horizontalAlignment = SwingConstants.CENTER
        verticalAlignment = SwingConstants.CENTER
    }
    private val tabsPane = CPanel(tm, sm, primary, BorderMode.SOUTH)
    val contentPane = CScrollPane(tm, sm, primary, CPanel(tm, sm))
    private val closeIcon = icons.close

    val tabs = mutableListOf<ClosableTab>()
    private var currentTab: ClosableTab? = null
        set(value) {
            field?.let { it.actionEvent?.let { it1 -> it1(Event.LOSTFOCUS, it) } }
            field = value
            contentPane.setViewportView(value?.content)
            value?.content?.requestFocus()
            field?.select()
            tabs.filter { it != value }.forEach {
                it.deselect()
            }
            if (value == null) {
                contentPane.setViewportView(emptyField)
            }
        }

    init {
        attachContent()
    }

    fun select(index: Int): Boolean {
        return if (index in tabs.indices) {
            currentTab = tabs[index]
            true
        } else {
            false
        }
    }

    fun addTab(tab: Component, content: Component) {
        val closeableTab = ClosableTab(tm, sm, closeIcon, !primary, tabsAreCloseable, tab, content)
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

    fun addTab(tab: Component, content: Component, action: (Event, ClosableTab) -> Unit) {
        val closeableTab = ClosableTab(tm, sm, closeIcon, !primary, tabsAreCloseable, tab, content, action)
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
        closeableTab.actionEvent?.let { it(Event.CLOSE, closeableTab) }
        tabs.remove(closeableTab)
        if (currentTab == closeableTab) currentTab = null
        tabsPane.remove(closeableTab)
        tabsPane.revalidate()
        tabsPane.repaint()
    }

    fun selectCurrentTab(closeableTab: ClosableTab?) {
        currentTab = closeableTab
    }

    private fun attachContent() {
        layout = BorderLayout()
        tabsPane.layout = FlowLayout(FlowLayout.LEFT)

        add(tabsPane, BorderLayout.NORTH)
        add(contentPane, BorderLayout.CENTER)
    }

    inner class ClosableTab(tm: ThemeManager, sm: ScaleManager, closeIcon: FlatSVGIcon, primary: Boolean, private val isCloseable: Boolean, val tab: Component, val content: Component, val actionEvent: ((Event, ClosableTab) -> Unit)? = null) :
        CPanel(tm, sm, primary, roundCorners = true) {

        val unselectedPrimaryValue = primary
        val closeButton = CIconButton(tm, sm, closeIcon, CIconButton.Mode.SECONDARY_SMALL)

        init {
            layout = BorderLayout()
            if (isCloseable) {
                add(closeButton, BorderLayout.EAST)
            }

            this.addMouseListener(object : MouseAdapter() {
                override fun mouseClicked(e: MouseEvent) {
                    if (SwingUtilities.isRightMouseButton(e)) {
                        showTabContextMenu(e)
                    }
                }
            })

            add(tab, BorderLayout.CENTER)
        }

        fun showTabContextMenu(e: MouseEvent) {
            val popupMenu = CPopupMenu(tm, sm)

            val itemCloseAll = CMenuItem(tm, sm, "Close all tabs", FontType.BASIC).apply {
                addActionListener {
                    this@CAdvancedTabPane.removeAllTabs()
                }
            }

            popupMenu.add(itemCloseAll)

            popupMenu.show(this, e.x, e.y)
        }

        fun select() {
            this.primary = !unselectedPrimaryValue
        }

        fun deselect() {
            this.primary = unselectedPrimaryValue
        }
    }

    enum class Event {
        LOSTFOCUS,
        CLOSE
    }

}