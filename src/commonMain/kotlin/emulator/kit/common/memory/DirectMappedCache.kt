package emulator.kit.common.memory

import emulator.kit.common.IConsole
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
        if (result.second.valid) return result.second to result.first.data[offset]

        // Write Back if Dirty
        if (result.second.dirty) result.first.writeBack(binRow.toRawString())

        val rowAddress = Variable.Value.Bin(binStr.substring(0, tagBits + rowBits) + "0".repeat(offsetBits), addressSize).toHex()
        return result.second to fetchRow(rowAddress).data[offset]
    }

    override fun updateCache(address: Variable.Value.Hex, value: Variable.Value, mark: InstanceType): AccessResult {
        val binStr = address.toBin().getRawBinStr()
        val binTag = Variable.Value.Bin(binStr.substring(0, tagBits))
        val binRow = Variable.Value.Bin(binStr.substring(tagBits, tagBits + rowBits), Variable.Size.Bit32())
        val binOffset = Variable.Value.Bin(binStr.substring(tagBits + rowBits), Variable.Size.Bit32())

        val rowIndex = binRow.toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate row index ($binRow)!")
        val offset = binOffset.toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate offset index ($binOffset)!")

        val result = checkRow(rowIndex, binTag)

        // Update if HIT
        if (result.second.valid) {
            result.first.update(value, offset)
            return result.second
        }

        // Write Back if Dirty
        if (result.second.dirty) result.first.writeBack(binRow.toRawString())

        // Load and Update Row
        val rowAddress = Variable.Value.Bin(binStr.substring(0, tagBits + rowBits) + "0".repeat(offsetBits), addressSize).toHex()
        fetchRow(rowAddress).update(value, offset)
        return result.second
    }

    override fun getAllBlocks(): Array<CacheBlock> = arrayOf(block)

    override fun clear() {
        block.clear()
    }

    private fun checkRow(rowIndex: Int, binTag: Variable.Value.Bin): Pair<DMRow, AccessResult> {
        val row = block.data.getOrNull(rowIndex) as? DMRow ?: throw Exception("Direct Mapped Cache row index ($rowIndex) out of Bounds (size ${block.data.size})!")
        return row to row.check(binTag)
    }

    private fun fetchRow(rowAddress: Variable.Value.Hex): DMRow {
        val loaded = backingMemory.loadArray(rowAddress, offsets)
        val newRow = DMRow(rowAddress, loaded)
        val rowIndex = Variable.Value.Bin(rowAddress.toBin().toRawString().substring(tagBits, tagBits + rowBits), Variable.Size.Bit32()).toDec().toIntOrNull() ?: throw Exception("Direct Mapped Cache couldn't calculate row index ($rowAddress)!")
        block.data[rowIndex] = newRow
        return newRow
    }

    inner class DMBlock() : CacheBlock(rows, DMRow())
    inner class DMRow(address: Variable.Value.Hex? = null, init: Array<Variable.Value.Bin>? = null) : CacheRow(offsets, instanceSize) {
        val tag: Variable.Value.Bin?

        init {
            tag = if (address != null) {
                Variable.Value.Bin(address.toBin().toRawString().substring(0, tagBits))
            } else {
                null
            }
            init?.let {
                for (i in init.indices) {
                    data[i] = init[i]
                }
            }
        }

        fun check(otherTag: Variable.Value.Bin): AccessResult {
            return when (tag?.getRawBinStr()) {
                otherTag.getRawBinStr() -> AccessResult(true, dirty)
                else -> AccessResult(false, dirty)
            }
        }

        fun writeBack(row: String) {
            tag?.let {
                writeBack(Variable.Value.Bin(it.getRawBinStr() + row + "0".repeat(offsetBits), addressSize).toHex(), backingMemory)
            }
        }
    }

}