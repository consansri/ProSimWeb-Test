package cengine.lang.asm.elf.elf32

import cengine.lang.asm.elf.Dyn
import cengine.lang.asm.elf.Elf32_Addr
import cengine.lang.asm.elf.Elf_Sword
import cengine.lang.asm.elf.Elf_Word

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

    override fun build(): ByteArray {
        TODO("Not yet implemented")
    }
}
