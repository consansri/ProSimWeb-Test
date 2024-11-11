package cengine.lang.obj.elf

import cengine.util.ByteBuffer
import cengine.util.Endianness

/**
 * Data class representing the Elf32_Dyn structure in the ELF format.
 *
 * @property d_tag The dynamic table entry type.
 * @property d_val The integer value associated with the dynamic table entry.
 * @property d_ptr The address associated with the dynamic table entry.
 */
data class ELF32_Dyn(
    var d_tag: Elf_Sword,
    var d_val: Elf_Word,
    var d_ptr: Elf32_Addr
): Dyn{
    override fun build(endianness: Endianness): ByteArray {
        val b = ByteBuffer(endianness)

        b.put(d_tag)
        b.put(d_val)
        b.put(d_ptr)

        return b.toByteArray()
    }

    override fun byteSize(): Int = 12
}
