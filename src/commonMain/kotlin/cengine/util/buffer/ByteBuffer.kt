package cengine.util.buffer

import cengine.util.Endianness
import cengine.util.integer.Size
import cengine.util.newint.Int8
import cengine.util.newint.Int8.Companion.toInt8

class ByteBuffer(endianness: Endianness, initial: Array<Byte> = emptyArray()) : Buffer<Byte>(endianness, initial) {

    companion object {
        fun String.toASCIIByteArray(): ByteArray = this.encodeToByteArray()
        fun Array<Byte>.toASCIIString(): String = this.toByteArray().decodeToString()
        fun List<Byte>.toASCIIString(): String = this.toByteArray().decodeToString()
    }

    override val wordWidth: Size get() = Size.Bit8


    // -------------------------- CONVERSION
    override fun toIntList(): List<Int8> = data.map { it.toInt8() }
    override fun toArray(): Array<Byte> = data.toTypedArray()

    override fun pad(length: Int) {
        putAll(Array(length) { 0.toByte() })
    }

    // -------------------------- GET

    // BYTE
    override fun getUByte(index: Int): UByte = data[index].toUByte()

    // SHORT

    override fun getUShort(index: Int): UShort {
        return when (endianness) {
            Endianness.LITTLE -> ((get(index + 1).toUInt() shl 8) or (get(index).toUInt() and 0xFFU)).toUShort()
            Endianness.BIG -> ((get(index).toUInt() shl 8) or (get(index + 1).toUInt() and 0xFFU)).toUShort()
        }
    }

    // INT

    override fun getUInt(index: Int): UInt {
        return when (endianness) {
            Endianness.LITTLE -> (
                    (get(index + 3).toUInt() shl 24) or
                            (get(index + 2).toUInt() and 0xFFU shl 16) or
                            (get(index + 1).toUInt() and 0xFFU shl 8) or
                            (get(index).toUInt() and 0xFFU)
                    )

            Endianness.BIG -> (
                    (get(index).toUInt() shl 24) or
                            (get(index + 1).toUInt() and 0xFFU shl 16) or
                            (get(index + 2).toUInt() and 0xFFU shl 8) or
                            (get(index + 3).toUInt() and 0xFFU)
                    )
        }
    }

    // LONG

    override fun getULong(index: Int): ULong {
        return when (endianness) {
            Endianness.LITTLE -> (
                    (get(index + 7).toULong() shl 56) or
                            (get(index + 6).toULong() and 0xFFU shl 48) or
                            (get(index + 5).toULong() and 0xFFU shl 40) or
                            (get(index + 4).toULong() and 0xFFU shl 32) or
                            (get(index + 3).toULong() and 0xFFU shl 24) or
                            (get(index + 2).toULong() and 0xFFU shl 16) or
                            (get(index + 1).toULong() and 0xFFU shl 8) or
                            (get(index).toULong() and 0xFFU)
                    )

            Endianness.BIG -> (
                    (get(index).toULong() shl 56) or
                            (get(index + 1).toULong() and 0xFFU shl 48) or
                            (get(index + 2).toULong() and 0xFFU shl 40) or
                            (get(index + 3).toULong() and 0xFFU shl 32) or
                            (get(index + 4).toULong() and 0xFFU shl 24) or
                            (get(index + 5).toULong() and 0xFFU shl 16) or
                            (get(index + 6).toULong() and 0xFFU shl 8) or
                            (get(index + 7).toULong() and 0xFFU)
                    )
        }
    }


    // -------------------------- PUT

    fun putAll(array: ByteArray) {
        putAll(array.toTypedArray())
    }

    override fun putBytes(bytes: Array<Byte>) {
        data.addAll(bytes)
    }

    // BYTE

    override fun put(value: UByte) {
        data.add(value.toByte())
    }

    // SHORT

    override fun put(value: UShort) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value.toUInt() and 0xFFU).toByte())
                data.add(((value.toUInt() shr 8) and 0xFFU).toByte())
            }

            Endianness.BIG -> {
                data.add(((value.toInt() shr 8) and 0xFF).toByte())
                data.add((value.toInt() and 0xFF).toByte())
            }
        }
    }

    // INT

    override fun put(value: UInt) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value and 0xFFU).toByte())
                data.add(((value shr 8) and 0xFFU).toByte())
                data.add(((value shr 16) and 0xFFU).toByte())
                data.add(((value shr 24) and 0xFFU).toByte())
            }

            Endianness.BIG -> {
                data.add(((value shr 24) and 0xFFU).toByte())
                data.add(((value shr 16) and 0xFFU).toByte())
                data.add(((value shr 8) and 0xFFU).toByte())
                data.add((value and 0xFFU).toByte())
            }
        }
    }

    // LONG

    override fun put(value: ULong) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value and 0xFFU).toByte())
                data.add(((value shr 8) and 0xFFU).toByte())
                data.add(((value shr 16) and 0xFFU).toByte())
                data.add(((value shr 24) and 0xFFU).toByte())
                data.add(((value shr 32) and 0xFFU).toByte())
                data.add(((value shr 40) and 0xFFU).toByte())
                data.add(((value shr 48) and 0xFFU).toByte())
                data.add(((value shr 56) and 0xFFU).toByte())
            }

            Endianness.BIG -> {
                data.add(((value shr 56) and 0xFFU).toByte())
                data.add(((value shr 48) and 0xFFU).toByte())
                data.add(((value shr 40) and 0xFFU).toByte())
                data.add(((value shr 32) and 0xFFU).toByte())
                data.add(((value shr 24) and 0xFFU).toByte())
                data.add(((value shr 16) and 0xFFU).toByte())
                data.add(((value shr 8) and 0xFFU).toByte())
                data.add((value and 0xFFU).toByte())
            }
        }
    }

    // -------------------------- SET

    override operator fun set(index: Int, value: UByte) {
        data[index] = value.toByte()
    }

    override operator fun set(index: Int, value: UShort) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (value.toUInt() and 0xFFU).toByte()
                data[index + 1] = ((value.toUInt() shr 8) and 0xFFU).toByte()
            }

            Endianness.BIG -> {
                data[index] = ((value.toUInt() shr 8) and 0xFFU).toByte()
                data[index + 1] = (value.toUInt() and 0xFFU).toByte()
            }
        }
    }

    override operator fun set(index: Int, value: UInt) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (value and 0xFFU).toByte()
                data[index + 1] = ((value shr 8) and 0xFFU).toByte()
                data[index + 2] = ((value shr 16) and 0xFFU).toByte()
                data[index + 3] = ((value shr 24) and 0xFFU).toByte()
            }

            Endianness.BIG -> {
                data[index] = ((value shr 24) and 0xFFU).toByte()
                data[index + 1] = ((value shr 16) and 0xFFU).toByte()
                data[index + 2] = ((value shr 8) and 0xFFU).toByte()
                data[index + 3] = (value and 0xFFU).toByte()
            }
        }
    }

    override operator fun set(index: Int, value: ULong) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (value and 0xFFU).toByte()
                data[index + 1] = ((value shr 8) and 0xFFU).toByte()
                data[index + 2] = ((value shr 16) and 0xFFU).toByte()
                data[index + 3] = ((value shr 24) and 0xFFU).toByte()
                data[index + 4] = ((value shr 32) and 0xFFU).toByte()
                data[index + 5] = ((value shr 40) and 0xFFU).toByte()
                data[index + 6] = ((value shr 48) and 0xFFU).toByte()
                data[index + 7] = ((value shr 56) and 0xFFU).toByte()
            }

            Endianness.BIG -> {
                data[index] = ((value shr 56) and 0xFFU).toByte()
                data[index + 1] = ((value shr 48) and 0xFFU).toByte()
                data[index + 2] = ((value shr 40) and 0xFFU).toByte()
                data[index + 3] = ((value shr 32) and 0xFFU).toByte()
                data[index + 4] = ((value shr 24) and 0xFFU).toByte()
                data[index + 5] = ((value shr 16) and 0xFFU).toByte()
                data[index + 6] = ((value shr 8) and 0xFFU).toByte()
                data[index + 7] = (value and 0xFFU).toByte()
            }
        }
    }

    override fun dataAsString(index: Int, radix: Int): String = data[index].toString(radix)

}