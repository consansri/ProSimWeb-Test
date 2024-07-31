package cengine.lang.asm.elf.elf64

import cengine.lang.asm.elf.*

data class ELF64_Shdr(
    override var sh_name: Elf_Word = 0U,
    override var sh_type: Elf_Word = Shdr.SHT_NULL,
    var sh_flags: Elf_Xword = 0U,
    var sh_addr: Elf64_Addr = 0U,
    var sh_offset: Elf64_Off = 0U,
    var sh_size: Elf_Xword = 0U,
    override var sh_link: Elf_Word = Shdr.SHN_UNDEF.toUInt(),
    override var sh_info: Elf_Word = 0U,
    var sh_addralign: Elf_Xword = 0U,
    var sh_entsize: Elf_Xword = 0U
): Shdr{
    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }

}
