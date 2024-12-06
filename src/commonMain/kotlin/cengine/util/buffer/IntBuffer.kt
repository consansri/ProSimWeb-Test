package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Size
import cengine.util.newint.Int32
import cengine.util.newint.Int32.Companion.toInt32

class IntBuffer(endianness: Endianness, initial: Array<Int> = emptyArray()) : Buffer<Int>(endianness, initial) {

    override val wordWidth: Size get() = Size.Bit32

    override fun toIntList(): List<Int32> = data.map { it.toInt32() }
    override fun toArray(): Array<Int> = data.toTypedArray()

    override fun getUByte(index: Int): UByte = data[index].toUByte()

    override fun getUShort(index: Int): UShort = data[index].toUShort()

    override fun getUInt(index: Int): UInt = data[index].toUInt()

    override fun getULong(index: Int): ULong = when (endianness) {
        Endianness.LITTLE -> get(index + 1).toULong() shl 32 or get(index).toULong()
        Endianness.BIG -> get(index).toULong() shl 32 or get(index + 1).toULong()
    }

    override fun pad(length: Int) {
        data.addAll(Array<Int>(length) { 0 })
    }

    override fun putBytes(bytes: Array<Byte>) {
        val ints = bytes.toList().chunked(4).map {
            val i0 = it.getOrNull(0)?.toUInt() ?: 0U
            val i1 = it.getOrNull(1)?.toUInt() ?: 0U
            val i2 = it.getOrNull(2)?.toUInt() ?: 0U
            val i3 = it.getOrNull(3)?.toUInt() ?: 0U

            when (endianness) {
                Endianness.BIG -> {
                    i0 shl 24 or i1 shl 16 or i2 shl 8 or i3
                }

                Endianness.LITTLE -> {
                    i3 shl 24 or i2 shl 16 or i1 shl 8 or i0
                }
            }.toInt()
        }
        putAll(ints.toTypedArray())
    }

    override fun put(value: UByte) {
        data.add(value.toInt())
    }

    override fun put(value: UShort) {
        data.add(value.toInt())
    }

    override fun put(value: UInt) {
        data.add(value.toInt())
    }

    override fun put(value: ULong) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add(value.toInt())
                data.add((value shr 32).toInt())
            }

            Endianness.BIG -> {
                data.add((value shr 32).toInt())
                data.add(value.toInt())
            }
        }
    }

    override fun set(index: Int, value: UByte) {
        data[index] = value.toInt()
    }

    override fun set(index: Int, value: UShort) {
        data[index] = value.toInt()
    }

    override fun set(index: Int, value: UInt) {
        data[index] = value.toInt()
    }

    override fun set(index: Int, value: ULong) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = value.toInt()
                data[index + 1] = (value shr 32).toInt()
            }

            Endianness.BIG -> {
                data[index] = (value shr 32).toInt()
                data[index + 1] = value.toInt()
            }
        }
    }

    override fun dataAsString(index: Int, radix: Int): String = data[index].toString(radix)
}