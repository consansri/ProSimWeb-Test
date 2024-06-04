package emulator.kit.common.memory

import emulator.kit.common.IConsole
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Tools.toValue
import kotlin.math.pow
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
    val tagBits: Int,
    val offsetBits: Int,
    val blockSize: Int,
    val replAlgo: ReplaceAlgo,
    override val name: String = "Cache (FA)"
) : Cache(backingMemory, console) {

    val decider = getDecider()
    val offsets = 2.toDouble().pow(offsetBits).roundToInt()
    val blocks = Array<FABlock>(blockSize) {
        FABlock()
    }

    override fun accessCache(address: Variable.Value.Hex): Pair<AccessResult, Variable.Value> {
        // Address Calculations
        val addrBinStr = address.toBin().toRawString()
        val tagBinStr = addrBinStr.take(tagBits)
        val offsetBinStr = addrBinStr.substring(tagBits)
        val offsetIndex = offsetBinStr.toInt(2)

        // Vadility Check
        if (offsetBinStr.length != offsetBits) throw MemoryException("Offset $offsetBinStr has not the length of $offsetBits. May be caused through invalid address size: ${address.size}.")

        // Search Valid Block
        val block = fetchBlockIndex(tagBinStr)
        if (block != null) {
            // HIT
            decider.read(blocks.indexOf(block))
            return AccessResult(hit = true, writeBack = false) to block.get().data[offsetIndex].value
        }
        // Miss

        // Get Replacement dependent on decider
        val indexToReplace = decider.indexToReplace()
        val blockToReplace = blocks[indexToReplace]

        // Write Back if needed
        val needWB = blockToReplace.isDirty()
        if (needWB) {
            blockToReplace.writeBack()
        }

        // Load Row from backingMemory
        val rowAddress = Variable.Value.Bin(tagBinStr + "0".repeat(offsetBits), addressSize).toHex()
        val data = backingMemory.loadArray(rowAddress, offsets)
        decider.read(indexToReplace)
        return AccessResult(false, needWB) to data[offsetIndex]
    }

    override fun updateCache(address: Variable.Value.Hex, bytes: List<Variable.Value>, mark: InstanceType): AccessResult {
        // Address Calculations
        val addrBinStr = address.toBin().toRawString()
        val tagBinStr = addrBinStr.take(tagBits)
        val offsetBinStr = addrBinStr.substring(tagBits)
        val offsetIndex = offsetBinStr.toInt(2)

        // Vadility Check
        if (offsetBinStr.length != offsetBits) throw MemoryException("Offset $offsetBinStr has not the length of $offsetBits. May be caused through invalid address size: ${address.size}.")
        if (offsetIndex + bytes.size >= offsets) throw MemoryException("${this::class.simpleName.toString()} isn't supporting unaligned access.")

        // Search Valid Block
        val block = fetchBlockIndex(tagBinStr)
        if (block != null) {
            // HIT
            decider.write(blocks.indexOf(block))
            for (i in bytes.indices) {
                block.get().update(CacheInstance(bytes[i], InstanceType.DATA, (address + i.toValue(addressSize)).toHex()), i)
            }
            return AccessResult(hit = true, writeBack = false)
        }
        // Miss

        // Get Replacement dependent on decider
        val indexToReplace = decider.indexToReplace()
        val blockToReplace = blocks[indexToReplace]

        // Write Back if needed
        val needWB = blockToReplace.isDirty()
        if (needWB) {
            blockToReplace.writeBack()
        }

        // Load Row from backingMemory
        val rowAddress = Variable.Value.Bin(tagBinStr + "0".repeat(offsetBits), addressSize).toHex()
        val data = backingMemory.loadArray(rowAddress, offsets)
        decider.write(indexToReplace)
        for (i in bytes.indices) {
            blockToReplace.get().update(CacheInstance(bytes[i], InstanceType.DATA, (address + i.toValue(addressSize)).toHex()), i)
        }
        return AccessResult(false, needWB)
    }

    override fun writeBackAll() {
        blocks.forEach {
            it.get().writeBack()
        }
    }

    override fun getAllBlocks(): Array<CacheBlock> {
        return arrayOf(*blocks)
    }

    override fun clear() {
        blocks.forEach {
            it.clear()
        }
    }

    private fun fetchBlockIndex(tagBinStr: String): FABlock? {
        return blocks.firstOrNull { it.get().tag?.toRawString() == tagBinStr }
    }

    private fun getDecider(): Decider = when (replAlgo) {
        ReplaceAlgo.FIFO -> Decider.FIFO(blockSize)
        ReplaceAlgo.LRU -> Decider.LRU(blockSize)
        ReplaceAlgo.RANDOM -> Decider.RANDOM(blockSize)
    }


    inner class FABlock() : CacheBlock(1, FARow()) {
        fun get(): FARow {
            return data[0] as FARow
        }

        fun set(new: FARow) {
            data[0] = new
        }

        fun isValid() = get().valid
        fun isDirty() = get().dirty

        fun writeBack() {
            get().writeBack()
        }
    }

    inner class FARow(address: Variable.Value.Hex? = null, init: Array<Variable.Value.Bin>? = null) : CacheRow(offsets, backingMemory.instanceSize, address != null) {
        val tag: Variable.Value.Bin?

        init {
            tag = if (address != null) {
                Variable.Value.Bin(address.toBin().toRawString().substring(0, tagBits))
            } else {
                null
            }

            init?.let {
                for (i in init.indices) {
                    val addr = if (address != null) {
                        address + Variable.Value.Hex(i.toString(16), addressSize)
                    } else null
                    data[i] = CacheInstance(init[i], InstanceType.DATA, addr?.toHex())
                }
            }
        }

        fun writeBack() {
            tag?.let {
                val addrBinStr = it.getRawBinStr().padStart(tagBits, '0') + "0".repeat(offsetBits)
                writeBack(Variable.Value.Bin(addrBinStr, addressSize).toHex(), backingMemory)
            }
        }
    }

    sealed class Decider(val size: Int) {
        val range = 0..<size

        abstract fun indexToReplace(): Int
        abstract fun read(index: Int)
        abstract fun write(index: Int)

        class FIFO(size: Int) : Decider(size) {
            private var currentIndex = 0
            override fun indexToReplace(): Int {
                return currentIndex
            }

            override fun read(index: Int) {}

            override fun write(index: Int) {
                currentIndex = (index + 1) % size
            }
        }

        class LRU(size: Int) : Decider(size) {
            private val accessOrder: MutableList<Int> = MutableList<Int>(size) { it }
            override fun indexToReplace(): Int {
                return accessOrder.first()
            }

            override fun read(index: Int) {
                accessOrder.remove(index)
                accessOrder.add(index)
            }

            override fun write(index: Int) {
                read(index)
            }
        }

        class RANDOM(size: Int) : Decider(size) {
            override fun indexToReplace(): Int = range.random()

            override fun read(index: Int) {}

            override fun write(index: Int) {}
        }
    }

    enum class ReplaceAlgo {
        FIFO,
        LRU,
        RANDOM
    }


}