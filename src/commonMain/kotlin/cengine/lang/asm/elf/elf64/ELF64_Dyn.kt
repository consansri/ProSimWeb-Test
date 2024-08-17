package cengine.lang.asm.elf.elf64

import cengine.lang.asm.elf.Dyn
import cengine.lang.asm.elf.Elf64_Addr
import cengine.lang.asm.elf.Elf_Sxword
import cengine.lang.asm.elf.Elf_Xword
import cengine.util.ByteBuffer
import cengine.util.Endianness

/**
 * Data class representing the Elf32_Dyn structure in the ELF format.
 *
 * @property d_tag The dynamic table entry type.
 * @property d_val The integer value associated with the dynamic table entry.
 * @property d_ptr The address associated with the dynamic table entry.
 */
data class ELF64_Dyn(
    var d_tag: Elf_Sxword,
    var d_val: Elf_Xword,
    var d_ptr: Elf64_Addr
): Dyn{
    override fun build(endianness: Endianness): ByteArray {
        val b = ByteBuffer(endianness)

        b.put(d_tag)
        b.put(d_val)
        b.put(d_ptr)

        return b.toByteArray()
    }

    override fun byteSize(): Int = 24
}
