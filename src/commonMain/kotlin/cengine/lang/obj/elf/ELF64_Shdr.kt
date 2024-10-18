package cengine.lang.obj.elf

import cengine.util.ByteBuffer
import cengine.util.Endianness

data class ELF64_Shdr(
    override var sh_name: cengine.lang.obj.elf.Elf_Word = 0U,
    override var sh_type: cengine.lang.obj.elf.Elf_Word = Shdr.SHT_NULL,
    var sh_flags: cengine.lang.obj.elf.Elf_Xword = 0U,
    var sh_addr: cengine.lang.obj.elf.Elf64_Addr = 0U,
    var sh_offset: cengine.lang.obj.elf.Elf64_Off = 0U,
    var sh_size: cengine.lang.obj.elf.Elf_Xword = 0U,
    override var sh_link: cengine.lang.obj.elf.Elf_Word = Shdr.SHN_UNDEF.toUInt(),
    override var sh_info: cengine.lang.obj.elf.Elf_Word = 0U,
    var sh_addralign: cengine.lang.obj.elf.Elf_Xword = 0U,
    var sh_entsize: cengine.lang.obj.elf.Elf_Xword = 0U
) : Shdr() {

    companion object {
        fun create(nameIndex: cengine.lang.obj.elf.Elf_Word, type: cengine.lang.obj.elf.Elf_Word, flags: cengine.lang.obj.elf.Elf_Xword): ELF64_Shdr {
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
    override fun getEntSize(): cengine.lang.obj.elf.Elf_Xword = sh_entsize

    override fun toString(): String = "$sh_name-${Shdr.getSectionType(sh_type)}-${Shdr.getSectionFlags(sh_flags)}"
}
