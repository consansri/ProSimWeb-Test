package cengine.lang.asm.elf

sealed class ELF32 {
    companion object {
        fun ELF32.size(): Int {
            return when (this) {
                is ADDR -> 4
                is HALF -> 2
                is OFF -> 4
                is Sword -> 4
                is Word -> 4
                is UnsignedChar -> 1
            }
        }

        fun ELF32.alignment(): Int {
            return when (this) {
                is ADDR -> 4
                is HALF -> 2
                is OFF -> 4
                is Sword -> 4
                is UnsignedChar -> 4
                is Word -> 1
            }
        }
    }


    class ADDR : ELF32()

    class HALF : ELF32()

    class OFF : ELF32()

    class Sword : ELF32()

    class Word : ELF32()

    class UnsignedChar : ELF32()

}