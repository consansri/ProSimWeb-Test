package emulator.kit.memory

import cengine.util.integer.Size
import cengine.util.integer.Value
import emulator.core.*
import cengine.util.integer.Size.Bit8
import cengine.util.integer.*

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
sealed class Memory {

    abstract val name: String
    abstract val addressSize: Size
    abstract val instanceSize: Size
    abstract val initHex: String

    abstract fun globalEndianess(): Endianess

    abstract fun load(address: Hex, amount: Int = 1, tracker: AccessTracker = AccessTracker(), endianess: Endianess = globalEndianess()): Hex
    abstract fun store(address: Hex, value: Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false, tracker: AccessTracker = AccessTracker(), endianess: Endianess = globalEndianess())
    abstract fun clear()

    fun storeArray(address: Hex, vararg values: Value, mark: InstanceType = InstanceType.ELSE, readonly: Boolean = false, tracker: AccessTracker = AccessTracker()) {
        var curraddr: Value = address
        for (value in values) {
            store(curraddr.toHex(), value, mark, readonly,tracker)
            curraddr += value.size.getByteCount().toValue(addressSize)
        }
    }

    fun loadArray(address: Hex, amount: Int, tracker: AccessTracker = AccessTracker()): Array<Hex> {
        val instances = mutableListOf<Hex>()

        var instanceAddress = address.toHex().getUResized(addressSize)
        for (i in 0..<amount) {
            val value = load(instanceAddress, 1, tracker).getUResized(instanceSize)
            instances.add(value.toHex())
            instanceAddress = (instanceAddress + Hex("01", Bit8)).toHex()
        }

        return instances.toTypedArray()
    }

    fun getInitialBinary(): Variable {
        return Variable(initHex, instanceSize)
    }

    class MemoryException(override val message: String) : Exception()

    data class AccessTracker(
        var hits: Int = 0,
        var misses: Int = 0,
        var writeBacks: Int = 0
    ){
        override fun toString(): String {
            return "$hits HITS and $misses MISSES (with $writeBacks write backs)"
        }
    }

    enum class Endianess(val uiName: String) {
        LittleEndian("Little Endian"), BigEndian("Big Endian")
    }

    enum class InstanceType(val light: Int, val dark: Int? = null) {
        PROGRAM(0xA040A0),
        DATA(0x40A0A0),
        EDITABLE(0x222222, 0xA0A0A0),
        NOTUSED(0x777777),
        ELSE(0xA0A040);
    }
}