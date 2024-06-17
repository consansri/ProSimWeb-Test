package visual.memory

import StyleAttr
import debug.DebugTools
import emulator.kit.Architecture
import emulator.kit.common.memory.Cache
import react.*
import visual.virtual.VirtualTable

external interface CacheViewProps : Props {
    var exeEventState: StateInstance<Boolean>
    var archState: StateInstance<Architecture>
    var cache: Cache
}

val CacheView = FC<CacheViewProps>() { props ->

    val (currExeAddr, setCurrExeAddr) = useState<String>()

    VirtualTable {
        this.headers = arrayOf("i", "m", "v", "d", "tag", *Array(props.cache.model.offsetCount) { it.toString(16) }, "ascii")
        this.colCount = props.cache.model.offsetCount + 6
        this.rowCount = props.cache.model.rows.size * props.cache.model.blockCount
        this.visibleRows = 8
        this.rowContent = { rowID ->
            val rowIndex = rowID / props.cache.model.blockCount
            val blockIndex = rowID % props.cache.model.blockCount
            val block = props.cache.model.rows.getOrNull(rowIndex)?.blocks?.getOrNull(blockIndex)

            arrayOf(
                rowIndex.toString(16) to null,
                blockIndex.toString(16) to null,
                if (block?.valid == true) ("1" to null) else "0" to StyleAttr.Main.DeleteColor.get(),
                if (block?.dirty == true) ("1" to StyleAttr.Main.DeleteColor.get()) else "0" to null,
                (block?.tag?.toHex()?.toRawZeroTrimmedString() ?: "invalid") to null,
                *block?.data?.map { it.value.toHex().toRawZeroTrimmedString() to if (it.address?.getRawHexStr() == currExeAddr) StyleAttr.Main.Table.FgPC else null }?.toTypedArray() ?: arrayOf(),
                (block?.data?.joinToString("") { it.value.toASCII() } ?: "") to null
            )
        }
    }

    useEffect(props.cache.model.rows) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Cache Data Changed!")
        }
    }

    useEffect(props.exeEventState.component1()) {
        if (DebugTools.REACT_showUpdateInfo) {
            console.log("REACT: Exe Event!")
        }
        setCurrExeAddr(props.archState.component1().regContainer.pc.variable.get().toHex().getRawHexStr())
    }
}