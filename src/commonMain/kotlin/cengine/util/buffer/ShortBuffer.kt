package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Hex
import cengine.util.integer.Size
import cengine.util.integer.Value.Companion.toValue

class ShortBuffer(endianness: Endianness, initial: Array<Short> = emptyArray()) : Buffer<Short>(endianness, initial) {
    override val wordWidth: Size get() = Size.Bit16

    override fun toHexList(): List<Hex> = data.map { it.toUShort().toValue() }
    override fun toArray(): Array<Short> = data.toTypedArray()

    override fun getUByte(index: Int): UByte = data[index].toUByte()

    override fun getUShort(index: Int): UShort = data[index].toUShort()

    override fun getUInt(index: Int): UInt {
        return when (endianness) {
            Endianness.LITTLE -> get(index + 1).toUInt() shl 16 or get(index).toUInt()
            Endianness.BIG -> get(index).toUInt() shl 16 or get(index + 1).toUInt()
        }
    }

    override fun getULong(index: Int): ULong {
        return when (endianness) {
            Endianness.LITTLE -> get(index + 3).toULong() shl 48 or
                    get(index + 2).toULong() shl 32 or
                    get(index + 1).toULong() shl 16 or
                    get(index).toULong()

            Endianness.BIG -> get(index).toULong() shl 48 or
                    get(index + 2).toULong() shl 32 or
                    get(index + 1).toULong() shl 16 or
                    get(index).toULong()
        }
    }

    override fun pad(length: Int) {
        data.addAll(Array(length) { 0.toShort() })
    }

    override fun putBytes(bytes: Array<Byte>) {
        val shorts = bytes.toList().chunked(2).map {
            val i0 = it.getOrNull(0)?.toUInt() ?: 0U
            val i1 = it.getOrNull(1)?.toUInt() ?: 0U
            when (endianness) {
                Endianness.BIG -> {
                    i0 shl 8 or i1
                }

                Endianness.LITTLE -> {
                    i1 shl 8 or i0
                }
            }.toShort()
        }
        putAll(shorts.toTypedArray())
    }

    override fun put(value: UByte) {
        data.add(value.toShort())
    }

    override fun put(value: UShort) {
        data.add(value.toShort())
    }

    override fun put(value: UInt) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add(value.toShort())
                data.add((value shr 16).toShort())
            }

            Endianness.BIG -> {
                data.add((value shr 16).toShort())
                data.add(value.toShort())
            }
        }
    }

    override fun put(value: ULong) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add(value.toShort())
                data.add((value shr 16).toShort())
                data.add((value shr 32).toShort())
                data.add((value shr 48).toShort())
            }

            Endianness.BIG -> {
                data.add((value shr 48).toShort())
                data.add((value shr 32).toShort())
                data.add((value shr 16).toShort())
                data.add(value.toShort())
            }
        }
    }

    override fun set(index: Int, value: UByte) {
        data[index] = value.toShort()
    }

    override fun set(index: Int, value: UShort) {
        data[index] = value.toShort()
    }

    override fun set(index: Int, value: UInt) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = value.toShort()
                data[index + 1] = (value shr 16).toShort()
            }

            Endianness.BIG -> {
                data[index] = (value shr 16).toShort()
                data[index + 1] = value.toShort()
            }
        }
    }

    override fun set(index: Int, value: ULong) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = value.toShort()
                data[index + 1] = (value shr 16).toShort()
                data[index + 2] = (value shr 32).toShort()
                data[index + 3] = (value shr 48).toShort()
            }

            Endianness.BIG -> {
                data[index] = (value shr 48).toShort()
                data[index + 1] = (value shr 32).toShort()
                data[index + 2] = (value shr 16).toShort()
                data[index + 3] = value.toShort()
            }
        }
    }

    override fun dataAsString(index: Int, radix: Int): String = data[index].toString(radix)
}