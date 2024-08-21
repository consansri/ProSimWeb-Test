package cengine.lang.asm.elf.elf32

import cengine.lang.asm.elf.*
import cengine.util.ByteBuffer
import cengine.util.Endianness

/**
 * Symbol Table
 *
 * An object file's symbol table holds information needed to locate and relocate a program's
 * symbolic definitions and references.  A symbol table index is a subscript into this array. Index
 * 0 both designates the first entry in the table and serves as the undefined symbol index. The
 * contents of the initial entry are specified later in this section.
 *
 * @param st_name This member holds an index into the object file's symbol string table, which holds
 * the character representations of the symbol names.
 *
 * @param st_value This member gives the value of the associated symbol. Depending on the context,
 * this may be an absolute value, an address, and so on; details appear below.
 *
 * @param st_size Many symbols have associated sizes. For example, a data object's size is the number
 * of bytes contained in the object. This member holds 0 if the symbol has no size or
 * an unknown size.
 *
 * @param st_info This member specifies the symbol's type and binding attributes. A list of the values
 * and meanings appears below. The following code shows how to manipulate the
 * values.
 *
 * @param st_other This member currently holds 0 and has no defined meaning.
 *
 * @param st_shndx Every symbol table entry is "defined'' in relation to some section; this member holds
 * the relevant section header table index. As Figure 1-7 and the related text describe,
 * some section indexes indicate special meanings.
 *
 */
data class ELF32_Sym(
    override var st_name: Elf_Word,
    var st_value: Elf32_Addr,
    var st_size: Elf_Word,
    override var st_info: Elf_Byte,
    override var st_other: Elf_Byte = 0U,
    override var st_shndx: Elf_Half
): Sym {
    override fun build(endianness: Endianness): ByteArray {
        val b = ByteBuffer(endianness)

        b.put(st_name)
        b.put(st_value)
        b.put(st_size)
        b.put(st_info)
        b.put(st_other)
        b.put(st_shndx)

        return b.toByteArray()
    }

    override fun byteSize(): Int = 16

}