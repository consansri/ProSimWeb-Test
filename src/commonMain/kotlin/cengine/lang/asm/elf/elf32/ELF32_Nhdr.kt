package cengine.lang.asm.elf.elf32

import cengine.lang.asm.elf.Elf_Word
import cengine.lang.asm.elf.Nhdr

/**
 * Note Information
 *
 * Sometimes a vendor or system builder needs to mark an object file with special information
 * that other programs will check for conformance, compatibility, etc. Sections of type SHT_NOTE
 * and program header elements of type PT_NOTE can be used for this purpose.  The note
 * information in sections and program header elements holds any number of entries, each of
 * which is an array of 4-byte words in the format of the target processor.  Labels appear below
 * to help explain note information organization, but they are not part of the specification.
 *
 *
 * @param n_namesz
 * @param name
 * The first [n_namesz] bytes in [name] contain a null-terminated character
 * representation of the entry's owner or originator. There is no formal
 * mechanism for avoiding name conflicts. By convention, vendors use their
 * own [name], such as "XYZ Computer Company,'' as the identifier.  If no name
 * is present, [n_namesz] contains 0. Padding is present, if necessary, to ensure
 * 4-byte alignment for the descriptor. Such padding is not included in
 * [n_namesz].
 *
 * @param n_descsz
 * @param descs
 * The first [n_descsz] bytes in [desc] hold the note descriptor. ELF places no
 * constraints on a descriptor's contents. If no descriptor is present, [n_descsz]
 * contains 0. Padding is present, if necessary, to ensure 4-byte alignment for
 * the next note entry. Such padding is not included in [n_descsz].
 *
 * @param n_type This word gives the interpretation of the descriptor. Each originator controls
 * its own types; multiple interpretations of a single type value may exist.
 * Thus, a program must recognize both the name and the type to "understand"
 * a descriptor.  Types currently must be non-negative. ELF does not define
 * what descriptors mean.
 */
data class ELF32_Nhdr(
    override var n_namesz: Elf_Word,
    override var n_descsz: Elf_Word,
    override var n_type: Elf_Word,
): Nhdr {

    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }
}