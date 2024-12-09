package emulator.kit.memory

import cengine.util.Endianness
import cengine.util.newint.IntNumber

/**
 * Represents a Memory class that provides functionality for loading, storing, and managing memory instances.
 * This class is abstract and provides methods for accessing memory properties and operations.
 *
 * @property name: String, the name of the memory
 * @property addressSize: Size, the size of the memory address
 * @property instanceSize: Size, the size of each memory instance
 * @property initHex: String, the initial binary value of the memory
 *
 * Methods:
 * - globalEndianess(): Endianess, returns the global endianess of the memory
 * - load(address: Value): Value, loads the value at the specified memory address
 * - store(address: Value, value: Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false): Unit,
 *   stores the value at the specified memory address with optional parameters for marking and readonly
 * - clear(): Unit, clears the memory
 * - store(address: Value, variable: Variable, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false): Unit,
 *   stores the value of the variable at the specified memory address with optional parameters for marking and readonly
 * - storeArray(address: Value, vararg values: Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false): Unit,
 *   stores an array of values starting at the specified memory address with optional parameters for marking and readonly
 * - load(address: Value, amount: Int): Bin, loads a specified amount of values starting at the memory address
 * - loadArray(address: Value, amount: Int): Array<Bin>, loads an array of values starting at the memory address
 * - getInitialBinary(): Variable, returns the initial binary value of the memory
 *
 * Nested Classes:
 * -  MemoryException: Exception, represents an exception specific to the Memory class
 *
 * Enums:
 * - Endianess: Represents the endianess of the memory (LittleEndian, BigEndian)
 * - InstanceType: Represents the type of memory instance with light and dark color values
 */
sealed class Memory<ADDR : IntNumber<*>, INSTANCE : IntNumber<*>> {

    abstract val name: String
    abstract val init: INSTANCE

    abstract fun globalEndianess(): Endianness

    /**
     *
     */
    abstract fun loadInstance(address: ADDR, tracker: AccessTracker = AccessTracker()): INSTANCE
    abstract fun storeInstance(address: ADDR, value: INSTANCE, tracker: AccessTracker = AccessTracker())
    abstract fun clear()

    fun storeArray(address: IntNumber<*>, values: Collection<IntNumber<*>>, tracker: AccessTracker = AccessTracker()) {
        storeArray(address.addr(), values.map { it.instance() }, tracker)
    }

    fun storeArray(address: ADDR, values: Collection<INSTANCE>, tracker: AccessTracker = AccessTracker()) {
        var curraddr: IntNumber<*> = address
        for (value in values) {
            storeInstance(curraddr.addr(), value, tracker)
            curraddr = curraddr.inc()
        }
    }

    fun loadArray(address: IntNumber<*>, amount: Int, accessTracker: AccessTracker = AccessTracker()): List<IntNumber<*>>{
        return loadArray(address.addr(), amount, accessTracker).map { it.instance() }
    }

    fun loadArray(address: ADDR, amount: Int, tracker: AccessTracker = AccessTracker()): List<INSTANCE> {
        val instances = mutableListOf<INSTANCE>()

        var instanceAddress: IntNumber<*> = address
        for (i in 0..<amount) {
            val value = loadInstance(instanceAddress.addr(), tracker)
            instances.add(value)
            instanceAddress = instanceAddress.inc()
        }

        return instances.toList()
    }

    protected abstract fun IntNumber<*>.instance(): INSTANCE

    protected abstract fun IntNumber<*>.addr(): ADDR

    class MemoryException(override val message: String) : Exception()

    data class AccessTracker(
        var hits: Int = 0,
        var misses: Int = 0,
        var writeBacks: Int = 0,
    ) {
        override fun toString(): String {
            return "$hits HITS and $misses MISSES (with $writeBacks write backs)"
        }
    }
}