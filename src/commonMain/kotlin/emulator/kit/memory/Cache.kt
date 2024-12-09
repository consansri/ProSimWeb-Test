package emulator.kit.memory

import cengine.util.Endianness
import cengine.util.integer.*
import cengine.util.newint.Int32.Companion.toInt32
import cengine.util.newint.IntNumber
import debug.DebugTools
import emulator.core.*
import emulator.kit.common.IConsole
import emulator.kit.nativeLog
import emulator.kit.nativeWarn
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Represents a cache that extends the functionality of a provided backing memory and utilizes an instance of [IConsole] for logging messages.
 *
 * This abstract class defines the basic structure and operations of a cache, such as loading and storing data, accessing cache blocks, and managing cache rows.
 *
 * Subclasses of Cache must implement the abstract methods accessCache, updateCache, and writeBackAll to define the specific behavior of the cache.
 *
 * The Cache class provides methods for loading data from the cache, storing data into the cache, and retrieving all cache blocks.
 *
 * Cache also includes nested classes CacheBlock and CacheRow, which represent blocks and rows within the cache, respectively.
 *
 * Each CacheRow contains multiple CacheInstances, which hold the actual data values along with metadata such as validity and dirtiness.
 *
 * The Cache class defines CacheRowState enum to represent the state of a cache row, and AccessResult data class to encapsulate the result of cache access.
 */
sealed class Cache<ADDR : IntNumber<*>, INSTANCE : IntNumber<*>>(
    protected val backingMemory: Memory<ADDR, INSTANCE>,
    val indexBits: Int,
    val blockCount: Int,
    val offsetBits: Int,
    val replaceAlgo: ReplaceAlgo,
    val toAddr: IntNumber<*>.() -> ADDR,
    val toInstance: IntNumber<*>.() -> INSTANCE,
) : Memory<ADDR, INSTANCE>() {

    override val init: INSTANCE
        get() = backingMemory.init

    val model = Model()

    init {
        if (DebugTools.KIT_showCacheInfo) nativeLog(this.toString())
    }

    private val addrBits: Int = 0.toInt32().addr().bitWidth

    override fun globalEndianess(): Endianness = backingMemory.globalEndianess()
    override fun clear() {
        model.clear()
    }

    fun writeBackAll() {
        model.wbAll()
    }

    final override fun IntNumber<*>.addr(): ADDR = this.toAddr()
    final override fun IntNumber<*>.instance(): INSTANCE = this.toInstance()

    private fun buildAddr(tag: ADDR, index: ADDR, offset: ADDR): ADDR = ((((tag shl indexBits) or index.toInt32().value) shl offsetBits) or offset.toInt32().value).addr()

    private fun ADDR.offset(): ADDR = this.and(IntNumber.bitMask(offsetBits)).addr()

    override fun loadInstance(address: ADDR, tracker: AccessTracker): INSTANCE {
        val offsetIndex = address.offset()

        val searchResult = model.search(address)

        if (searchResult != null) {
            // HIT
            tracker.hits++

            val (row, offset) = searchResult

            val rowData = row.read(offset)

            return rowData[offsetIndex.toInt32().value]
        }

        val fetchedResult = model.fetch(address)

        // MISS
        tracker.misses++
        if (fetchedResult.third) tracker.writeBacks++

        val rowData = fetchedResult.first.read(fetchedResult.second)

        return rowData[offsetIndex.toInt32().value]
    }

    override fun storeInstance(address: ADDR, value: INSTANCE, tracker: AccessTracker) {
        val offsetIndex = address.offset()

        val searchResult = model.search(address)

        if (searchResult != null) {
            // HIT
            tracker.hits++

            val (row, block) = searchResult

            row.write(block, offsetIndex.toInt32().value, value)

            return
        }

        val (row, block, wb) = model.fetch(address)

        // MISS
        tracker.misses++
        if (wb) tracker.writeBacks++

        row.write(block, offsetIndex.toInt32().value, value)
    }

    override fun toString(): String = "${this::class.simpleName} $model"


    inner class Model {

        val tagBits = addrBits - indexBits - offsetBits

        val indexCount = 2.0.pow(indexBits).roundToInt()
        val offsetCount = 2.0.pow(offsetBits).roundToInt()

        val rows = Array<CacheRow>(indexCount) {
            CacheRow(it.toInt32().addr().index())
        }

        fun clear() {
            rows.forEach {
                it.clear()
            }
        }

        private fun ADDR.tag(): ADDR = (shr(indexBits + offsetBits) and IntNumber.bitMask(tagBits)).addr()

        private fun ADDR.index(): ADDR = (shr(offsetBits) and IntNumber.bitMask(indexBits)).addr()

        fun search(address: ADDR): Pair<CacheRow, Int>? {
            val tag = address.tag()
            val index = address.index()
            rows.forEach {
                val index = it.compare(tag, index)
                if (index != null) return it to index
            }
            return null
        }

        /**
         * @return [CacheRow], [blockIndex], [neededWriteBack] (if block was valid and dirty)
         */
        fun fetch(address: ADDR): Triple<CacheRow, Int, Boolean> {
            val tag = address.tag()
            val index = address.index()

            val row = rows.firstOrNull {
                it.rowIndex == index
            } ?: throw MemoryException("Invalid row index: ${index}.")
            val (blockIndex, wroteBack) = row.fetchBlock(tag)
            return Triple(row, blockIndex, wroteBack)
        }

        fun wbAll() {
            rows.forEach { row ->
                row.blocks.forEach {
                    it.writeBackIfDirty(row.rowIndex)
                }
            }
        }

        override fun toString(): String {
            return """
                CacheModel
                - instanceSize:   ${init.bitWidth}
                - addrSize:       $addrBits
                - tagBits:        $tagBits
                - indexBits:      $indexBits
                - offsetBits:     $offsetBits
                - blockCount:     $blockCount
                - replaceAlgo:    $replaceAlgo
            """.trimIndent()
        }

        inner class CacheRow(val rowIndex: ADDR) {

            val decider = when (replaceAlgo) {
                ReplaceAlgo.FIFO -> Decider.FIFO(blockCount)
                ReplaceAlgo.LRU -> Decider.LRU(blockCount)
                ReplaceAlgo.RANDOM -> Decider.RANDOM(blockCount)
            }

            val blocks: Array<CacheBlock> = Array(blockCount) {
                CacheBlock()
            }

            /**
             * @return block index on (Hit) and null on (Miss)
             */
            fun compare(tag: ADDR, index: ADDR): Int? {
                if (this.rowIndex != index) return null
                blocks.forEachIndexed { blockIndex, cacheBlock ->
                    if (cacheBlock.compare(tag)) return blockIndex
                }
                return null
            }

            fun read(blockIndex: Int): List<INSTANCE> {
                decider.read(blockIndex)
                return blocks[blockIndex].read()
            }

            fun write(blockIndex: Int, offsetIndex: Int, vararg values: INSTANCE) {
                decider.write(blockIndex)
                values.forEachIndexed { i, value ->
                    if (offsetIndex + i < offsetCount) {
                        blocks[blockIndex].write(offsetIndex + i, value)
                    } else {
                        nativeWarn("Invalid offset for cache write! (offset: ${offsetIndex + i})")
                    }
                }
            }

            /**
             * @return Pair of [blockIndex] and [neededWriteBack] -> true (wrote back old block) or false (write back not needed)
             */
            fun fetchBlock(tag: ADDR): Pair<Int, Boolean> {
                val blockIndex = decider.indexToReplace()
                val rowAddr = buildAddr(tag, rowIndex, 0.toInt32().addr())
                val neededWriteBack = blocks[blockIndex].writeBackIfDirty(rowIndex)
                val values = backingMemory.loadArray(rowAddr, offsetCount)
                blocks[blockIndex] = CacheBlock(values.toMutableList(), tag)
                decider.fetch(blockIndex)
                return blockIndex to neededWriteBack
            }

            fun clear() {
                for (i in blocks.indices) {
                    blocks[i] = CacheBlock()
                }
                decider.reset()
            }
        }

        inner class CacheBlock(
            val data: MutableList<INSTANCE> = MutableList(offsetCount) {
                init
            },
            var tag: ADDR? = null,
        ) {
            var dirty: Boolean = false
            val valid: Boolean
                get() {
                    return tag != null
                }

            fun compare(tag: ADDR): Boolean {
                return this.tag == tag
            }

            fun read(): List<INSTANCE> = data.toList()

            fun write(offsetIndex: Int, value: INSTANCE) {
                data[offsetIndex] = value
                dirty = true
            }

            /**
             * @return returns true if the block needed a write back!
             */
            fun writeBackIfDirty(rowIndex: ADDR): Boolean {
                val tag = tag
                if (!dirty || tag == null) return false
                val rowAddr = buildAddr(tag, rowIndex, 0.toInt32().addr())
                backingMemory.storeArray(rowAddr, data)
                dirty = false
                return true
            }
        }
    }

    sealed class Decider(size: Int) {
        val range = 0..<size

        abstract fun indexToReplace(): Int
        abstract fun read(index: Int)
        abstract fun write(index: Int)
        abstract fun fetch(index: Int)
        abstract fun reset()

        fun Array<Int>.moveToLast(index: Int) {
            val list = this.toMutableList()
            list.remove(index)
            list.add(index)
            list.forEachIndexed { i, value ->
                this[i] = value
            }
        }

        class FIFO(size: Int) : Decider(size) {
            private val order = Array(size) {
                it
            }

            override fun indexToReplace(): Int {
                return order.first()
            }

            override fun read(index: Int) {}

            override fun write(index: Int) {}

            override fun fetch(index: Int) {
                order.moveToLast(index)
            }

            override fun reset() {
                order.sort()
            }
        }

        class LRU(size: Int) : Decider(size) {
            private val order = Array(size) {
                it
            }

            override fun indexToReplace(): Int {
                return order.first()
            }

            override fun read(index: Int) {
                order.moveToLast(index)
            }

            override fun write(index: Int) {
                read(index)
            }

            override fun fetch(index: Int) {}

            override fun reset() {
                order.sort()
            }
        }

        class RANDOM(size: Int) : Decider(size) {
            private var currIndexToReplace = range.random()
            override fun indexToReplace(): Int = currIndexToReplace

            override fun read(index: Int) {
                currIndexToReplace = range.random()
            }

            override fun write(index: Int) {
                currIndexToReplace = range.random()
            }

            override fun fetch(index: Int) {}

            override fun reset() {
                currIndexToReplace = range.random()
            }
        }
    }

    enum class ReplaceAlgo {
        FIFO,
        LRU,
        RANDOM
    }

    data class AccessResult(val hit: Boolean, val writeBack: Boolean) {
        override fun toString(): String {
            return "Cache ${if (hit) "HIT" else "MISS"} (needed write back: $writeBack)"
        }
    }

    /**
     * Only for settings
     */
    enum class Setting(val uiName: String) {
        NONE("NONE"),
        DirectedMapped("Direct Mapped"),
        FullAssociativeRandom("Fully Associative Random"),
        FullAssociativeLRU("Fully Associative LRU"),
        FullAssociativeFIFO("Fully Associative FIFO"),
        SetAssociativeRandom("Set Associative Random"),
        SetAssociativeLRU("Set Associative LRU"),
        SetAssociativeFIFO("Set Associative FIFO");

        override fun toString(): String {
            return uiName
        }
    }
}