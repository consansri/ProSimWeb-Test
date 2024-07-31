package cengine.lang.asm.elf.elf64

import cengine.lang.asm.elf.*

data class ELF64_Ehdr(
    override var e_ident: E_IDENT,
    override var e_type: Elf_Half,
    override var e_machine: Elf_Half,
    override var e_version: Elf_Word,
    override var e_entry: Elf64_Addr,
    override var e_phoff: Elf64_Off,
    override var e_shoff: Elf64_Off,
    override var e_flags: Elf_Word,
    override var e_ehsize: Elf_Half,
    override var e_phentsize: Elf_Half,
    override var e_phnum: Elf_Half,
    override var e_shentsize: Elf_Half,
    override var e_shnum: Elf_Half,
    override var e_shstrndx: Elf_Half
) : Ehdr<Elf64_Addr, Elf64_Off> {


    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }
}