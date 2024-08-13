package cengine.util

class ByteBuffer(private var endianness: Endianness) {
    private val data = mutableListOf<Byte>()

    val size: Int get() = data.size

    fun setEndianess(newEndianess: Endianness) {
        endianness = newEndianess
    }

    fun put(value: Byte) {
        data.add(value)
    }

    fun put(value: UByte) {
        put(value.toByte())
    }

    fun put(bytes: ByteArray) {
        data.addAll(bytes.toList())
    }

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

    fun position(): Int = data.size

    fun toByteArray(): ByteArray = data.toByteArray()
}