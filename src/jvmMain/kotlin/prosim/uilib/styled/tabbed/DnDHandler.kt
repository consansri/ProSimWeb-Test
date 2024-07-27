package prosim.uilib.styled.tabbed

import emulator.kit.nativeLog
import java.awt.Component
import java.awt.KeyboardFocusManager
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.*

class DnDHandler(tabbedPane: CTabbedPane) : DropTargetListener, DragSourceListener, DragGestureListener {

    private val dragSource = DragSource.getDefaultDragSource()

    init {
        setup(tabbedPane)
    }

    private fun setup(tabbedPane: CTabbedPane) {
        dragSource.createDefaultDragGestureRecognizer(tabbedPane, DnDConstants.ACTION_MOVE, this)
        DropTarget(tabbedPane, DnDConstants.ACTION_MOVE, this, true)
    }

    override fun dragEnter(dtde: DropTargetDragEvent) {
        if (dtde.isDataFlavorSupported(TabData.flavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE)
        } else {
            dtde.rejectDrag()
        }
    }

    override fun dragOver(dtde: DropTargetDragEvent) {
        if (dtde.isDataFlavorSupported(TabData.flavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE)
        } else {
            dtde.rejectDrag()
        }
    }

    override fun dropActionChanged(dtde: DropTargetDragEvent?) {}

    override fun dragExit(dte: DropTargetEvent?) {}

    override fun drop(dtde: DropTargetDropEvent) {
        if (dtde.isDataFlavorSupported(TabData.flavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE)

            val tabData = dtde.transferable.getTransferData(TabData.flavor) as TabData

            val target = dtde.dropTargetContext.component as? CTabbedPane ?: return
            val targetIndex = target.indexAtLocation(dtde.location.x, dtde.location.y)

            if (targetIndex >= 0 && (target != tabData.sourceTabbedPane || targetIndex != tabData.sourceTabIndex)) {

                tabData.sourceTabbedPane.removeTabAt(tabData.sourceTabIndex)

                target.insertTab("", null, tabData.contentComp, null, targetIndex)
                target.setTabComponentAt(targetIndex, tabData.tabComp)
            }

            dtde.dropComplete(true)
        } else {
            dtde.rejectDrop()
        }
    }

    override fun dragEnter(dsde: DragSourceDragEvent?) {
        nativeLog("Drag entered, focused component: ${KeyboardFocusManager.getCurrentKeyboardFocusManager().focusOwner}")
    }

    override fun dragOver(dsde: DragSourceDragEvent) {

    }

    override fun dropActionChanged(dsde: DragSourceDragEvent?) {}

    override fun dragExit(dse: DragSourceEvent?) {}

    override fun dragDropEnd(dsde: DragSourceDropEvent) {}

    override fun dragGestureRecognized(dge: DragGestureEvent) {
        val sourceTabbedPane = dge.component as? CTabbedPane ?: return
        val draggingTabIndex = sourceTabbedPane.indexAtLocation(dge.dragOrigin.x, dge.dragOrigin.y)
        if (draggingTabIndex >= 0) {
            val contentComponent = sourceTabbedPane.getComponentAt(draggingTabIndex)
            val tabComponent = sourceTabbedPane.getTabComponentAt(draggingTabIndex)

            val transferable = TabTransferable(TabData(tabComponent, contentComponent, sourceTabbedPane, draggingTabIndex))

            dragSource.startDrag(dge, DragSource.DefaultMoveDrop, transferable, this)
        }
    }

    data class TabData(
        val tabComp: Component,
        val contentComp: Component,
        val sourceTabbedPane: CTabbedPane,
        val sourceTabIndex: Int
    ) {
        companion object {
            val flavor: DataFlavor = DataFlavor(TabData::class.java, "Tab Data")
        }
    }

    class TabTransferable(val tabData: TabData) : Transferable {
        override fun getTransferDataFlavors(): Array<DataFlavor> {
            return arrayOf(TabData.flavor)
        }

        override fun isDataFlavorSupported(flavor: DataFlavor?): Boolean {
            return flavor == TabData.flavor
        }

        override fun getTransferData(flavor: DataFlavor?): TabData {
            if (isDataFlavorSupported(flavor)) {
                return tabData
            } else {
                throw UnsupportedFlavorException(flavor)
            }
        }
    }
}