package cengine.lang.asm.elf.elf64

import cengine.lang.asm.elf.Elf64_Addr
import cengine.lang.asm.elf.Elf_Xword
import cengine.lang.asm.elf.Rel
import cengine.util.ByteBuffer
import cengine.util.Endianness


/**
 * ELF Relocation Entry
 *
 * Relocation is the process of connecting symbolic references with symbolic definitions. For
 * example, when a program calls a function, the associated call instruction must transfer control
 * to the proper destination address at execution. In other words, relocatable files must have
 * information that describes how to modify their section contents, thus allowing executable and
 * shared object files to hold the right information for a process's program image. Relocation
 * entries are these data.
 *
 * @param r_offset This member gives the location at which to apply the relocation action. For
 * a relocatable file, the value is the byte offset from the beginning of the
 * section to the storage unit affected by the relocation. For an executable file
 * or a shared object, the value is the virtual address of the storage unit affected
 * by the relocation.
 *
 * @param r_info This member gives both the symbol table index with respect to which the
 * relocation must be made, and the type of relocation to apply. For example,
 * a call instruction's relocation entry would hold the symbol table index of
 * the function being called. If the index is STN_UNDEF, the undefined symbol
 * index, the relocation uses 0 as the "symbol value.''  Relocation types are
 * processor-specific; descriptions of their behavior appear in the processor
 * supplement. When the text in the processor supplement refers to a
 * relocation entry's relocation type or symbol table index, it means the result
 * of applying [ELF32_R_TYPE] or  [ELF32_R_SYM], respectively, to the
 * entry's [r_info] member.
 *
 */
data class ELF64_Rel(
    var r_offset: Elf64_Addr,
    var r_info: Elf_Xword
): Rel {

    override fun build(endianness: Endianness): ByteArray {
        val b = ByteBuffer(endianness)

        b.put(r_offset)
        b.put(r_info)

        return b.toByteArray()
    }
}