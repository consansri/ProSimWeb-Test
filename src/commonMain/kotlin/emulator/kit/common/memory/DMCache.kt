package emulator.kit.common.memory

import emulator.kit.common.IConsole
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Size.Bit32
import emulator.kit.types.Variable.Value.Bin
import emulator.kit.types.Variable.Value.Hex
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Represents a direct-mapped cache implementation that extends the Cache class.
 *
 * @property backingMemory The backing memory for the cache.
 * @property console The console for logging messages.
 * @property tagBits The number of bits used for tag in the cache address.
 * @property rowBits The number of bits used for row in the cache address.
 * @property offsetBits The number of bits used for offset in the cache address.
 * @property name The name of the direct-mapped cache.
 *
 * @constructor Creates a DirectMappedCache with the specified parameters.
 *
 * @throws Exception if the combination of tag, row, and offset widths does not match the address size.
 *
 * @see Cache
 */
class DMCache(
    backingMemory: Memory,
    console: IConsole,
    val tagBits: Int,
    val rowBits: Int,
    val offsetBits: Int,
    override val name: String = "Cache (DM)"
) : Cache( backingMemory, console) {

    val offsets = 2.toDouble().pow(offsetBits).roundToInt()
    val rows = 2.toDouble().pow(rowBits).roundToInt()
    val block = DMBlock()

    init {
        if (offsetBits + tagBits + rowBits != addressSize.bitWidth) throw Exception("Direct Mapped Cache expects a valid combination of tag, row and offset widths.")
    }

    override fun accessCache(address: Hex): Pair<AccessResult, Variable.Value> {
        val binStr = address.toBin().getRawBinStr()
        val binTag = Bin(binStr.substring(0, tagBits))
        val binRow = Bin(binStr.substring(tagBits, tagBits + rowBits), Bit32())
        val binOffset = Bin(binStr.substring(tagBits + rowBits), Bit32())

        val rowIndex = binRow.toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate row index ($binRow)!")
        val offset = binOffset.toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate offset index ($binOffset)!")

        val result = checkRow(rowIndex, binTag)

        // Read if HIT
        if (result.second.hit) return result.second to result.first.data[offset].value

        // Write Back if Dirty
        if (result.second.writeBack) result.first.writeBack(rowIndex.toString(2))

        val rowAddress = Bin(binStr.substring(0, tagBits + rowBits) + "0".repeat(offsetBits), addressSize).toHex()
        return result.second to fetchRow(rowAddress).data[offset].value
    }

    override fun updateCache(address: Hex, bytes: List<Variable.Value>, mark: InstanceType): AccessResult {
        val binStr = address.toBin().getRawBinStr()
        val binTag = Bin(binStr.substring(0, tagBits))
        val binRow = Bin(binStr.substring(tagBits, tagBits + rowBits), Bit32())
        val binOffset = Bin(binStr.substring(tagBits + rowBits), Bit32())

        val rowIndex = binRow.toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate row index ($binRow)!")
        val offset = binOffset.toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate offset index ($binOffset)!")

        val result = checkRow(rowIndex, binTag)

        if (offset + bytes.size - 1 >= offsets) console.warn("Unsupported store above multiple cache rows!")

        // Update if HIT
        if (result.second.hit) {
            updateRow(address, rowIndex, offset, bytes, mark)
            return result.second
        }

        // Write Back if Dirty
        if (result.second.writeBack) result.first.writeBack(rowIndex.toString(2))

        // Load and Update Row
        val rowAddress = Bin(binStr.substring(0, tagBits + rowBits) + "0".repeat(offsetBits), addressSize).toHex()
        fetchRow(rowAddress)
        updateRow(address, rowIndex, offset, bytes, mark)
        return result.second
    }

    override fun writeBackAll() {
        block.writeBack()
    }

    override fun getAllBlocks(): Array<CacheBlock> = arrayOf(block)

    override fun clear() {
        block.clear()
    }

    private fun checkRow(rowIndex: Int, binTag: Bin): Pair<DMRow, AccessResult> {
        val row = block.data.getOrNull(rowIndex) as? DMRow ?: throw Exception("Direct Mapped Cache row index ($rowIndex) out of Bounds (size ${block.data.size})!")
        return row to row.check(binTag)
    }

    private fun updateRow(address: Hex, rowIndex: Int, offset: Int, new: List<Variable.Value>, mark: InstanceType) {
        new.forEachIndexed { i, newVal ->
            val addr = address + Hex(i.toString(16), addressSize)
            if (offset + i < offsets) {
                block.data[rowIndex].update(CacheInstance(newVal, mark, addr.toHex()), offset + i)
            }
        }
    }

    private fun fetchRow(rowAddress: Hex): DMRow {
        val loaded = backingMemory.loadArray(rowAddress, offsets)
        val newRow = DMRow(rowAddress, loaded)
        val rowIndex = Bin(rowAddress.toBin().toRawString().substring(tagBits, tagBits + rowBits), Bit32()).toDec().toIntOrNull() ?: throw MemoryException("Direct Mapped Cache couldn't calculate row index ($rowAddress)")
        block.data[rowIndex] = newRow
        return newRow
    }

    inner class DMBlock : CacheBlock(rows, DMRow()) {
        fun writeBack() {
            data.forEachIndexed { index, cacheRow ->
                val row = index.toString(2).padStart(rowBits, '0')

                if (row.length != rowBits) throw MemoryException("$row is not of length $rowBits")

                if (cacheRow !is DMRow) throw MemoryException("$cacheRow is not of type ${DMRow::class.simpleName}")

                cacheRow.writeBack(row)
            }
        }
    }

    inner class DMRow(address: Hex? = null, init: Array<Bin>? = null) : CacheRow(offsets, instanceSize, address != null) {
        val tag: Bin?

        init {
            tag = if (address != null) {
                Bin(address.toBin().toRawString().substring(0, tagBits))
            } else {
                null
            }
            init?.let {
                for (i in init.indices) {
                    val addr = if (address != null) {
                        address + Hex(i.toString(16), addressSize)
                    } else null
                    data[i] = CacheInstance(init[i], InstanceType.DATA, addr?.toHex())
                }
            }
        }

        fun check(otherTag: Bin): AccessResult {
            return when (tag?.getRawBinStr()) {
                otherTag.getRawBinStr() -> AccessResult(true, false)
                else -> AccessResult(false, dirty)
            }
        }

        fun writeBack(row: String) {
            tag?.let {
                if (row.length > rowBits) throw MemoryException("$row exceeds $rowBits bits")
                val addrBinStr = it.getRawBinStr().padStart(tagBits, '0') + row.padStart(rowBits, '0') + "0".repeat(offsetBits)
                writeBack(Bin(addrBinStr, addressSize).toHex(), backingMemory)
            }
        }
    }

}