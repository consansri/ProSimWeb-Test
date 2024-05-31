package emulator.kit.common.memory

import emulator.kit.common.IConsole
import emulator.kit.nativeLog
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.*

sealed class Cache(protected val backingMemory: Memory, val console: IConsole) : Memory() {
    override val initBin: String = "0"
    override val instanceSize: Variable.Size = backingMemory.instanceSize
    override val addressSize: Variable.Size = backingMemory.addressSize

    protected abstract fun accessCache(address: Hex): Pair<AccessResult, Variable.Value>
    protected abstract fun updateCache(address: Hex, values: List<Variable.Value>, mark: InstanceType): AccessResult

    protected abstract fun writeBackAll()

    override fun load(address: Variable.Value): Variable.Value {

        val result = accessCache(address.toHex().getUResized(addressSize))
        console.log("${result.first} for load($address)")
        return result.second
    }

    override fun store(address: Variable.Value, value: Variable.Value, mark: InstanceType, readonly: Boolean) {
        val hexValue = value.toHex()
        val hexAddress: Variable.Value = address.toHex().getUResized(addressSize)

        val bytes = if (endianess == Endianess.LittleEndian) hexValue.splitToByteArray().reversed() else hexValue.splitToByteArray().toList()

        val result = updateCache(hexAddress.toHex(), bytes, mark)

        console.log("$result for store($address)")
    }

    abstract fun getAllBlocks(): Array<CacheBlock>

    abstract fun getAddress(row: CacheRow, index: Int): Hex

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
        val data: Array<CacheInstance> = Array(offsets) {
             CacheInstance(Bin("0", instanceSize))
        }

        fun update(instance: CacheInstance, index: Int) {
            data[index] = instance
            dirty = true
        }

        fun getRowState(): CacheRowState{
            if(!valid) return CacheRowState.INVALID
            if(!dirty) return CacheRowState.VALID_CLEAN
            return CacheRowState.VALID_DIRTY
        }

        fun writeBack(rowAddress: Hex, backingMemory: Memory) {
            if (valid) {
                backingMemory.storeArray(rowAddress, *data.map { it.value }.toTypedArray(), mark = InstanceType.DATA)
                dirty = false
            }
        }
    }

    data class CacheInstance(val value: Variable.Value, val mark: InstanceType = InstanceType.NOTUSED,val address: Hex? = null)

    enum class CacheRowState(val light: Int, val dark: Int? = null) {
        INVALID(0x777777),
        VALID_CLEAN(0x222222, 0xA0A0A0),
        VALID_DIRTY(0xA0A040)
    }

    data class AccessResult(val hit: Boolean, val valid: Boolean, val dirty: Boolean) {
        override fun toString(): String {
            return "Cache ${if (hit) "HIT" else "MISS"} (valid: $valid, dirty: $dirty)"
        }
    }
}