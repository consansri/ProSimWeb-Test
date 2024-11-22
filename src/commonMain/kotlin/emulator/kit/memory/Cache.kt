package emulator.kit.memory

import cengine.util.integer.Size
import cengine.util.integer.Value
import debug.DebugTools
import emulator.core.*
import cengine.util.integer.*
import emulator.kit.common.IConsole
import emulator.kit.nativeLog
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
sealed class Cache(protected val backingMemory: Memory, val console: IConsole, indexBits: Int, blockCount: Int, offsetBits: Int, replaceAlgo: Model.ReplaceAlgo, final override val initHex: String = "0") : Memory() {
    final override val instanceSize: Size = backingMemory.instanceSize
    final override val addressSize: Size = backingMemory.addressSize

    val model = Model(backingMemory, console, instanceSize, addressSize, indexBits, offsetBits, blockCount, replaceAlgo, initHex)

    init {
        if (DebugTools.KIT_showCacheInfo) nativeLog(this.toString())
    }

    override fun globalEndianess(): Endianess = backingMemory.globalEndianess()
    override fun clear() {
        model.clear()
    }

    fun writeBackAll() {
        model.wbAll()
    }

    override fun load(address: Hex, amount: Int, tracker: AccessTracker, endianess: Endianess): Hex {
        val offsetIndex = address.toBin().rawInput.takeLast(model.offsetBits).toInt(2)

        if (offsetIndex + amount > model.offsetCount) {
            console.warn("Unaligned Cache Access!")
            return loadUnaligned(address, amount, tracker, endianess)
        }

        val searchResult = model.search(address.getUResized(addressSize))

        if (searchResult != null) {
            // HIT
            tracker.hits++

            val rowData = searchResult.first.read(searchResult.second)
            if (offsetIndex + amount - 1 >= model.offsetCount) console.warn("Unsupported Exceeding Cache Access!")
            val hexValues = mutableListOf<String>()

            repeat(amount) {
                hexValues += rowData[offsetIndex + it].toHex().rawInput
            }

            when (endianess) {
                Endianess.LittleEndian -> hexValues.reverse()
                Endianess.BigEndian -> {}
            }

            return Hex(hexValues.joinToString("") { it })
        }

        val fetchedResult = model.fetch(address)

        // MISS
        tracker.misses++
        if (fetchedResult.third) tracker.writeBacks++

        val rowData = fetchedResult.first.read(fetchedResult.second)

        val hexValues = mutableListOf<String>()

        repeat(amount) {
            hexValues += rowData[offsetIndex + it].toHex().rawInput
        }

        when (globalEndianess()) {
            Endianess.LittleEndian -> hexValues.reverse()
            Endianess.BigEndian -> {}
        }

        return Hex(hexValues.joinToString("") { it })
    }

    override fun store(address: Hex, value: Value, mark: InstanceType, readonly: Boolean, tracker: AccessTracker, endianess: Endianess) {
        val offsetIndex = address.toBin().rawInput.takeLast(model.offsetBits).toInt(2)
        val values = value.toHex().splitToArray(instanceSize)

        if (offsetIndex + values.size > model.offsetCount) {
            console.warn("Unaligned Cache Access!")
            return storeUnaligned(address, value, mark, readonly, tracker, endianess)
        }

        when (endianess) {
            Endianess.LittleEndian -> values.reverse()
            Endianess.BigEndian -> {}
        }

        val searchResult = model.search(address.getUResized(addressSize))

        if (searchResult != null) {
            // HIT
            tracker.hits++

            searchResult.first.write(searchResult.second, offsetIndex, *values.toCacheInstances(address))

            return
        }

        val fetchedResult = model.fetch(address)

        // MISS
        tracker.misses++
        if (fetchedResult.third) tracker.writeBacks++

        fetchedResult.first.write(fetchedResult.second, offsetIndex, *values.toCacheInstances(address))
    }

    private fun loadUnaligned(address: Hex, amount: Int, tracker: AccessTracker, endianess: Endianess): Hex {
        val ranges = mutableListOf<Pair<Hex, Int>>()
        var remaining = amount
        var currAddress: Value = address

        while (remaining > 0) {
            val offsetIndex = currAddress.toBin().rawInput.takeLast(model.offsetBits).toInt(2)
            val lastIndex = (remaining - 1) + offsetIndex
            val currAmount = if (lastIndex >= model.offsetCount) {
                model.offsetCount - offsetIndex
            } else {
                lastIndex - offsetIndex
            }
            ranges += currAddress.toHex() to currAmount
            remaining -= currAmount
            currAddress += currAmount.toValue(addressSize)
        }

        val values = mutableListOf<Hex>()

        ranges.forEach {
            values.addAll(load(it.first, it.second, tracker, Endianess.BigEndian).splitToArray(instanceSize))
        }

        when (endianess) {
            Endianess.LittleEndian -> values.reverse()
            Endianess.BigEndian -> {}
        }
        return Hex(values.joinToString("") { it.rawInput })
    }

    private fun storeUnaligned(address: Hex, value: Value, mark: InstanceType, readonly: Boolean, tracker: AccessTracker, endianess: Endianess) {
        val ranges = mutableListOf<Pair<Hex, Array<Hex>>>()

        val remaining = value.toHex().splitToArray(instanceSize).toMutableList()

        when (endianess) {
            Endianess.LittleEndian -> remaining.reverse()
            Endianess.BigEndian -> {}
        }

        var currAddress: Value = address

        while (remaining.isNotEmpty()) {
            val offsetIndex = currAddress.toBin().rawInput.takeLast(model.offsetBits).toInt(2)
            val lastIndex = (remaining.size - 1) + offsetIndex
            val currAmount = if (lastIndex >= model.offsetCount) {
                model.offsetCount - offsetIndex
            } else {
                lastIndex - offsetIndex
            }
            val currValues = remaining.subList(0, currAmount)
            ranges += currAddress.toHex() to currValues.toTypedArray()
            remaining -= currValues
            currAddress += currAmount.toValue(addressSize)
        }

        ranges.forEach {
            val joinedValue = Hex(it.second.joinToString("") { it.rawInput })
            store(it.first, joinedValue, mark, readonly, tracker, Endianess.BigEndian)
        }
    }

    override fun toString(): String = "${this::class.simpleName} $model"

    companion object {
        private fun Array<Hex>.toCacheInstances(address: Hex): Array<CacheInstance> {
            return this.mapIndexed { index, hex -> CacheInstance(hex, (address + index.toValue(address.size)).toHex()) }.toTypedArray()
        }
    }

    class Model(val backingMemory: Memory, val console: IConsole, val instanceSize: Size, val addrSize: Size, val indexBits: Int, val offsetBits: Int, val blockCount: Int, val replaceAlgo: ReplaceAlgo, val initHex: String = "0") {

        val tagBits = addrSize.bitWidth - indexBits - offsetBits

        val indexCount = 2.0.pow(indexBits).roundToInt()
        val offsetCount = 2.0.pow(offsetBits).roundToInt()

        val rows = Array<CacheRow>(indexCount) {
            CacheRow(if (indexBits > 0) it.toString(2).padStart(indexBits, '0') else "")
        }

        fun clear() {
            rows.forEach {
                it.clear()
            }
        }

        fun search(address: Hex): Pair<CacheRow, Int>? {
            val addrBinStr = address.toBin().rawInput
            val tagBinStr = addrBinStr.take(tagBits)
            val indexBinStr = addrBinStr.substring(tagBits, tagBits + indexBits)
            rows.forEach {
                val index = it.compare(tagBinStr, indexBinStr)
                if (index != null) return it to index
            }
            return null
        }

        /**
         * @return [CacheRow], [blockIndex], [neededWriteBack] (if block was valid and dirty)
         */
        fun fetch(address: Hex): Triple<CacheRow, Int, Boolean> {
            val addrBinStr = address.toBin().rawInput
            val tagBinStr = addrBinStr.take(tagBits)
            val indexBinStr = addrBinStr.substring(tagBits, tagBits + indexBits)
            val row = rows.firstOrNull {
                it.rowIndexBinStr == indexBinStr
            } ?: throw MemoryException("Invalid row index: ${indexBinStr.toIntOrNull(2)}.")
            val (blockIndex, wroteBack) = row.fetchBlock(tagBinStr)
            return Triple(row, blockIndex, wroteBack)
        }

        fun wbAll() {
            rows.forEach { row ->
                row.blocks.forEach {
                    it.writeBackIfDirty(row.rowIndexBinStr)
                }
            }
        }

        override fun toString(): String {
            return """
                CacheModel
                - instanceSize:   $instanceSize
                - addrSize:       $addrSize
                - tagBits:        $tagBits
                - indexBits:      $indexBits
                - offsetBits:     $offsetBits
                - blockCount:     $blockCount
                - replaceAlgo:    $replaceAlgo
            """.trimIndent()
        }

        inner class CacheRow(val rowIndexBinStr: String) {

            val decider = when (replaceAlgo) {
                ReplaceAlgo.FIFO -> Decider.FIFO(blockCount)
                ReplaceAlgo.LRU -> Decider.LRU(blockCount)
                ReplaceAlgo.RANDOM -> Decider.RANDOM(blockCount)
            }

            val blocks: Array<CacheBlock> = Array(blockCount) {
                CacheBlock(Array(offsetCount) {
                    CacheInstance(Hex(initHex, instanceSize), null)
                })
            }

            /**
             * @return block index on (Hit) and null on (Miss)
             */
            fun compare(tagBinStr: String, rowIndexBinStr: String): Int? {
                if (this.rowIndexBinStr != rowIndexBinStr) return null
                blocks.forEachIndexed { blockIndex, cacheBlock ->
                    if (cacheBlock.compare(tagBinStr)) return blockIndex
                }
                return null
            }

            fun read(blockIndex: Int): Array<Value> {
                decider.read(blockIndex)
                return blocks[blockIndex].read()
            }

            fun write(blockIndex: Int, offsetIndex: Int, vararg values: CacheInstance) {
                decider.write(blockIndex)
                values.forEachIndexed { i, value ->
                    if (offsetIndex + i < offsetCount) {
                        blocks[blockIndex].write(offsetIndex + i, value)
                    } else {
                        console.warn("Invalid offset for cache write! (offset: ${offsetIndex + i})")
                    }
                }
            }

            /**
             * @return Pair of [blockIndex] and [neededWriteBack] -> true (wrote back old block) or false (write back not needed)
             */
            fun fetchBlock(tagBinStr: String): Pair<Int, Boolean> {
                val blockIndex = decider.indexToReplace()
                val addr = Bin((tagBinStr + rowIndexBinStr).padEnd(addrSize.bitWidth, '0'), addrSize).toHex()
                val neededWriteBack = blocks[blockIndex].writeBackIfDirty(rowIndexBinStr)
                val values = backingMemory.loadArray(addr, offsetCount).toCacheInstances(addr)
                blocks[blockIndex] = CacheBlock(values, Bin(tagBinStr))
                decider.fetch(blockIndex)
                return blockIndex to neededWriteBack
            }

            fun clear() {
                for (i in blocks.indices) {
                    blocks[i] = CacheBlock(Array(offsetCount) {
                        CacheInstance(Hex(initHex, instanceSize), null)
                    })
                }
                decider.reset()
            }
        }

        inner class CacheBlock(
            val data: Array<CacheInstance>,
            var tag: Bin? = null
        ) {
            var dirty: Boolean = false
            val valid: Boolean
                get() {
                    return tag != null
                }

            fun compare(tagBinStr: String): Boolean {
                return tag?.rawInput == tagBinStr
            }

            fun read(): Array<Value> = data.map { it.value }.toTypedArray()

            fun write(offsetIndex: Int, value: CacheInstance) {
                data[offsetIndex] = value
                dirty = true
            }

            /**
             * @return returns true if the block needed a write back!
             */
            fun writeBackIfDirty(rowIndexBinStr: String): Boolean {
                val tag = tag
                if (!dirty || tag == null) return false
                val rowAddr = Bin((tag.rawInput + rowIndexBinStr).padEnd(addrSize.bitWidth, '0'), addrSize).toHex()
                backingMemory.storeArray(rowAddr, *data.map { it.value }.toTypedArray(), mark = InstanceType.DATA)
                dirty = false
                return true
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
    }

    data class CacheInstance(val value: Hex, val address: Hex?) {
        override fun toString(): String {
            return value.rawInput
        }
    }

    enum class CacheBlockState(val light: Int, val dark: Int? = null) {
        INVALID(0x777777),
        VALID_CLEAN(0x222222, 0xA0A0A0),
        VALID_DIRTY(0xA0A040)
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