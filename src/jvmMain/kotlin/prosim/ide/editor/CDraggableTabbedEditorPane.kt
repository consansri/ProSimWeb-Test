package prosim.ide.editor

import prosim.uilib.styled.CLabel
import prosim.uilib.styled.CPanel
import prosim.uilib.styled.CSplitPane
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.tabbed.CTabbedPane
import java.awt.BorderLayout
import java.awt.Component
import java.awt.Point
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import java.awt.event.MouseMotionAdapter
import javax.swing.JSplitPane
import javax.swing.SwingUtilities

class CDraggableTabbedEditorPane : CPanel() {

    private val placeholder: CPanel = CPanel().apply {
        layout = BorderLayout()
        add(CLabel("No tabs open", FontType.CODE), BorderLayout.CENTER)
    }
    private val leftPane: CTabbedPane = CTabbedPane().apply {
        addMouseListener(TabMouseListener())
        addMouseMotionListener(TabMouseMotionListener())
    }
    private val rightPane: CTabbedPane = CTabbedPane().apply {
        addMouseListener(TabMouseListener())
        addMouseMotionListener(TabMouseMotionListener())
    }
    private var splitPane: CSplitPane? = null
    private var draggedTab: EditorComponent? = null
    private var draggedTabIndex: Int = -1
    private var draggedTabPane: CTabbedPane? = null
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
        add(splitPane, BorderLayout.CENTER)
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

    private inner class TabMouseListener : MouseAdapter() {
        override fun mousePressed(e: MouseEvent) {
            val tabbedPane = e.source as CTabbedPane
            draggedTabIndex = tabbedPane.indexAtLocation(e.x, e.y)
            if (draggedTabIndex != -1) {
                draggedTab = tabbedPane.getComponentAt(draggedTabIndex) as? EditorComponent
                draggedTabPane = tabbedPane
            }
        }

        override fun mouseReleased(e: MouseEvent) {
            if (draggedTab != null) {
                val dropLocation = SwingUtilities.convertPoint(e.source as Component, e.point, this@CDraggableTabbedEditorPane)
                handleTabDrop(dropLocation)
            }
            draggedTab = null
            draggedTabIndex = -1
            draggedTabPane = null
        }
    }

    private inner class TabMouseMotionListener : MouseMotionAdapter() {
        override fun mouseDragged(e: MouseEvent) {
            repaint()
        }
    }

    private fun handleTabDrop(dropLocation: Point) {
        val targetComponent = findComponentAt(dropLocation)
        when (targetComponent) {
            is CTabbedPane -> moveTabToPane(targetComponent, dropLocation)
            else -> splitOrMerge(dropLocation)
        }
    }

    private fun moveTabToPane(targetPane: CTabbedPane, dropLocation: Point) {
        if (draggedTabPane != targetPane) {
            draggedTabPane?.remove(draggedTab)
            val targetIndex = targetPane.indexAtLocation(dropLocation.x, dropLocation.y)
            if (targetIndex != -1) {
                targetPane.insertTab(draggedTab!!.name, null, draggedTab, null, targetIndex)
            } else {
                targetPane.addTab(draggedTab!!.name, draggedTab)
            }
        } else {
            val targetIndex = targetPane.indexAtLocation(dropLocation.x, dropLocation.y)
            if (targetIndex != -1 && targetIndex != draggedTabIndex) {
                targetPane.moveTab(draggedTabIndex, targetIndex)
            }
        }
        cleanupEmptyPanes()
    }

    private fun splitOrMerge(dropLocation: Point) {
        when {
            splitPane == null -> {
                val orientation = if (dropLocation.x > width / 2) JSplitPane.HORIZONTAL_SPLIT else JSplitPane.VERTICAL_SPLIT
                draggedTabPane?.remove(draggedTab)
                rightPane.addTab(draggedTab!!.name, draggedTab)
                showSplitPane(orientation)
            }

            dropLocation.x < width / 3 -> {
                splitPane?.orientation = JSplitPane.HORIZONTAL_SPLIT
                draggedTabPane?.remove(draggedTab)
                leftPane.addTab(draggedTab!!.name, draggedTab)
            }

            dropLocation.x > width * 2 / 3 -> {
                splitPane?.orientation = JSplitPane.HORIZONTAL_SPLIT
                draggedTabPane?.remove(draggedTab)
                rightPane.addTab(draggedTab!!.name, draggedTab)
            }

            dropLocation.y < height / 3 -> {
                splitPane?.orientation = JSplitPane.VERTICAL_SPLIT
                draggedTabPane?.remove(draggedTab)
                leftPane.addTab(draggedTab!!.name, draggedTab)
            }

            dropLocation.y > height * 2 / 3 -> {
                splitPane?.orientation = JSplitPane.VERTICAL_SPLIT
                draggedTabPane?.remove(draggedTab)
                rightPane.addTab(draggedTab!!.name, draggedTab)
            }
        }
        cleanupEmptyPanes()
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

}