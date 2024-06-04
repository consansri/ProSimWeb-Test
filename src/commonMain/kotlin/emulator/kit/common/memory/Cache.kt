package emulator.kit.common.memory

import emulator.kit.common.IConsole
import emulator.kit.types.Variable
import emulator.kit.types.Variable.Value.Bin
import emulator.kit.types.Variable.Value.Hex

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
sealed class Cache(protected val backingMemory: Memory, val console: IConsole) : Memory() {
    override val initBin: String = "0"
    override val instanceSize: Variable.Size = backingMemory.instanceSize
    override val addressSize: Variable.Size = backingMemory.addressSize

    override fun globalEndianess(): Endianess = backingMemory.globalEndianess()

    protected abstract fun accessCache(address: Hex): Pair<AccessResult, Variable.Value>
    protected abstract fun updateCache(address: Hex, bytes: List<Variable.Value>, mark: InstanceType): AccessResult

    protected abstract fun writeBackAll()

    override fun load(address: Variable.Value): Variable.Value {
        val result = accessCache(address.toHex().getUResized(addressSize))
        console.log("${result.first} for load($address)")
        return result.second
    }

    override fun store(address: Variable.Value, value: Variable.Value, mark: InstanceType, readonly: Boolean) {
        val hexValue = value.toHex()
        val hexAddress: Variable.Value = address.toHex().getUResized(addressSize)

        val bytes = if (globalEndianess() == Endianess.LittleEndian) hexValue.splitToByteArray().reversed() else hexValue.splitToByteArray().toList()

        val result = updateCache(hexAddress.toHex(), bytes, mark)

        console.log("$result for store($address)")
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

    data class CacheInstance(val value: Variable.Value, val mark: InstanceType = InstanceType.NOTUSED,val address: Hex? = null){
        override fun toString(): String {
            return value.toHex().toRawString()
        }
    }

    enum class CacheRowState(val light: Int, val dark: Int? = null) {
        INVALID(0x777777),
        VALID_CLEAN(0x222222, 0xA0A0A0),
        VALID_DIRTY(0xA0A040)
    }

    data class AccessResult(val hit: Boolean, val writeBack: Boolean) {
        override fun toString(): String {
            return "Cache ${if (hit) "HIT" else "MISS"} (needed write back: $writeBack)"
        }
    }
}