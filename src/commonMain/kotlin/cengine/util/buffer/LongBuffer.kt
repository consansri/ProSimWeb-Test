package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Size
import cengine.util.newint.Int64
import cengine.util.newint.Int64.Companion.toInt64

class LongBuffer(endianness: Endianness, initial: Array<Long> = emptyArray()) : Buffer<Long>(endianness, initial) {

    override val wordWidth: Size get() = Size.Bit64

    override fun toIntList(): List<Int64> = data.map { it.toInt64() }
    override fun toArray(): Array<Long> = data.toTypedArray()

    override fun getUByte(index: Int): UByte = data[index].toUByte()

    override fun getUShort(index: Int): UShort = data[index].toUShort()

    override fun getUInt(index: Int): UInt = data[index].toUInt()

    override fun getULong(index: Int): ULong = data[index].toULong()

    override fun pad(length: Int) {
        data.addAll(Array(length) { 0L })
    }

    override fun putBytes(bytes: Array<Byte>) {
        val longs = bytes.toList().chunked(8).map {
            val i0 = it.getOrNull(0)?.toULong() ?: 0U
            val i1 = it.getOrNull(1)?.toULong() ?: 0U
            val i2 = it.getOrNull(2)?.toULong() ?: 0U
            val i3 = it.getOrNull(3)?.toULong() ?: 0U
            val i4 = it.getOrNull(4)?.toULong() ?: 0U
            val i5 = it.getOrNull(5)?.toULong() ?: 0U
            val i6 = it.getOrNull(6)?.toULong() ?: 0U
            val i7 = it.getOrNull(7)?.toULong() ?: 0U
            when (endianness) {
                Endianness.BIG -> {
                    i0 shl 56 or i1 shl 48 or i2 shl 40 or i3 shl 32 or i4 shl 24 or i5 shl 16 or i6 shl 8 or i7
                }

                Endianness.LITTLE -> {
                    i7 shl 56 or i6 shl 48 or i5 shl 40 or i4 shl 32 or i3 shl 24 or i2 shl 16 or i1 shl 8 or i0
                }
            }.toLong()
        }
        putAll(longs.toTypedArray())
    }

    override fun put(value: UByte) {
        data.add(value.toLong())
    }

    override fun put(value: UShort) {
        data.add(value.toLong())
    }

    override fun put(value: UInt) {
        data.add(value.toLong())
    }

    override fun put(value: ULong) {
        data.add(value.toLong())
    }

    override fun set(index: Int, value: UByte) {
        data[index] = value.toLong()
    }

    override fun set(index: Int, value: UShort) {
        data[index] = value.toLong()
    }

    override fun set(index: Int, value: UInt) {
        data[index] = value.toLong()
    }

    override fun set(index: Int, value: ULong) {
        data[index] = value.toLong()
    }

    override fun dataAsString(index: Int, radix: Int): String = data[index].toString(radix)
}