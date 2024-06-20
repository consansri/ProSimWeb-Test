package emulator.kit.memory

import emulator.kit.common.IConsole
import kotlin.math.log
import kotlin.math.roundToInt

class SACache(
    backingMemory: Memory,
    console: IConsole,
    rowBits: Int,
    offsetBits: Int,
    blockCount: Int,
    replaceAlgo: Model.ReplaceAlgo,
    override val name: String = "Cache (SA)"
) : Cache(
    backingMemory,
    console,
    indexBits = rowBits,
    offsetBits = offsetBits,
    blockCount = blockCount,
    replaceAlgo = replaceAlgo
) {
    constructor(backingMemory: Memory, console: IConsole, blockCount: Int, cacheSize: CacheSize, replaceAlgo: Model.ReplaceAlgo, name: String = "Cache") : this(
        backingMemory,
        console,
        blockCount = blockCount,
        rowBits = log(((cacheSize.bytes / CacheSize.BYTECOUNT_IN_ROW) / blockCount).toDouble(), 2.0).roundToInt(),
        offsetBits = log((CacheSize.BYTECOUNT_IN_ROW / backingMemory.instanceSize.getByteCount()).toDouble(), 2.0).roundToInt(),
        replaceAlgo = replaceAlgo,
        name = "$name($cacheSize ${blockCount}SA ${replaceAlgo})"
    )
}