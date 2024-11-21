package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Hex
import cengine.util.integer.Size

abstract class Buffer<T : Comparable<*>>(endianness: Endianness, initial: Array<T>) : Collection<T> {

    protected val data: MutableList<T> = initial.toMutableList()
    abstract val wordWidth: Size
    var endianness: Endianness = endianness
        private set

    override val size: Int get() = data.size
    override fun iterator(): Iterator<T> = data.iterator()
    override fun contains(element: T): Boolean = data.contains(element)
    override fun isEmpty(): Boolean = data.isEmpty()
    override fun containsAll(elements: Collection<T>): Boolean = data.containsAll(elements)

    // -------------------------- CONVERSION

    abstract fun toHexList(): List<Hex>

    abstract fun toArray(): Array<T>

    // -------------------------- GET

    operator fun get(index: Int): T = data[index]

    // BYTE

    fun getByte(index: Int): Byte = getUByte(index).toByte()

    abstract fun getUByte(index: Int): UByte

    // SHORT
    fun getShort(index: Int): Short = getUShort(index).toShort()

    abstract fun getUShort(index: Int): UShort

    // INT

    fun getInt(index: Int): Int = getUInt(index).toInt()

    abstract fun getUInt(index: Int): UInt

    // LONG

    fun getLong(index: Int): Long = getULong(index).toLong()

    abstract fun getULong(index: Int): ULong

    // STRING

    fun getZeroTerminated(index: Int): List<T> {
        val result = mutableListOf<T>()
        var currentIndex = index

        while (currentIndex < size && this[currentIndex] != 0) {
            result.add(get(currentIndex))
            currentIndex++
        }

        return result
    }

    // -------------------------- PUT

    abstract fun pad(length: Int)

    // BYTEARRAY

    fun putAll(bytes: Array<T>) {
        data.addAll(bytes.toList())
    }

    abstract fun putBytes(bytes: Array<Byte>)

    fun putBytes(bytes: ByteArray) {
        putBytes(bytes.toTypedArray())
    }

    // BYTE

    fun put(value: Byte) {
        put(value.toUByte())
    }

    abstract fun put(value: UByte)

    // SHORT

    fun put(value: Short) {
        put(value.toUShort())
    }

    abstract fun put(value: UShort)

    // INT

    fun put(value: Int) {
        put(value.toUInt())
    }

    abstract fun put(value: UInt)

    // LONG

    fun put(value: Long) {
        put(value.toULong())
    }

    abstract fun put(value: ULong)

    // -------------------------- SET

    operator fun set(index: Int, value: Byte) {
        this[index] = value.toUByte()
    }

    abstract operator fun set(index: Int, value: UByte)

    operator fun set(index: Int, value: Short) {
        this[index] = value.toUShort()
    }

    abstract operator fun set(index: Int, value: UShort)

    operator fun set(index: Int, value: Int) {
        this[index] = value.toUInt()
    }

    abstract operator fun set(index: Int, value: UInt)

    operator fun set(index: Int, value: Long) {
        this[index] = value.toULong()
    }

    abstract operator fun set(index: Int, value: ULong)

    abstract fun dataAsString(index: Int, radix: Int): String

    fun mapAsString(radix: Int) = indices.map { index -> dataAsString(index, radix) }
}