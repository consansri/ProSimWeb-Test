package emulator.kit.memory

import emulator.kit.common.IConsole
import kotlin.math.log
import kotlin.math.roundToInt

/**
 * Represents a fully associative cache implementation that extends the Cache class.
 *
 * @property backingMemory The backing memory for the cache.
 * @property console The console for logging messages.
 * @property tagBits The number of bits used for tag in the cache address.
 * @property offsetBits The number of bits used for offset in the cache address.
 * @property name The name of the fully associative cache.
 *
 * @constructor Creates a FullyAssociativeCache with the specified parameters.
 *
 * @throws Exception if the combination of tag and offset widths does not match the address size.
 *
 * @see Cache
 */
class FACache(
    backingMemory: Memory,
    console: IConsole,
    offsetBits: Int,
    blockCount: Int,
    replaceAlgo: Model.ReplaceAlgo,
    override val name: String = "Cache (FA)"
) : Cache(
    backingMemory,
    console,
    offsetBits = offsetBits,
    blockCount = blockCount,
    replaceAlgo = replaceAlgo,
    indexBits = 0,
) {
    constructor(backingMemory: Memory, console: IConsole, cacheSize: CacheSize, replaceAlgo: Model.ReplaceAlgo, name: String = "Cache") : this(
        backingMemory,
        console,
        blockCount = (cacheSize.bytes / CacheSize.BYTECOUNT_IN_ROW).toInt(),
        offsetBits = log((CacheSize.BYTECOUNT_IN_ROW / backingMemory.instanceSize.byteCount).toDouble(), 2.0).roundToInt(),
        replaceAlgo = replaceAlgo,
        name = "$name($cacheSize FA ${replaceAlgo})"
    )
}