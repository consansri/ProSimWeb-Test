package cengine.lang.obj.elf

import cengine.util.ByteBuffer
import cengine.util.Endianness

data class ELF64_Shdr(
    override var sh_name: Elf_Word = 0U,
    override var sh_type: Elf_Word = SHT_NULL,
    var sh_flags: Elf_Xword = 0U,
    var sh_addr: Elf64_Addr = 0U,
    var sh_offset: Elf64_Off = 0U,
    var sh_size: Elf_Xword = 0U,
    override var sh_link: Elf_Word = SHN_UNDEF.toUInt(),
    override var sh_info: Elf_Word = 0U,
    var sh_addralign: Elf_Xword = 0U,
    var sh_entsize: Elf_Xword = 0U
) : Shdr() {

    companion object {
        fun create(nameIndex: Elf_Word, type: Elf_Word, flags: Elf_Xword): ELF64_Shdr {
            return ELF64_Shdr(
                sh_name = nameIndex,
                sh_type = type,
                sh_flags = flags
            )
        }
    }

    override fun build(endianness: Endianness): ByteArray {
        val b = ByteBuffer(endianness)

        b.put(sh_name)
        b.put(sh_type)
        b.put(sh_flags)
        b.put(sh_addr)
        b.put(sh_offset)
        b.put(sh_size)
        b.put(sh_link)
        b.put(sh_info)
        b.put(sh_addralign)
        b.put(sh_entsize)

        return b.toByteArray()
    }

    override fun byteSize(): Int = 64
    override fun getEntSize(): Elf_Xword = sh_entsize

    override fun toString(): String = "$sh_name-${getSectionType(sh_type)}-${getSectionFlags(sh_flags)}"
}
