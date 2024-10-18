package cengine.lang.obj.elf

import cengine.util.ByteBuffer
import cengine.util.Endianness
import cengine.util.string.hexDump

data class ELF64_Ehdr(
    override var e_ident: E_IDENT,
    override var e_type: cengine.lang.obj.elf.Elf_Half,
    override var e_machine: cengine.lang.obj.elf.Elf_Half,
    override var e_version: cengine.lang.obj.elf.Elf_Word = Ehdr.EV_CURRENT,
    var e_entry: cengine.lang.obj.elf.Elf64_Addr,
    var e_phoff: cengine.lang.obj.elf.Elf64_Off,
    var e_shoff: cengine.lang.obj.elf.Elf64_Off,
    override var e_flags: cengine.lang.obj.elf.Elf_Word,
    override var e_ehsize: cengine.lang.obj.elf.Elf_Half,
    override var e_phentsize: cengine.lang.obj.elf.Elf_Half,
    override var e_phnum: cengine.lang.obj.elf.Elf_Half,
    override var e_shentsize: cengine.lang.obj.elf.Elf_Half,
    override var e_shnum: cengine.lang.obj.elf.Elf_Half,
    override var e_shstrndx: cengine.lang.obj.elf.Elf_Half
) : Ehdr {

    override fun build(endianness: Endianness): ByteArray {
        val buffer = ByteBuffer(endianness)

        buffer.putAll(e_ident.build(endianness))
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

    override fun byteSize(): Int = e_ident.byteSize() + 48

    override fun toString(): String {
        return "Elf Header:\n" +
                " Magic:                             ${e_ident.build(Endianness.BIG).hexDump()}\n" +
                " Class:                             ${E_IDENT.getElfClass(e_ident.ei_class)}\n" +
                " Data:                              ${E_IDENT.getElfData(e_ident.ei_data)}\n" +
                " Version:                           ${e_ident.ei_version}\n" +
                " OS/ABI:                            ${E_IDENT.getOsAbi(e_ident.ei_osabi)}\n" +
                " ABI Version:                       ${e_ident.ei_abiversion}\n" +
                " Type:                              ${Ehdr.getELFType(e_type)}\n" +
                " Machine:                           ${Ehdr.getELFMachine(e_machine)}\n" +
                " Version:                           $e_version\n" +
                " Entry point address:               $e_entry\n" +
                " Start of program headers:          $e_phoff (bytes into file)\n" +
                " Start of section headers:          $e_shoff (bytes into file)\n" +
                " Flags:                             0x${e_flags.toString(16)}\n" +
                " Size of this header:               $e_ehsize (bytes)\n" +
                " Size of program headers:           $e_phentsize (bytes)\n" +
                " Number of program headers:         $e_phnum\n" +
                " Size of section headers:           $e_shentsize (bytes)\n" +
                " Number of section headers:         $e_shnum\n" +
                " Section header string table index: $e_shstrndx\n"
    }
}