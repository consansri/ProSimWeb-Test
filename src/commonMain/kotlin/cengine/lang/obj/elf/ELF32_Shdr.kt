package cengine.lang.obj.elf

import cengine.lang.obj.elf.Shdr.Companion.SHN_UNDEF
import cengine.util.ByteBuffer
import cengine.util.Endianness

/**
 * ELF Section Header
 *
 * @param sh_name This member specifies the name of the section. Its value is an index into
 * the section header string table section [see "String Table'' below], giving
 * the location of a null-terminated string.
 *
 * @param sh_type This member categorizes the section's contents and semantics.
 *
 * @param sh_flags Sections support 1-bit flags that describe miscellaneous attributes.
 *
 * @param sh_addr If the section will appear in the memory image of a process, this member
 * gives the address at which the section's first byte should reside. Otherwise,
 * the member contains 0.
 *
 * @param sh_offset This member's value gives the byte offset from the beginning of the file to
 * the first byte in the section. One section type, SHT_NOBITS described
 * below, occupies no space in the file, and its sh_offset member locates
 * the conceptual placement in the file.
 *
 * @param sh_size This member gives the section's size in bytes.  Unless the section type is
 * SHT_NOBITS, the section occupies sh_size bytes in the file. A section
 * of type SHT_NOBITS may have a non-zero size, but it occupies no space
 * in the file.
 *
 * @param sh_link This member holds a section header table index link, whose interpretation
 * depends on the section type. If not defined/used then its default value is [SHN_UNDEF].
 *
 * @param sh_info This member holds extra information, whose interpretation depends on the
 * section type. If not defined/used then its default value is [0U].
 *
 * @param sh_addralign Some sections have address alignment constraints. For example, if a section
 * holds a doubleword, the system must ensure doubleword alignment for the
 * entire section.  That is, the value of sh_addr must be congruent to 0,
 * modulo the value of sh_addralign. Currently, only 0 and positive
 * integral powers of two are allowed. Values 0 and 1 mean the section has no
 * alignment constraints.
 *
 * @param sh_entsize Some sections hold a table of fixed-size entries, such as a symbol table. For
 * such a section, this member gives the size in bytes of each entry. The
 * member contains 0 if the section does not hold a table of fixed-size entries.
 *
 *
 *
 */
data class ELF32_Shdr(
    override var sh_name: cengine.lang.obj.elf.Elf_Word = 0U,
    override var sh_type: cengine.lang.obj.elf.Elf_Word = SHT_NULL,
    var sh_flags: cengine.lang.obj.elf.Elf_Word = 0U,
    var sh_addr: cengine.lang.obj.elf.Elf32_Addr = 0U,
    var sh_offset: cengine.lang.obj.elf.Elf32_Off = 0U,
    var sh_size: cengine.lang.obj.elf.Elf_Word = 0U,
    override var sh_link: cengine.lang.obj.elf.Elf_Word = SHN_UNDEF.toUInt(),
    override var sh_info: cengine.lang.obj.elf.Elf_Word = 0U,
    var sh_addralign: cengine.lang.obj.elf.Elf_Word = 0U,
    var sh_entsize: cengine.lang.obj.elf.Elf_Word = 0U
) : Shdr() {

    companion object {
        fun create(nameIndex: cengine.lang.obj.elf.Elf_Word, type: cengine.lang.obj.elf.Elf_Word, flags: cengine.lang.obj.elf.Elf_Word): ELF32_Shdr {
            return ELF32_Shdr(
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

    override fun byteSize(): Int = 40
    override fun getEntSize(): cengine.lang.obj.elf.Elf_Xword = sh_entsize.toULong()

    override fun toString(): String = "$sh_name-${Shdr.getSectionType(sh_type)}-${Shdr.getSectionFlags(sh_flags.toULong())}"

}