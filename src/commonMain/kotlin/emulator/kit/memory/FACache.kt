package emulator.kit.memory

import cengine.util.newint.IntNumber
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
class FACache<ADDR : IntNumber<*>, INSTANCE : IntNumber<*>>(
    backingMemory: Memory<ADDR, INSTANCE>,
    blockCount: Int,
    offsetBits: Int,
    replaceAlgo: ReplaceAlgo,
    toAddr: IntNumber<*>.() -> ADDR,
    toInstance: IntNumber<*>.() -> INSTANCE,
    override val name: String = "Cache (FA)"
) : Cache<ADDR, INSTANCE>(
    backingMemory,
    0,
    blockCount,
    offsetBits,
    replaceAlgo,
    toAddr,
    toInstance
) {
    constructor(backingMemory: Memory<ADDR, INSTANCE>, cacheSize: CacheSize, replaceAlgo: ReplaceAlgo, toAddr: IntNumber<*>.() -> ADDR, toInstance: IntNumber<*>.() -> INSTANCE, name: String = "Cache") : this(
        backingMemory,
        (cacheSize.bytes / CacheSize.BYTECOUNT_IN_ROW).toInt(),
        log((CacheSize.BYTECOUNT_IN_ROW / backingMemory.init.byteCount).toDouble(), 2.0).roundToInt(),
        replaceAlgo,
        toAddr,
        toInstance,
        "$name($cacheSize FA ${replaceAlgo})",
    )
}