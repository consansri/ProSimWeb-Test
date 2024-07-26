package prosim.uilib.styled.tabbed

import java.awt.Component
import java.awt.datatransfer.DataFlavor
import java.awt.datatransfer.Transferable
import java.awt.datatransfer.UnsupportedFlavorException
import java.awt.dnd.*

class DnDHandler(val tabbedPane: CTabbedPane) : DropTargetListener, DragSourceListener, DragGestureListener {

    private val dragSource = DragSource.getDefaultDragSource()
    private var draggingTabIndex: Int = -1

    init {
        setup()
    }

    private fun setup() {
        dragSource.createDefaultDragGestureRecognizer(tabbedPane, DnDConstants.ACTION_MOVE, this)
        DropTarget(tabbedPane, DnDConstants.ACTION_MOVE, this, true)
    }

    override fun dragEnter(dtde: DropTargetDragEvent) {
        if (dtde.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
            dtde.acceptDrag(DnDConstants.ACTION_MOVE)
        }
    }

    override fun dragOver(dtde: DropTargetDragEvent) {
        if (dtde.isDataFlavorSupported(TabData.flavor)) {
            val targetIndex = tabbedPane.indexAtLocation(dtde.location.x, dtde.location.y)
            if (targetIndex >= 0 && targetIndex != draggingTabIndex) {
                val tabComp = dtde.transferable.getTransferData(TabData.flavor) as TabData
                tabbedPane.removeTabAt(draggingTabIndex)
                tabbedPane.insertTab("", null, tabComp.contentComp, null, targetIndex)
                tabbedPane.setTabComponentAt(targetIndex, tabComp.tabComp)

                draggingTabIndex = targetIndex
            }
        }
    }

    override fun dropActionChanged(dtde: DropTargetDragEvent?) {}

    override fun dragExit(dte: DropTargetEvent?) {}

    override fun drop(dtde: DropTargetDropEvent) {
        if (dtde.isDataFlavorSupported(TabData.flavor)) {
            dtde.acceptDrop(DnDConstants.ACTION_MOVE)
            dtde.dropComplete(true)
        } else {
            dtde.rejectDrop()
        }
    }

    override fun dragEnter(dsde: DragSourceDragEvent?) {}

    override fun dragOver(dsde: DragSourceDragEvent) {
    }

    override fun dropActionChanged(dsde: DragSourceDragEvent?) {}

    override fun dragExit(dse: DragSourceEvent?) {}

    override fun dragDropEnd(dsde: DragSourceDropEvent?) {
        draggingTabIndex = -1
    }

    override fun dragGestureRecognized(dge: DragGestureEvent) {
        draggingTabIndex = tabbedPane.indexAtLocation(dge.dragOrigin.x, dge.dragOrigin.y)
        if (draggingTabIndex >= 0) {
            val contentComponent = tabbedPane.getComponentAt(draggingTabIndex)
            val tabComponent = tabbedPane.getTabComponentAt(draggingTabIndex)
            val transferable = TabTransferable(TabData(tabComponent, contentComponent))

            dragSource.startDrag(dge, DragSource.DefaultMoveDrop, transferable, this)
        }
    }

    data class TabData(
        val tabComp: Component,
        val contentComp: Component
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