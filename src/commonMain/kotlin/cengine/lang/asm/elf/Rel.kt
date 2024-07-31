package cengine.lang.asm.elf

interface Rel: BinaryProvider {

    companion object {
        fun ELF32_R_SYM(i: Elf_Word) = i.shr(8)
        fun ELF32_R_TYPE(i: Elf_Word) = i.toUByte()
        fun ELF32_R_INFO(s: Elf_Word, t: Elf_Word) = s.shl(8) + t.toUByte()
    }

}