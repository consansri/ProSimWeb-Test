package cengine.util

import emulator.kit.nativeLog

class ByteBuffer(endianness: Endianness, initial: ByteArray = byteArrayOf()) {
    private val data = initial.toMutableList()

    var endianness: Endianness = endianness
        private set

    val size: Int get() = data.size

    companion object {
        fun String.toASCIIByteArray(): ByteArray = this.encodeToByteArray()
        fun ByteArray.toASCIIString(): String = this.decodeToString()
    }


    // -------------------------- CONVERSION

    fun toByteArray(): ByteArray = data.toByteArray()


    // -------------------------- GET

    operator fun get(index: Int): Byte = data[index]

    // BYTE

    fun getByte(index: Int): Byte = get(index)

    fun getUByte(index: Int): UByte = get(index).toUByte()

    // SHORT

    fun getShort(index: Int): Short {
        return when (endianness) {
            Endianness.LITTLE -> ((get(index + 1).toInt() shl 8) or (get(index).toInt() and 0xFF)).toShort()
            Endianness.BIG -> ((get(index).toInt() shl 8) or (get(index + 1).toInt() and 0xFF)).toShort()
        }
    }

    fun getUShort(index: Int): UShort = getShort(index).toUShort()

    // INT

    fun getInt(index: Int): Int {
        return when (endianness) {
            Endianness.LITTLE -> (
                    (get(index + 3).toInt() shl 24) or
                            (get(index + 2).toInt() and 0xFF shl 16) or
                            (get(index + 1).toInt() and 0xFF shl 8) or
                            (get(index).toInt() and 0xFF)
                    )

            Endianness.BIG -> (
                    (get(index).toInt() shl 24) or
                            (get(index + 1).toInt() and 0xFF shl 16) or
                            (get(index + 2).toInt() and 0xFF shl 8) or
                            (get(index + 3).toInt() and 0xFF)
                    )
        }
    }

    fun getUInt(index: Int): UInt = getInt(index).toUInt()

    // LONG

    fun getLong(index: Int): Long {
        return when (endianness) {
            Endianness.LITTLE -> (
                    (get(index + 7).toLong() shl 56) or
                            (get(index + 6).toLong() and 0xFF shl 48) or
                            (get(index + 5).toLong() and 0xFF shl 40) or
                            (get(index + 4).toLong() and 0xFF shl 32) or
                            (get(index + 3).toLong() and 0xFF shl 24) or
                            (get(index + 2).toLong() and 0xFF shl 16) or
                            (get(index + 1).toLong() and 0xFF shl 8) or
                            (get(index).toLong() and 0xFF)
                    )

            Endianness.BIG -> (
                    (get(index).toLong() shl 56) or
                            (get(index + 1).toLong() and 0xFF shl 48) or
                            (get(index + 2).toLong() and 0xFF shl 40) or
                            (get(index + 3).toLong() and 0xFF shl 32) or
                            (get(index + 4).toLong() and 0xFF shl 24) or
                            (get(index + 5).toLong() and 0xFF shl 16) or
                            (get(index + 6).toLong() and 0xFF shl 8) or
                            (get(index + 7).toLong() and 0xFF)
                    )
        }
    }

    fun getULong(index: Int): ULong = getLong(index).toULong()

    // STRING

    fun getZeroTerminated(index: Int): ByteArray {
        val result = mutableListOf<Byte>()
        var currentIndex = index

        while (currentIndex < size && this[currentIndex] != 0.toByte()) {
            result.add(this[currentIndex])
            currentIndex++
        }

        return result.toByteArray()
    }

    // -------------------------- PUT

    // BYTE

    fun put(value: Byte) {
        data.add(value)
    }

    fun put(value: UByte) {
        put(value.toByte())
    }

    // BYTEARRAY

    fun putAll(bytes: ByteArray) {
        data.addAll(bytes.toList())
    }

    fun putAll(bytes: Array<Byte>) {
        data.addAll(bytes.toList())
    }

    // SHORT

    fun put(value: Short) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value.toInt() and 0xFF).toByte())
                data.add(((value.toInt() shr 8) and 0xFF).toByte())
            }

            Endianness.BIG -> {
                data.add(((value.toInt() shr 8) and 0xFF).toByte())
                data.add((value.toInt() and 0xFF).toByte())
            }
        }
    }

    fun put(value: UShort) {
        put(value.toShort())
    }

    // INT

    fun put(value: Int) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value and 0xFF).toByte())
                data.add(((value shr 8) and 0xFF).toByte())
                data.add(((value shr 16) and 0xFF).toByte())
                data.add(((value shr 24) and 0xFF).toByte())
            }

            Endianness.BIG -> {
                data.add(((value shr 24) and 0xFF).toByte())
                data.add(((value shr 16) and 0xFF).toByte())
                data.add(((value shr 8) and 0xFF).toByte())
                data.add((value and 0xFF).toByte())
            }
        }
    }

    fun put(value: UInt) {
        put(value.toInt())
    }

    // LONG

    fun put(value: Long) {
        when (endianness) {
            Endianness.LITTLE -> {
                data.add((value and 0xFF).toByte())
                data.add(((value shr 8) and 0xFF).toByte())
                data.add(((value shr 16) and 0xFF).toByte())
                data.add(((value shr 24) and 0xFF).toByte())
                data.add(((value shr 32) and 0xFF).toByte())
                data.add(((value shr 40) and 0xFF).toByte())
                data.add(((value shr 48) and 0xFF).toByte())
                data.add(((value shr 56) and 0xFF).toByte())
            }

            Endianness.BIG -> {
                data.add(((value shr 56) and 0xFF).toByte())
                data.add(((value shr 48) and 0xFF).toByte())
                data.add(((value shr 40) and 0xFF).toByte())
                data.add(((value shr 32) and 0xFF).toByte())
                data.add(((value shr 24) and 0xFF).toByte())
                data.add(((value shr 16) and 0xFF).toByte())
                data.add(((value shr 8) and 0xFF).toByte())
                data.add((value and 0xFF).toByte())
            }
        }
    }

    fun put(value: ULong) {
        put(value.toLong())
    }

    // -------------------------- SET

    operator fun set(index: Int, byte: Byte) {
        data[index] = byte
    }

    operator fun set(index: Int, value: UByte) {
        this[index] = value.toByte()
    }

    operator fun set(index: Int, short: Short) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (short.toInt() and 0xFF).toByte()
                data[index + 1] = ((short.toInt() shr 8) and 0xFF).toByte()
            }

            Endianness.BIG -> {
                data[index] = ((short.toInt() shr 8) and 0xFF).toByte()
                data[index + 1] = (short.toInt() and 0xFF).toByte()
            }
        }
    }

    operator fun set(index: Int, value: UShort) {
        this[index] = value.toShort()
    }

    operator fun set(index: Int, value: Int) {
        nativeLog("Set ${value.toString(16)} on $index")
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (value and 0xFF).toByte()
                data[index + 1] = ((value shr 8) and 0xFF).toByte()
                data[index + 2] = ((value shr 16) and 0xFF).toByte()
                data[index + 3] = ((value shr 24) and 0xFF).toByte()
            }

            Endianness.BIG -> {
                data[index] = ((value shr 24) and 0xFF).toByte()
                data[index + 1] = ((value shr 16) and 0xFF).toByte()
                data[index + 2] = ((value shr 8) and 0xFF).toByte()
                data[index + 3] = (value and 0xFF).toByte()
            }
        }
    }

    operator fun set(index: Int, value: UInt) {
        this[index] = value.toInt()
    }

    operator fun set(index: Int, value: Long) {
        when (endianness) {
            Endianness.LITTLE -> {
                data[index] = (value and 0xFF).toByte()
                data[index + 1] = ((value shr 8) and 0xFF).toByte()
                data[index + 2] = ((value shr 16) and 0xFF).toByte()
                data[index + 3] = ((value shr 24) and 0xFF).toByte()
                data[index + 4] = ((value shr 32) and 0xFF).toByte()
                data[index + 5] = ((value shr 40) and 0xFF).toByte()
                data[index + 6] = ((value shr 48) and 0xFF).toByte()
                data[index + 7] = ((value shr 56) and 0xFF).toByte()
            }

            Endianness.BIG -> {
                data[index] = ((value shr 56) and 0xFF).toByte()
                data[index + 1] = ((value shr 48) and 0xFF).toByte()
                data[index + 2] = ((value shr 40) and 0xFF).toByte()
                data[index + 3] = ((value shr 32) and 0xFF).toByte()
                data[index + 4] = ((value shr 24) and 0xFF).toByte()
                data[index + 5] = ((value shr 16) and 0xFF).toByte()
                data[index + 6] = ((value shr 8) and 0xFF).toByte()
                data[index + 7] = (value and 0xFF).toByte()
            }
        }
    }


    operator fun set(index: Int, value: ULong) {
        this[index] = value.toLong()
    }

}