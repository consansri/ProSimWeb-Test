package prosim.ide.editor

import prosim.uilib.UIStates
import prosim.uilib.styled.*
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.tabbed.CTabbedPane
import prosim.uilib.styled.tabbed.DnDHandler
import java.awt.BorderLayout
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

class CDraggableTabbedEditorPane : CPanel() {

    private val placeholder: CPanel = CPanel(true).apply {
        layout = BorderLayout()
        add(CLabel("Open File through Tree", FontType.CODE), BorderLayout.CENTER)
    }
    private val leftPane: CTabbedPane = CTabbedPane()
    private val rightPane: CTabbedPane = CTabbedPane()
    private var splitPane: CSplitPane? = null
    private var state: State = State.EMPTY

    init {
        layout = BorderLayout()
        showPlaceholder()
    }

    private fun showPlaceholder() {
        removeAll()
        add(placeholder, BorderLayout.CENTER)
        state = State.EMPTY
        revalidate()
        repaint()
    }

    private fun showSinglePane() {
        removeAll()
        add(leftPane, BorderLayout.CENTER)
        state = State.SINGLE
        revalidate()
        repaint()
    }

    private fun showSplitPane(orientation: Int) {
        removeAll()
        splitPane = CSplitPane(orientation).apply {
            leftComponent = leftPane
            rightComponent = rightPane
            resizeWeight = 0.5
        }
        splitPane?.let { add(it, BorderLayout.CENTER) }
        state = if (orientation == JSplitPane.HORIZONTAL_SPLIT) {
            State.MULTIPLE_HORIZONTAL
        } else {
            State.MULTIPLE_VERTICAL
        }

        revalidate()
        repaint()
    }

    fun addTab(component: EditorComponent) {
        leftPane.addClosableTab(component)
        if (state == State.EMPTY) {
            showSinglePane()
        }
    }

    fun CTabbedPane.toOtherPane(index: Int) {
        if (this == leftPane) {
            val tabComponent = leftPane.getTabComponentAt(index)
            val contentComponent = leftPane.getComponentAt(index)
            leftPane.removeTabAt(index)

            val insertIndex = rightPane.tabCount
            rightPane.insertTab("", null, contentComponent, null, insertIndex)
            rightPane.setTabComponentAt(insertIndex, tabComponent)
        } else {
            val tabComponent = rightPane.getTabComponentAt(index)
            val contentComponent = rightPane.getComponentAt(index)
            rightPane.removeTabAt(index)

            val insertIndex = leftPane.tabCount
            leftPane.insertTab("", null, contentComponent, null, insertIndex)
            leftPane.setTabComponentAt(insertIndex, tabComponent)
        }
    }

    fun cleanupEmptyPanes() {
        when {
            leftPane.tabCount == 0 && rightPane.tabCount == 0 -> showPlaceholder()
            leftPane.tabCount == 0 -> {
                showSinglePane()
                leftPane.copyTabs(rightPane)
            }

            rightPane.tabCount == 0 -> {
                showSinglePane()
            }
        }
    }

    private enum class State {
        EMPTY,
        SINGLE,
        MULTIPLE_HORIZONTAL,
        MULTIPLE_VERTICAL
    }

    inner class RightClickMenu : MouseAdapter() {
        override fun mouseClicked(e: MouseEvent) {
            if (SwingUtilities.isRightMouseButton(e)) {
                val sourcePane = e.source as? CTabbedPane ?: return
                val index = sourcePane.indexAtLocation(e.x, e.y)
                if (index == -1) return

                showPopupMenu(e.point, sourcePane, index)
            }
        }

        private fun showPopupMenu(point: Point, tabbedPane: CTabbedPane, index: Int) {
            val menu = CPopupMenu()

            val splitHorizontally = CMenuItem("Split Horizontally").apply {
                icon = UIStates.icon.get().splitCells
                addActionListener {
                    tabbedPane.toOtherPane(index)
                    showSplitPane(JSplitPane.HORIZONTAL_SPLIT)
                }
            }

            val splitVertically = CMenuItem("Split Vertically").apply {
                icon = UIStates.icon.get().splitCells
                addActionListener {
                    tabbedPane.toOtherPane(index)
                    showSplitPane(JSplitPane.VERTICAL_SPLIT)
                }
            }

            val singlePane = CMenuItem("Single Pane").apply {
                icon = UIStates.icon.get().combineCells
                addActionListener {
                    showSinglePane()
                }
            }

            val closeAll = CMenuItem("Close All").apply {
                addActionListener {
                    showPlaceholder()
                }
            }

            cleanupEmptyPanes()

            menu.add(closeAll)
            menu.add(singlePane)
            menu.add(splitVertically)
            menu.add(splitHorizontally)

            menu.show(this@CDraggableTabbedEditorPane, point.x, point.y)
        }
    }

    private fun CTabbedPane.addTabFromTabData(tabData: DnDHandler.TabData) {
        tabData.sourceTabbedPane.removeTabAt(tabData.sourceTabIndex)

        val lastIndex = this.tabCount

        this.insertTab("", null, tabData.contentComp, null, lastIndex)
        this.setTabComponentAt(lastIndex, tabData.tabComp)
    }

}