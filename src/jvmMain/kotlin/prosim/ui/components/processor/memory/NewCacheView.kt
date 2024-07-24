package prosim.ui.components.processor.memory

import emulator.kit.assembler.CodeStyle
import emulator.kit.memory.Cache
import prosim.ui.Events
import prosim.ui.States
import prosim.uilib.UIStates
import prosim.uilib.styled.params.FontType
import prosim.uilib.styled.table.CVirtualTable
import prosim.uilib.styled.table.CVirtualTableUI
import java.awt.Color
import java.lang.ref.WeakReference

class NewCacheView(val cache: Cache) : CVirtualTable(
    FontType.CODE,
    FontType.BASIC,
    cache.model.rows.size * cache.model.blockCount,
    cache.model.offsetCount + 6,
    16,
    cache.model.offsetCount + 6,
    arrayOf("i", "m", "v", "d", "tag", *Array(cache.model.offsetCount) { it.toString(16) }, "ascii"),
    null,
    defaultWeight = 1.0
) {

    init {
        updateCellContent()

        Events.exe.addListener(WeakReference(this)) {
            updateCellContent()
        }

        Events.compile.addListener(WeakReference(this)) {
            updateCellContent()
        }
    }

    override fun getCellContent(contentRowID: Int, contentColID: Int): String {
        val rowIndex = contentRowID / cache.model.blockCount
        val blockIndex = contentRowID % cache.model.blockCount
        val block = cache.model.rows.getOrNull(rowIndex)?.blocks?.getOrNull(blockIndex) ?: return "?"

        return when (contentColID) {
            0 -> if (blockIndex == 0) rowIndex.toString(16) else ""
            1 -> blockIndex.toString(16)
            2 -> if (block.valid) "1" else "0"
            3 -> if (block.dirty) "1" else "0"
            4 -> {
                val tag = block.tag
                if (tag != null) tag.toHex().toRawZeroTrimmedString() else "invalid"
            }

            in 5..<(5 + cache.model.offsetCount) -> {
                block.data.getOrNull(contentColID - 5)?.value?.toHex()?.toRawString() ?: "-"
            }

            5 + cache.model.offsetCount -> {
                block.data.joinToString("") { it.value.toASCII() }
            }

            else -> {
                "?"
            }
        }
    }

    override fun isEditable(contentRowID: Int, contentColID: Int): Boolean {
        return false
    }

    override fun onEdit(newVal: String, contentRowID: Int, contentColID: Int) {
        // unused
    }

    override fun customCellFGColor(contentRowID: Int, contentColID: Int): Color? {
        if (contentColID in 5..<5 + cache.model.offsetCount) {
            val rowIndex = contentRowID / cache.model.blockCount
            val blockIndex = contentRowID % cache.model.blockCount
            val block = cache.model.rows.getOrNull(rowIndex)?.blocks?.getOrNull(blockIndex) ?: return null
            val instance = block.data.getOrNull(contentColID - 5) ?: return null
            val instAddr = instance.address?.toHex() ?: return null
            val pcAddr = States.arch.get().regContainer.pc.get().toHex()
            if (instAddr.toRawString() == pcAddr.toRawString()) {
                return UIStates.theme.get().getColor(CodeStyle.GREENPC)
            }
        }

        if (contentColID == 2) {
            val rowIndex = contentRowID / cache.model.blockCount
            val blockIndex = contentRowID % cache.model.blockCount
            val block = cache.model.rows.getOrNull(rowIndex)?.blocks?.getOrNull(blockIndex) ?: return null
            return if (!block.valid) UIStates.theme.get().getColor(CodeStyle.error) else null
        }

        if (contentColID == 3) {
            val rowIndex = contentRowID / cache.model.blockCount
            val blockIndex = contentRowID % cache.model.blockCount
            val block = cache.model.rows.getOrNull(rowIndex)?.blocks?.getOrNull(blockIndex) ?: return null
            return if (block.dirty) UIStates.theme.get().getColor(CodeStyle.YELLOW) else null
        }

        return null
    }

    override fun customCellBGColor(contentRowID: Int, contentColID: Int): Color? {
        val rowIndex = contentRowID / cache.model.blockCount
        val blockIndex = contentRowID % cache.model.blockCount
        val block = cache.model.rows.getOrNull(rowIndex)?.blocks?.getOrNull(blockIndex) ?: return null
        return if (block.dirty) UIStates.theme.get().getColor(CodeStyle.BASE6) else if(block.valid) UIStates.theme.get().getColor(CodeStyle.BASE7) else null
    }

    override fun onCellClick(cell: CVirtualTableUI.CCellRenderer, contentRowID: Int, contentColID: Int) {
        // unused
    }

    override fun onHeaderClick(header: CVirtualTableUI.CHeaderRenderer, headerRowID: Int, headerColID: Int) {
        // unused
    }


}