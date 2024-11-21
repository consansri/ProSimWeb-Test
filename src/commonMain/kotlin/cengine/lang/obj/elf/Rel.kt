package cengine.lang.obj.elf

sealed class Rel: BinaryProvider {

    companion object {

        fun size(ei_class: Elf_Byte): Elf_Half {
            return when(ei_class){
                E_IDENT.ELFCLASS32 -> ELF32_Rel.SIZE.toUShort()
                E_IDENT.ELFCLASS64 -> ELF64_Rel.SIZE.toUShort()
                else -> throw ELFGenerator.InvalidElfClassException(ei_class)
            }
        }

        fun extractFrom(byteArray: ByteArray, eIdent: E_IDENT, offset: Int): Rel {
            var currIndex = offset
            when (eIdent.ei_class) {
                E_IDENT.ELFCLASS32 -> {
                    val r_offset = byteArray.loadUInt(eIdent, currIndex)
                    currIndex += 4
                    val r_info = byteArray.loadUInt(eIdent, currIndex)
                    return ELF32_Rel(r_offset, r_info)
                }
                E_IDENT.ELFCLASS64 -> {
                    val r_offset = byteArray.loadULong(eIdent, currIndex)
                    currIndex += 8
                    val r_info = byteArray.loadULong(eIdent, currIndex)
                    return ELF64_Rel(r_offset, r_info)
                }
                else -> throw NotInELFFormatException
            }
        }


    }

}