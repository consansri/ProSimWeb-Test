package emulator.kit.common.memory

import emulator.kit.common.IConsole
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*
import kotlin.math.pow
import kotlin.math.roundToInt

abstract class Cache(protected val backingMemory: Memory, val console: IConsole) : Memory() {
    override val initBin: String = "0"
    override val instanceSize: Variable.Size = backingMemory.instanceSize
    override val addressSize: Variable.Size = backingMemory.addressSize

    protected abstract fun accessCache(address: Hex): Pair<AccessResult, Variable.Value>
    protected abstract fun updateCache(address: Hex, value: Variable.Value, mark: InstanceType): AccessResult

    protected abstract fun writeBackAll()

    override fun load(address: Variable.Value): Variable.Value {
        val result = accessCache(address.toHex().getUResized(addressSize))
        console.log("${result.first} for load($address)")
        return result.second
    }

    override fun store(address: Variable.Value, value: Variable.Value, mark: InstanceType, readonly: Boolean) {
        val result = updateCache(address.toHex().getUResized(addressSize), value, mark)
        console.log("${result} for store($address)")
    }

    abstract fun getAllBlocks(): Array<CacheBlock>

    open class CacheBlock(rows: Int, val initialRow: CacheRow) {
        val data: Array<CacheRow> = Array(rows) {
            initialRow
        }

        fun clear() {
            for (i in 0..<data.size) {
                data[i] = initialRow
            }
        }
    }

    open class CacheRow(offsets: Int, instanceSize: Variable.Size, var valid: Boolean) {
        var dirty: Boolean = false
        val data: Array<Variable.Value> = Array(offsets) {
            Bin("0", instanceSize)
        }

        fun update(instance: Variable.Value, index: Int) {
            data[index] = instance
            dirty = true
        }

        fun writeBack(rowAddress: Hex, backingMemory: Memory) {
            backingMemory.storeArray(rowAddress, *data, mark = InstanceType.DATA)
            dirty = false
        }
    }

    data class AccessResult(val hit: Boolean,val valid: Boolean, val dirty: Boolean) {
        override fun toString(): String {
            return "Cache ${if (hit) "HIT" else "MISS"} (valid: $valid, dirty: $dirty)"
        }
    }
}