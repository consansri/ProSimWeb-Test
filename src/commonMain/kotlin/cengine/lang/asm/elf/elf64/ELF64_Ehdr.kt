package cengine.lang.asm.elf.elf64

import cengine.lang.asm.elf.*
import cengine.util.ByteBuffer
import cengine.util.Endianness

data class ELF64_Ehdr(
    override var e_ident: E_IDENT,
    override var e_type: Elf_Half,
    override var e_machine: Elf_Half,
    override var e_version: Elf_Word,
    var e_entry: Elf64_Addr,
    var e_phoff: Elf64_Off,
    var e_shoff: Elf64_Off,
    override var e_flags: Elf_Word,
    override var e_ehsize: Elf_Half,
    override var e_phentsize: Elf_Half,
    override var e_phnum: Elf_Half,
    override var e_shentsize: Elf_Half,
    override var e_shnum: Elf_Half,
    override var e_shstrndx: Elf_Half
) : Ehdr {
    override fun build(endianness: Endianness): ByteArray {
        val buffer = ByteBuffer(endianness)

        buffer.put(e_ident.build(endianness))
        buffer.put(e_type)
        buffer.put(e_machine)
        buffer.put(e_version)
        buffer.put(e_entry)
        buffer.put(e_phoff)
        buffer.put(e_shoff)
        buffer.put(e_flags)
        buffer.put(e_ehsize)
        buffer.put(e_phentsize)
        buffer.put(e_phnum)
        buffer.put(e_shentsize)
        buffer.put(e_shnum)
        buffer.put(e_shstrndx)

        return buffer.toByteArray()
    }


}