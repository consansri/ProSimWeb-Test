package emulator.kit.common.memory

enum class CacheSize(val bytes: Long, val uiName: String) {
    KiloByte_1(1024, "1 KB"),
    KiloByte_2(2048, "2 KB"),
    KiloByte_4(4096,"4 KB"),
    KiloByte_8(8192, "8 KB"),
    KiloByte_16(16384,"16 KB"),
    KiloByte_32(32768, "32 KB"),
    KiloByte_64(65536, "64 KB"),
    KiloByte_128(131072, "128 KB"),
    KiloByte_256(262144, "256 KB"),
    KiloByte_512(524288, "512 KB"),
    MegaByte_1(1048576, "1 MB");

    override fun toString(): String {
        return uiName
    }

    companion object{
        val BYTECOUNT_IN_ROW = 16
    }

}