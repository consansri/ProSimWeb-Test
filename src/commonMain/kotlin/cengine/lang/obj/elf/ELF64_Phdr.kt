package cengine.lang.obj.elf

import cengine.lang.obj.elf.*
import cengine.util.ByteBuffer
import cengine.util.Endianness

/**
 * ELF Program Header
 *
 * An executable or shared object file's program header table is an array of structures, each
 * describing a segment or other information the system needs to prepare the program for
 * execution. An object file segment contains one or more sections. Program headers are
 * meaningful only for executable and shared object files. A file specifies its own program header
 * size with the ELF header's [e_phentsize] and  [e_phnum] members.
 *
 * @param p_type This member tells what kind of segment this array element describes or how to
 * interpret the array element's information. Type values and their meanings appear
 * below.
 *
 * @param p_offset This member gives the offset from the beginning of the file at which the first byte
 * of the segment resides.
 *
 * @param p_vaddr This member gives the virtual address at which the first byte of the segment resides
 * in memory.
 *
 * @param p_paddr On systems for which physical addressing is relevant, this member is reserved for
 * the segment's physical address. This member requires operating system specific
 * information, which is described in the appendix at the end of Book III.
 *
 * @param p_filesz This member gives the number of bytes in the file image of the segment; it may be
 * zero.
 *
 * @param p_memsz This member gives the number of bytes in the memory image of the segment; it
 * may be zero.
 *
 * @param p_flags This member gives flags relevant to the segment. Defined flag values appear below.
 *
 * @param p_align Loadable process segments must have congruent values for [p_vaddr] and
 * [p_offset], modulo the page size. This member gives the value to which the
 * segments are aligned in memory and in the file. Values 0 and 1 mean that no
 * alignment is required. Otherwise, [p_align] should be a positive, integral power of
 * 2, and [p_addr] should equal [p_offset], modulo [p_align].
 *
 *
 */
data class ELF64_Phdr(
    override var p_type: cengine.lang.obj.elf.Elf_Word,
    override var p_flags: cengine.lang.obj.elf.Elf_Word,
    var p_offset: cengine.lang.obj.elf.Elf64_Off = 0U,
    var p_vaddr: cengine.lang.obj.elf.Elf64_Addr = 0U,
    var p_paddr: cengine.lang.obj.elf.Elf64_Addr = 0U,
    var p_filesz: cengine.lang.obj.elf.Elf_Xword = 0U,
    var p_memsz: cengine.lang.obj.elf.Elf_Xword = 0U,
    var p_align: cengine.lang.obj.elf.Elf_Xword
) : Phdr {
    override fun build(endianness: Endianness): ByteArray {
        val b = ByteBuffer(endianness)

        b.put(p_type)
        b.put(p_flags)
        b.put(p_offset)
        b.put(p_vaddr)
        b.put(p_paddr)
        b.put(p_filesz)
        b.put(p_memsz)
        b.put(p_align)

        return b.toByteArray()
    }

    override fun byteSize(): Int = 56

}