package cengine.util

class ByteBuffer(endianness: Endianness, initial: ByteArray = byteArrayOf()) {
    private val data = initial.toMutableList()

    var endianness: Endianness = endianness
        private set

    val size: Int get() = data.size

    companion object {
        fun String.toASCIIByteArray(): ByteArray = this.encodeToByteArray()
        fun ByteArray.toASCIIString(): String = this.decodeToString()
    }

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


    fun getZeroTerminated(index: Int): ByteArray {
        val result = mutableListOf<Byte>()
        var currentIndex = index

        while (currentIndex < size && this[currentIndex] != 0.toByte()) {
            result.add(this[currentIndex])
            currentIndex++
        }

        return result.toByteArray()
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

    fun toByteArray(): ByteArray = data.toByteArray()

    operator fun get(index: Int): Byte = data[index]

    operator fun set(index: Int, byte: Byte) {
        data[index] = byte
    }

    operator fun set(index: Int, value: UByte){
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

    operator fun set(index: Int, value: UShort){
        this[index] = value.toShort()
    }

    operator fun set(index: Int, value: Int) {
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

    operator fun set(index: Int, value: UInt){
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


    operator fun set(index: Int, value: ULong){
        this[index] = value.toLong()
    }

}