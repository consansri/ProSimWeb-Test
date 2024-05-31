package emulator.kit.common.memory

import emulator.kit.common.IConsole
import emulator.kit.nativeLog
import emulator.kit.types.Variable
import kotlin.math.pow
import kotlin.math.roundToInt

class DirectMappedCache(
    backingMemory: Memory,
    console: IConsole,
    val tagBits: Int,
    val rowBits: Int,
    val offsetBits: Int
) : Cache(backingMemory, console) {

    val offsets = 2.toDouble().pow(offsetBits).roundToInt()
    val rows = 2.toDouble().pow(rowBits).roundToInt()
    val block = DMBlock()

    init {
        if (offsetBits + tagBits + rowBits != addressSize.bitWidth) throw Exception("Direct Mapped Cache expects a valid combination of tag, row and offset widths.")
    }

    override fun accessCache(address: Variable.Value.Hex): Pair<AccessResult, Variable.Value> {
        val binStr = address.toBin().getRawBinStr()
        val binTag = Variable.Value.Bin(binStr.substring(0, tagBits))
        val binRow = Variable.Value.Bin(binStr.substring(tagBits, tagBits + rowBits), Variable.Size.Bit32())
        val binOffset = Variable.Value.Bin(binStr.substring(tagBits + rowBits), Variable.Size.Bit32())

        val rowIndex = binRow.toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate row index ($binRow)!")
        val offset = binOffset.toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate offset index ($binOffset)!")

        val result = checkRow(rowIndex, binTag)

        // Read if HIT
        if (result.second.hit) return result.second to result.first.data[offset].value

        // Write Back if Dirty
        if (result.second.dirty) result.first.writeBack(binRow.toRawString())

        val rowAddress = Variable.Value.Bin(binStr.substring(0, tagBits + rowBits) + "0".repeat(offsetBits), addressSize).toHex()
        return result.second to fetchRow(rowAddress).data[offset].value
    }

    override fun updateCache(address: Variable.Value.Hex, bytes: List<Variable.Value>, mark: InstanceType): AccessResult {
        val binStr = address.toBin().getRawBinStr()
        val binTag = Variable.Value.Bin(binStr.substring(0, tagBits))
        val binRow = Variable.Value.Bin(binStr.substring(tagBits, tagBits + rowBits), Variable.Size.Bit32())
        val binOffset = Variable.Value.Bin(binStr.substring(tagBits + rowBits), Variable.Size.Bit32())

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
        if (result.second.dirty) result.first.writeBack(rowIndex.toString(2))

        // Load and Update Row
        val rowAddress = Variable.Value.Bin(binStr.substring(0, tagBits + rowBits) + "0".repeat(offsetBits), addressSize).toHex()
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

    private fun checkRow(rowIndex: Int, binTag: Variable.Value.Bin): Pair<DMRow, AccessResult> {
        val row = block.data.getOrNull(rowIndex) as? DMRow ?: throw Exception("Direct Mapped Cache row index ($rowIndex) out of Bounds (size ${block.data.size})!")
        return row to row.check(binTag)
    }

    private fun updateRow(address: Variable.Value.Hex, rowIndex: Int, offset: Int, new: List<Variable.Value>, mark: InstanceType) {
        new.forEachIndexed() { i, newVal ->
            val addr = address + Variable.Value.Hex(i.toString(16), addressSize)
            if (offset + i < offsets) {
                block.data[rowIndex].update(CacheInstance(newVal, mark, addr.toHex()), offset + i)
            }
        }
    }

    private fun fetchRow(rowAddress: Variable.Value.Hex): DMRow {
        val loaded = backingMemory.loadArray(rowAddress, offsets)
        val newRow = DMRow(rowAddress, loaded)
        val rowIndex = Variable.Value.Bin(rowAddress.toBin().toRawString().substring(tagBits, tagBits + rowBits), Variable.Size.Bit32()).toDec().toIntOrNull() ?: throw MemoryException(this, "Direct Mapped Cache couldn't calculate row index ($rowAddress)")
        block.data[rowIndex] = newRow
        return newRow
    }

    inner class DMBlock() : CacheBlock(rows, DMRow()) {
        fun writeBack() {
            data.forEachIndexed { index, cacheRow ->
                val row = index.toString(2).padStart(rowBits, '0')

                if (row.length != rowBits) throw MemoryException(this@DirectMappedCache, "$row is not of length $rowBits")

                if (cacheRow !is DMRow) throw MemoryException(this@DirectMappedCache, "$cacheRow is not of type ${DMRow::class.simpleName}")

                cacheRow.writeBack(row)
            }
        }
    }

    inner class DMRow(address: Variable.Value.Hex? = null, init: Array<Variable.Value.Bin>? = null) : CacheRow(offsets, instanceSize, address != null) {
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

        fun check(otherTag: Variable.Value.Bin): AccessResult {
            return when (tag?.getRawBinStr()) {
                otherTag.getRawBinStr() -> AccessResult(true, valid, dirty)
                else -> AccessResult(false, valid, dirty)
            }
        }

        fun writeBack(row: String) {
            tag?.let {
                if (row.length > rowBits) throw MemoryException(this@DirectMappedCache, "$row exceeds $rowBits bits")
                val addrBinStr = it.getRawBinStr().padStart(tagBits, '0') + row.padStart(rowBits, '0') + "0".repeat(offsetBits)
                writeBack(Variable.Value.Bin(addrBinStr, addressSize).toHex(), backingMemory)
            }
        }
    }

}